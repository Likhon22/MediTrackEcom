package com.example.demo5;

import java.sql.Connection;
import java.sql.DriverManager;

public class database {
    public static Connection connectDb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3307/pharmacy?useSSL=false&serverTimezone=UTC",
                    "root", "");

            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
