package com.example.fastlibdemo.db;

import com.fastlib.annotation.Database;

public class PersonBeen {

    @Database(keyPrimary = true)
    public String name;
    public int age;
    public String intro;

    private PersonBeen() {
        //私有构造
    }

    public PersonBeen(String name, int age, String intro) {
        this.name = name;
        this.age = age;
        this.intro = intro;
    }
}
