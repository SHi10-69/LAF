package com.lostfound.storage;

import com.lostfound.model.User;

import java.util.ArrayList;
import java.util.List;

public class TestStorage {

    public static void main(String[] args) {

        List<User> users = new ArrayList<>();

        User user = new User(
                1,
                "test",
                "1234",
                "Test User",
                "USER"
        );

        users.add(user);

        try {

            FileStorage.saveList(
                    "test.dat",
                    users
            );

            System.out.println(
                    "Data saved successfully!"
            );

            List<User> loadedUsers =
                    FileStorage.loadList(
                            "test.dat"
                    );

            System.out.println(
                    "Users loaded: "
                            + loadedUsers.size()
            );

            System.out.println(
                    "Username: "
                            + loadedUsers
                            .get(0)
                            .getUsername()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}