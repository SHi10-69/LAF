package com.lostfound.service;

import com.lostfound.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchingService {

    private final ItemService itemService;

    public MatchingService() {
        itemService = new ItemService();
    }

    // Find possible matches for a given LOST or FOUND item
    public List<MatchResult> findMatches(Item targetItem) {
        List<Item> allItems = itemService.getAllItems();
        List<MatchResult> matches = new ArrayList<>();

        if (targetItem == null || targetItem.getItemType() == null) {
            return matches;
        }

        for (Item otherItem : allItems) {
            // Do not compare an item with itself
            if (otherItem.getId() == targetItem.getId()) {
                continue;
            }

            // Only compare LOST with FOUND
            if (targetItem.getItemType().equalsIgnoreCase(otherItem.getItemType())) {
                continue;
            }

            // Ignore completed or closed items
            String status = otherItem.getStatus();
            if ("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
                continue;
            }

            double score = calculateMatchPercentage(targetItem, otherItem);

            // Display matches with at least 30% similarity
            if (score >= 30) {
                Item lostItem;
                Item foundItem;

                if ("LOST".equalsIgnoreCase(targetItem.getItemType())) {
                    lostItem = targetItem;
                    foundItem = otherItem;
                } else {
                    lostItem = otherItem;
                    foundItem = targetItem;
                }

                matches.add(new MatchResult(lostItem, foundItem, score));
            }
        }

        // Show highest matching percentage first
        matches.sort(Comparator.comparingDouble(MatchResult::getPercentage).reversed());
        return matches;
    }

    // Calculate overall matching percentage
    private double calculateMatchPercentage(Item item1, Item item2) {
        double nameScore = compareText(item1.getItemName(), item2.getItemName());
        double categoryScore = compareText(item1.getCategory(), item2.getCategory());
        double locationScore = compareText(item1.getLocation(), item2.getLocation());
        double descriptionScore = compareText(item1.getDescription(), item2.getDescription());

        double total = (nameScore * 40)
                + (categoryScore * 25)
                + (locationScore * 20)
                + (descriptionScore * 15);

        return Math.round(total * 100.0) / 100.0;
    }

    // Compare two text values and return similarity from 0.0 to 1.0
    private double compareText(String text1, String text2) {
        if (text1 == null || text2 == null
                || text1.trim().isEmpty()
                || text2.trim().isEmpty()) {
            return 0.0;
        }

        String first = text1.toLowerCase().trim();
        String second = text2.toLowerCase().trim();

        // Exact match
        if (first.equals(second)) {
            return 1.0;
        }

        // One text contains the other
        if (first.contains(second) || second.contains(first)) {
            return 0.8;
        }

        // Compare individual words
        String[] words1 = first.split("\\s+");
        String[] words2 = second.split("\\s+");

        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }

        int totalWords = words1.length + words2.length - commonWords;
        if (totalWords == 0) {
            return 0.0;
        }

        return (double) commonWords / totalWords;
    }

    // Stores one possible LOST and FOUND match
    public static class MatchResult {
        private final Item lostItem;
        private final Item foundItem;
        private final double percentage;

        public MatchResult(Item lostItem, Item foundItem, double percentage) {
            this.lostItem = lostItem;
            this.foundItem = foundItem;
            this.percentage = percentage;
        }

        public Item getLostItem() {
            return lostItem;
        }

        public Item getFoundItem() {
            return foundItem;
        }

        public double getPercentage() {
            return percentage;
        }
    }
}
