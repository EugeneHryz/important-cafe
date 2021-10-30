package com.eugene.cafe.model.dao;

import com.eugene.cafe.entity.User;
import com.eugene.cafe.exception.DaoException;

import java.util.List;
import java.util.Optional;

public abstract class UserDao extends AbstractDao<User> {

    public abstract Optional<User> findByEmail(String email) throws DaoException;

    public abstract Optional<User> updateProfilePicture(int id, String imagePath) throws DaoException;

    public abstract boolean changePassword(int id, String newPassword) throws DaoException;

    public abstract List<User> getSubsetOfUsers(int limit, int offset) throws DaoException;

    public abstract int getCount() throws DaoException;
}
