package com.eugene.cafe.model.dao.impl;

import com.eugene.cafe.entity.Category;
import com.eugene.cafe.model.dao.MenuItemDao;
import com.eugene.cafe.entity.MenuItem;
import com.eugene.cafe.exception.DaoException;
import com.eugene.cafe.model.dao.MenuItemSortOrder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.eugene.cafe.model.dao.DatabaseColumnLabel.*;

public class MenuItemDaoImpl extends MenuItemDao {

    private static final Logger logger = LogManager.getLogger(MenuItemDaoImpl.class);

    private static final String SQL_CREATE_MENU_ITEM = "INSERT INTO menu_items(name, description, price, category_id, image) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_MENU_ITEM_BY_ID = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items WHERE menu_items.id = ?";

    private static final String SQL_FIND_ALL_MENU_ITEMS = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items";

    private static final String SQL_FIND_SUBSET_PRICE_ASC = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items ORDER BY price LIMIT ? OFFSET ?";

    private static final String SQL_FIND_SUBSET_PRICE_DESC = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items ORDER BY price DESC LIMIT ? OFFSET ?";

    private static final String SQL_FIND_SUBSET_BY_CATEGORY_PRICE_ASC = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items WHERE category_id = ? ORDER BY price LIMIT ? OFFSET ?";

    private static final String SQL_FIND_SUBSET_BY_CATEGORY_PRICE_DESC = "SELECT id, name, description, price, category_id, image " +
            "FROM menu_items WHERE category_id = ? ORDER BY price DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_ALL = "SELECT COUNT(*) FROM menu_items";

    private static final String SQL_COUNT_BY_CATEGORY = "SELECT COUNT(*) FROM menu_items WHERE menu_items.category_id = ?";

    private static final String SQL_UPDATE_MENU_ITEM = "UPDATE menu_items " +
            "SET name = ?, description = ?, price = ?, category_id = ?, image = ? WHERE menu_items.id = ?";

    private static final String SQL_DELETE_MENU_ITEM_BY_ID = "DELETE FROM menu_items WHERE menu_items.id = ?";

    @Override
    public boolean create(MenuItem entity) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        boolean created = false;
        try (PreparedStatement statement = connection.prepareStatement(SQL_CREATE_MENU_ITEM)) {
            initStatement(statement, entity);

            if (statement.executeUpdate() > 0) {
                created = true;
            }
        } catch (SQLException e) {
            System.out.println("sql exception: " + e);
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return created;
    }

    @Override
    public Optional<MenuItem> findById(int id) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        Optional<MenuItem> result = Optional.empty();
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_MENU_ITEM_BY_ID)) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                MenuItem menuItem = buildMenuItem(resultSet);
                result = Optional.of(menuItem);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return result;
    }

    @Override
    public List<MenuItem> findAll() throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        List<MenuItem> menuItems = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SQL_FIND_ALL_MENU_ITEMS);

            while (resultSet.next()) {
                MenuItem menuItem = buildMenuItem(resultSet);
                menuItems.add(menuItem);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return menuItems;
    }

    @Override
    public Optional<MenuItem> update(MenuItem entity) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        Optional<MenuItem> updated = Optional.empty();
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_MENU_ITEM)) {
            initStatement(statement, entity);
            statement.setInt(6, entity.getId());

            if (statement.executeUpdate() > 0) {
                updated = Optional.of(entity);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return updated;
    }

    @Override
    public boolean deleteById(int id) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        boolean deleted = false;
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_MENU_ITEM_BY_ID)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() > 0) {
                deleted = true;
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return deleted;
    }

    @Override
    public List<MenuItem> getSubsetOfMenuItems(int limit, int offset, MenuItemSortOrder sortOrder) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        List<MenuItem> menuItems = new ArrayList<>();
        String statementSql = (sortOrder == MenuItemSortOrder.PRICE_ASCENDING) ? SQL_FIND_SUBSET_PRICE_ASC :
                SQL_FIND_SUBSET_PRICE_DESC;
        try (PreparedStatement statement = connection.prepareStatement(statementSql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                MenuItem menuItem = buildMenuItem(resultSet);
                menuItems.add(menuItem);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return menuItems;
    }

    @Override
    public List<MenuItem> getSubsetOfMenuItemsByCategory(int limit, int offset, MenuItemSortOrder sortOrder, Category category) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        List<MenuItem> menuItems = new ArrayList<>();
        String statementSql = (sortOrder == MenuItemSortOrder.PRICE_ASCENDING) ? SQL_FIND_SUBSET_BY_CATEGORY_PRICE_ASC :
                SQL_FIND_SUBSET_BY_CATEGORY_PRICE_DESC;
        try (PreparedStatement statement = connection.prepareStatement(statementSql)) {
            statement.setInt(1, category.getId());
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                MenuItem menuItem = buildMenuItem(resultSet);
                menuItems.add(menuItem);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred " + e);
            throw new DaoException("Database error occurred", e);
        }
        return menuItems;
    }

    @Override
    public int getCount() throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        int number = 0;
        try (Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(SQL_COUNT_ALL);
            if (resultSet.next()) {
                number = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            // todo: write log
            throw new DaoException("Database error occurred", e);
        }
        return number;
    }

    @Override
    public int getCountByCategory(Category category) throws DaoException {
        if (connection == null) {
            logger.error("Database connection is not set for MenuItemDao");
            throw new DaoException("Database connection is not set for MenuItemDao");
        }

        int number = 0;
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_BY_CATEGORY)) {
            statement.setInt(1, category.getId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                number = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            // todo: write log
            throw new DaoException("Database error occurred", e);
        }
        return number;
    }

    private MenuItem buildMenuItem(ResultSet resultSet) throws SQLException {

        MenuItem.Builder builder = new MenuItem.Builder();
        builder.setId(resultSet.getInt(MENU_ITEMS_ID))
                .setName(resultSet.getString(MENU_ITEMS_NAME))
                .setDescription(resultSet.getString(MENU_ITEMS_DESCRIPTION))
                .setPrice(resultSet.getDouble(MENU_ITEMS_PRICE))
                .setCategoryId(resultSet.getInt(MENU_ITEMS_CATEGORY_ID))
                .setImagePath(resultSet.getString(MENU_ITEMS_IMAGE));

        return builder.buildMenuItem();
    }

    private void initStatement(PreparedStatement statement, MenuItem entity) throws SQLException {

        statement.setString(1, entity.getName());
        statement.setString(2, entity.getDescription());
        statement.setDouble(3, entity.getPrice());
        statement.setString(4, entity.getImagePath());
        statement.setInt(5, entity.getCategoryId());
    }
}
