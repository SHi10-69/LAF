package com.lostfound.storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    private static File getDataFolder() {
        File explicit = new File("d:/Program Files/Java/LostFound/data");
        if (explicit.exists() || explicit.getParentFile().exists()) {
            if (!explicit.exists()) explicit.mkdirs();
            return explicit;
        }
        File relative = new File("data");
        if (!relative.exists()) relative.mkdirs();
        return relative;
    }

    public static <T> void saveList(String fileName, List<T> list) throws IOException {
        File file = new File(getDataFolder(), fileName);
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(list);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadList(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                String name = desc.getName();
                if (name.startsWith("com.smartlostfound.")) {
                    name = name.replace("com.smartlostfound.", "com.lostfound.");
                    return Class.forName(name);
                }
                return super.resolveClass(desc);
            }
        }) {
            return (List<T>) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
