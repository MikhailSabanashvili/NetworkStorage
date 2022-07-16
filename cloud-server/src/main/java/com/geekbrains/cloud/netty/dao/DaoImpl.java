package com.geekbrains.cloud.netty.dao;

import lombok.Getter;

import java.sql.*;

public class DaoImpl implements Dao{
    private Connection connection;
    private Statement statement;
    @Getter
    private static DaoImpl dao = new DaoImpl();

    private DaoImpl() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:identifier.sqlite");
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertUser(String login, String password, String firstName, String secondName, String email) {
        try {
            statement.executeUpdate(String.format("INSERT INTO users(login, password, first_name, second_name, email) " +
                    "VALUES('%s', '%s', '%s', '%s', '%s')", login, password, firstName, secondName, email));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getId(String login) {
        try {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT id FROM users WHERE login='%s'", login));
            return resultSet.getString("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Cant return id from database");
    }

    @Override
    public User getUser(String login) {
        try {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT login, password, first_name, second_name," +
                    " email FROM users WHERE login='%s'", login));
            return new User(resultSet.getString("login"),
                    resultSet.getString("password"),
                    resultSet.getString("first_name"),
                    resultSet.getString("second_name"),
                    resultSet.getString("email"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
