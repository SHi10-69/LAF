package com.lostfound.service;

import com.lostfound.model.Item;
import com.lostfound.storage.FileStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemService {

    private static final String FILE_NAME = "items.dat";

    // Get all items (auto-seeds realistic sample items if inventory is empty)
    public List<Item> getAllItems() {
        List<Item> items = FileStorage.loadList(FILE_NAME);
        if (items.isEmpty()) {
            items = seedInitialItems();
            saveItems(items);
        }
        return items;
    }

    private List<Item> seedInitialItems() {
        List<Item> list = new ArrayList<>();
        Date now = new Date();

        Item it1 = new Item();
        it1.setId(1);
        it1.setItemName("Apple MacBook Pro 14 Space Gray");
        it1.setCategory("Electronics");
        it1.setItemType("FOUND");
        it1.setLocation("Central Library 2nd Floor Study Room");
        it1.setDescription("Space gray Apple MacBook Pro 14-inch found near window cubicle. Has silver hard shell case.");
        it1.setOwnerName("Security Officer");
        it1.setContact("security@lostfound.com");
        it1.setStatus("FOUND");
        it1.setReportedByUsername("admin");
        it1.setUrgency(3);
        it1.setQuantity(1);
        it1.setItemDate(now);
        list.add(it1);

        Item it2 = new Item();
        it2.setId(2);
        it2.setItemName("MacBook Pro 14-inch Space Gray");
        it2.setCategory("Electronics");
        it2.setItemType("LOST");
        it2.setLocation("Central Library");
        it2.setDescription("Left my Apple MacBook Pro 14 space gray during study session. Case has slight scratch on bottom.");
        it2.setOwnerName("Alex Chen");
        it2.setContact("alex.chen@lostfound.com");
        it2.setStatus("LOST");
        it2.setReportedByUsername("admin");
        it2.setUrgency(3);
        it2.setQuantity(1);
        it2.setItemDate(now);
        list.add(it2);

        Item it3 = new Item();
        it3.setId(3);
        it3.setItemName("Brown Leather Bi-Fold Wallet");
        it3.setCategory("Wallet & Purses");
        it3.setItemType("FOUND");
        it3.setLocation("Dining Commons Main Hall");
        it3.setDescription("Brown genuine leather wallet containing membership cards and receipt.");
        it3.setOwnerName("Staff Associate");
        it3.setContact("staff@lostfound.com");
        it3.setStatus("FOUND");
        it3.setReportedByUsername("admin");
        it3.setUrgency(2);
        it3.setQuantity(1);
        it3.setItemDate(now);
        list.add(it3);

        Item it4 = new Item();
        it4.setId(4);
        it4.setItemName("Sony WH-1000XM5 Noise Cancelling Headphones");
        it4.setCategory("Electronics");
        it4.setItemType("FOUND");
        it4.setLocation("Science Complex Lecture Hall 101");
        it4.setDescription("Black Sony over-ear headphones in zippered carrying case.");
        it4.setOwnerName("Lecture Proctor");
        it4.setContact("proctor@lostfound.com");
        it4.setStatus("FOUND");
        it4.setReportedByUsername("admin");
        it4.setUrgency(3);
        it4.setQuantity(1);
        it4.setItemDate(now);
        list.add(it4);

        Item it5 = new Item();
        it5.setId(5);
        it5.setItemName("Honda Car Key with Black Lanyard");
        it5.setCategory("Keys");
        it5.setItemType("LOST");
        it5.setLocation("North Parking Structure Level 2");
        it5.setDescription("Key fob with lock/unlock buttons attached to black branded lanyard.");
        it5.setOwnerName("David Kim");
        it5.setContact("david.kim@lostfound.com");
        it5.setStatus("LOST");
        it5.setReportedByUsername("admin");
        it5.setUrgency(2);
        it5.setQuantity(1);
        it5.setItemDate(now);
        list.add(it5);

        return list;
    }

    // Add new item
    public void addItem(Item item) {
        List<Item> items = getAllItems();
        item.setId(generateNextId(items));
        items.add(item);
        saveItems(items);
    }

    // Update existing item
    public void updateItem(Item updatedItem) {
        List<Item> items = getAllItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == updatedItem.getId()) {
                items.set(i, updatedItem);
                break;
            }
        }
        saveItems(items);
    }

    // Delete item
    public void deleteItem(int id) {
        List<Item> items = getAllItems();
        items.removeIf(item -> item.getId() == id);
        saveItems(items);
    }

    // Search items
    public List<Item> searchItems(String keyword) {
        List<Item> allItems = getAllItems();
        List<Item> results = new ArrayList<>();
        String search = keyword.toLowerCase().trim();

        for (Item item : allItems) {
            if ((item.getItemName() != null && item.getItemName().toLowerCase().contains(search))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(search))
                    || (item.getLocation() != null && item.getLocation().toLowerCase().contains(search))
                    || (item.getDescription() != null && item.getDescription().toLowerCase().contains(search))) {
                results.add(item);
            }
        }
        return results;
    }

    // Generate next item ID
    private int generateNextId(List<Item> items) {
        int maxId = 0;
        for (Item item : items) {
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        return maxId + 1;
    }

    // Count all reported items
    public int countItems() {
        return getAllItems().size();
    }

    // Count lost items
    public int countLostItems() {
        int count = 0;
        for (Item item : getAllItems()) {
            if ("LOST".equalsIgnoreCase(item.getItemType())) {
                count++;
            }
        }
        return count;
    }

    // Count found items
    public int countFoundItems() {
        int count = 0;
        for (Item item : getAllItems()) {
            if ("FOUND".equalsIgnoreCase(item.getItemType())) {
                count++;
            }
        }
        return count;
    }

    // Count pending items
    public int countPendingItems() {
        int count = 0;
        for (Item item : getAllItems()) {
            if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean updateItemStatus(int itemId, String newStatus) {
        List<Item> items = getAllItems();
        for (Item item : items) {
            if (item.getId() == itemId) {
                item.setStatus(newStatus);
                try {
                    FileStorage.saveList(FILE_NAME, items);
                    List<Item> savedItems = getAllItems();
                    for (Item savedItem : savedItems) {
                        if (savedItem.getId() == itemId) {
                            return newStatus.equalsIgnoreCase(savedItem.getStatus());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    // Save items to local file
    private void saveItems(List<Item> items) {
        try {
            FileStorage.saveList(FILE_NAME, items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
