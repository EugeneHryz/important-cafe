package com.eugene.cafe.model.service.impl;

import com.eugene.cafe.entity.MenuItem;
import com.eugene.cafe.entity.Order;
import com.eugene.cafe.entity.Review;
import com.eugene.cafe.entity.User;
import com.eugene.cafe.exception.DaoException;
import com.eugene.cafe.exception.ServiceException;
import com.eugene.cafe.model.dao.OrderDao;
import com.eugene.cafe.model.dao.ReviewDao;
import com.eugene.cafe.model.dao.TransactionHelper;
import com.eugene.cafe.model.dao.UserDao;
import com.eugene.cafe.model.dao.impl.OrderDaoImpl;
import com.eugene.cafe.model.dao.impl.ReviewDaoImpl;
import com.eugene.cafe.model.dao.impl.UserDaoImpl;
import com.eugene.cafe.model.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);

    private static final int ORDERS_PER_PAGE = 4;

    @Override
    public Optional<User> placeOrder(int userId, double orderTotal, Map<MenuItem, Integer> menuItems, LocalTime pickupTime) throws ServiceException {

        final TransactionHelper helper = new TransactionHelper();
        final OrderDao orderDao = new OrderDaoImpl();
        final UserDao userDao = new UserDaoImpl();

        List<MenuItem> menuItemsAsList = new ArrayList<>();
        for (Map.Entry<MenuItem, Integer> entry : menuItems.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                menuItemsAsList.add(entry.getKey());
            }
        }
        Optional<User> updatedUser = Optional.empty();
        try {
            helper.beginTransaction(orderDao, userDao);

            LocalDateTime dateTime = pickupTime.atDate(LocalDate.now());
            Order.Builder builder = new Order.Builder();
            builder.setUserId(userId)
                    .setDate(Timestamp.valueOf(LocalDateTime.now()))
                    .setPickupTime(Timestamp.valueOf(dateTime))
                    .setMenuItems(menuItems)
                    .setTotalPrice(orderTotal);
            Order order = builder.buildOrder();

            Optional<User> userOptional = userDao.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getBalance() >= orderTotal) {

                    user.setBalance(user.getBalance() - orderTotal);
                    if (orderDao.create(order) && orderDao.createOrderMenuItemMappings(order, menuItemsAsList)) {
                        updatedUser = userDao.update(user);
                    }
                }
            }
            helper.commit();
        } catch (DaoException e) {
            try {
                helper.rollback();
            } catch (DaoException daoException) {
                logger.error("Failed to undo transaction changes", daoException);
            }
            logger.error("Failed to place order", e);
            throw new ServiceException("Failed to place order", e);
        } finally {
            try {
                helper.endTransaction();
            } catch (DaoException e) {
                logger.error("Failed to end transaction", e);
            }
        }
        return updatedUser;
    }

    @Override
    public List<Order> getSubsetOfUserOrders(int userId, int pageNumber) throws ServiceException {

        final TransactionHelper helper = new TransactionHelper();
        final OrderDao orderDao = new OrderDaoImpl();
        final ReviewDao reviewDao = new ReviewDaoImpl();

        helper.init(orderDao);
        helper.init(reviewDao);
        List<Order> userOrders = new ArrayList<>();
        // todo: validate page number
        int offset = ORDERS_PER_PAGE * (pageNumber - 1);
        try {
            userOrders = orderDao.getSubsetOfUserOrders(userId, offset, ORDERS_PER_PAGE);

            for (Order order : userOrders) {
                List<MenuItem> menuItems = orderDao.findMenuItemsByOrderId(order.getId());
                Map<MenuItem, Integer> orderContent = convertListToMap(menuItems);

                Optional<Review> review = reviewDao.findOrderReview(order.getId());
                review.ifPresent(order::setReview);

                order.setMenuItems(orderContent);
            }
        } catch (DaoException e) {
            // todo: write log
            throw new ServiceException("Unable to get a subset of orders", e);
        } finally {
            helper.end();
        }
        return userOrders;
    }

    @Override
    public int getUserOrderCount(int userId) throws ServiceException {

        final TransactionHelper helper = new TransactionHelper();
        final OrderDao orderDao = new OrderDaoImpl();

        helper.init(orderDao);
        int count;
        try {
            count = orderDao.getUserOrderCount(userId);
        } catch (DaoException e) {
            // todo: write log
            throw new ServiceException("Failed to get user order count", e);
        } finally {
            helper.end();
        }
        return count;
    }

    @Override
    public double calculateOrderTotal(Map<MenuItem, Integer> shoppingCart) throws ServiceException {
        // todo: is this necessary?
        if (shoppingCart == null) {
            logger.error("shopping cart is null");
            throw new ServiceException("shoppingCart is null");
        }

        double total = 0;
        for (Map.Entry<MenuItem, Integer> entry : shoppingCart.entrySet()) {
            total += (entry.getValue() * entry.getKey().getPrice());
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(total);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    @Override
    public boolean saveOrderReview(int orderId, short rating, String comment) throws ServiceException {

        final TransactionHelper helper = new TransactionHelper();
        final ReviewDao reviewDao = new ReviewDaoImpl();

        helper.init(reviewDao);
        boolean reviewSaved;
        try {
            Optional<Review> orderReview = reviewDao.findOrderReview(orderId);
            if (orderReview.isPresent()) {
                Review review = orderReview.get();
                review.setRating(rating);
                review.setComment(comment);
                review.setDate(Timestamp.valueOf(LocalDateTime.now()));

                reviewSaved = reviewDao.update(review).isPresent();
            } else {
                Review.Builder builder = new Review.Builder();
                builder.setOrderId(orderId)
                        .setRating(rating)
                        .setComment(comment)
                        .setDate(Timestamp.valueOf(LocalDateTime.now()));
                System.out.println("building review");

                reviewSaved = reviewDao.create(builder.buildReview());
                System.out.println("review add result: " + reviewSaved);
            }
        } catch (DaoException e) {
            // todo: write log
            throw new ServiceException("Unable to save review");
        } finally {
            helper.end();
        }
        return reviewSaved;
    }

    private Map<MenuItem, Integer> convertListToMap(List<MenuItem> menuItems) {
        Map<MenuItem, Integer> map = new HashMap<>();

        for (MenuItem item : menuItems) {
            if (!map.containsKey(item)) {
                map.put(item, 1);
            } else {
                map.put(item, map.get(item) + 1);
            }
        }
        return map;
    }
}
