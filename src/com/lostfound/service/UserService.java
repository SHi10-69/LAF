
package com.lostfound.service;

import com.lostfound.model.User;
import com.lostfound.storage.FileStorage;

import java.util.List;

public class UserService {

    private static final String FILE_NAME = "users.dat";

    public UserService() {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {

        List<User> users =
                FileStorage.loadList(FILE_NAME);

        boolean adminExists = false;

        for (User user : users) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                adminExists = true;
                break;
            }
        }

        if (!adminExists) {

            User admin = new User(
                    getNextId(users),
                    "admin",
                    "admin123",
                    "Administrator",
                    "admin@lostfound.com",
                    "ADMIN"
            );

            users.add(admin);
            saveUsers(users);
        }
    }

    public boolean registerUser(
            String fullName,
            String username,
            String email,
            String password) {

        List<User> users =
                FileStorage.loadList(FILE_NAME);

        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(username)) {
                return false;
            }

            if (user.getEmail() != null &&
                    user.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        User newUser = new User(
                getNextId(users),
                username,
                password,
                fullName,
                email,
                "USER"
        );

        users.add(newUser);

        return saveUsers(users);
    }

    public User login(String username, String password) {

        List<User> users =
                FileStorage.loadList(FILE_NAME);

        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(username)
                    && user.getPassword().equals(password)) {

                if (user.isBlocked()) {
                    return null;
                }

                return user;
            }
        }

        return null;
    }

    public List<User> getAllUsers() {
        return FileStorage.loadList(FILE_NAME);
    }

    public boolean toggleBlock(int userId) {
        List<User> users = FileStorage.loadList(FILE_NAME);
        for (User user : users) {
            if (user.getId() == userId && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                user.setBlocked(!user.isBlocked());
                return saveUsers(users);
            }
        }
        return false;
    }

    public boolean setBlocked(int userId, boolean blocked) {

        List<User> users =
                FileStorage.loadList(FILE_NAME);

        for (User user : users) {

            if (user.getId() == userId
                    && !"ADMIN".equalsIgnoreCase(user.getRole())) {

                user.setBlocked(blocked);
                return saveUsers(users);
            }
        }

        return false;
    }

    private int getNextId(List<User> users) {

        int maxId = 0;

        for (User user : users) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }

        return maxId + 1;
    }

    private boolean saveUsers(List<User> users) {

        try {
            FileStorage.saveList(FILE_NAME, users);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
