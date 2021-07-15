package com.example.fastlibdemo.db;

import com.fastlib.annotation.Database;

import java.util.List;

/**
 * Created by liuwp on 2020/5/14.
 */
public class ProvinceBeen {

    @Database(keyPrimary = true,autoincrement = true)
    public int id;
    public String name;
    public int score;
    public List<City> cities;

    public static class City {
        public String name;

        public City(String name) {
            this.name = name;
        }
    }
}
