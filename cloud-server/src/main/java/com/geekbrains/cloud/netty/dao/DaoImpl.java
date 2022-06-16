package com.geekbrains.cloud.netty.dao;

import java.sql.*;

public class DaoImpl implements Dao{
    private Connection connection;
    private Statement statement;

    public DaoImpl() {
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
    public boolean isExist(String login, String password) {
        try {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT login, password FROM users WHERE login='%s' and password='%s'",
                    login, password));
            if(resultSet.getString("login") != null && resultSet.getString("password") != null)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
