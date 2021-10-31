package com;


import java.util.List;
import java.sql.*;

public class DBAuthService implements AuthService{
    private static DBAuthService INSTANCE;

    private static String DB_URL="jdbc:postgresql://127.0.0.1:5433/gb";
    private static String USER="postgres";
    private static String PASS="password";
    private static Connection connection;

    public static DBAuthService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DBAuthService();
        }
        return INSTANCE;
    }

    DBAuthService() {

        try {
            connect();

        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public static void connect () throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        System.out.println("PostgreSQL JDBC Driver successfully connected");
        connection = DriverManager.getConnection(DB_URL,USER,PASS);
        System.out.println("Success connected to DB");
    }
    public static void disconnect () {
        if (connection!=null){
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }}
    }



    @Override
    public String findByLogin(String login) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SELECT password FROM USERS WHERE login = " + "'%s';", login));
            {
                if (!resultSet.next()) return null;
                else {
                    String password = resultSet.getString("password");
                    return password;
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();

        }
        return null;
    }

    @Override
    public Object save(Object object) {
        return null;
    }

    @Override
    public Object remove(Object object) {
        return null;
    }

    @Override
    public Object removeById(Object o) {
        return null;
    }

    @Override
    public Object findById(Object o) {
        return null;
    }

    @Override
    public List findAll() {
        return null;
    }
}