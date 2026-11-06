
package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StatisticsPanel extends JPanel {

    private final ItemService itemService = new ItemService();

    private JLabel totalLabel;
    private JLabel lostLabel;
    private JLabel foundLabel;
    private JLabel claimedLabel;
    private JLabel resolvedLabel;

    private JProgressBar lostProgress;
    private JProgressBar foundProgress;
    private JProgressBar claimedProgress;

    private final Color purple = new Color(91, 55, 180);
    private final Color background = new Color(245, 243, 255);

    public StatisticsPanel() {
        createGUI();
        refreshStatistics();
    }

    private void createGUI() {

        setLayout(new BorderLayout(15, 20));
        setBackground(background);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(purple);
        header.setBorder(new EmptyBorder(22, 15, 22, 15));

        JLabel title = new JLabel(
                "LOST & FOUND STATISTICS",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(background);

        // STATISTIC CARDS
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 12));
        cards.setBackground(background);
        cards.setPreferredSize(new Dimension(900, 190));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        totalLabel = createCard(cards, "TOTAL ITEMS",
                new Color(116, 82, 190));

        lostLabel = createCard(cards, "LOST ITEMS",
                new Color(225, 65, 65));

        foundLabel = createCard(cards, "FOUND ITEMS",
                new Color(35, 165, 105));

        claimedLabel = createCard(cards, "CLAIMED ITEMS",
                new Color(235, 145, 35));

        resolvedLabel = createCard(cards, "RESOLVED",
                new Color(145, 75, 180));

        content.add(cards);
        content.add(Box.createVerticalStrut(25));

        // PROGRESS SECTION
        JPanel progressPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        progressPanel.setBackground(background);

        lostProgress = createProgressCard(progressPanel, "Lost Items",
                new Color(225, 65, 65));

        foundProgress = createProgressCard(progressPanel, "Found Items",
                new Color(35, 165, 105));

        claimedProgress = createProgressCard(progressPanel, "Claimed Items",
                new Color(235, 145, 35));

        progressPanel.setPreferredSize(new Dimension(900, 130));
        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        content.add(progressPanel);

        add(content, BorderLayout.CENTER);

        // REFRESH BUTTON
        JButton refreshButton = new JButton("REFRESH STATISTICS");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 15));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(purple);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(new EmptyBorder(13, 30, 13, 30));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        refreshButton.addActionListener(e -> refreshStatistics());

        JPanel footer = new JPanel();
        footer.setBackground(background);
        footer.add(refreshButton);

        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createCard(
            JPanel parent,
            String title,
            Color color) {

        JPanel card = new JPanel(new BorderLayout(5, 15));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(20, 8, 20, 8));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel("0", SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 34));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        parent.add(card);

        return valueLabel;
    }

    private JProgressBar createProgressCard(
            JPanel parent,
            String title,
            Color color) {

        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 215, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(45, 35, 85));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setForeground(color);
        bar.setBackground(new Color(230, 228, 240));
        bar.setBorderPainted(false);
        bar.setFont(new Font("Arial", Font.BOLD, 13));

        card.add(label, BorderLayout.NORTH);
        card.add(bar, BorderLayout.CENTER);

        parent.add(card);

        return bar;
    }

    public void refreshStatistics() {

        List<Item> items = itemService.getAllItems();

        int total = items.size();
        int lost = 0;
        int found = 0;
        int claimed = 0;
        int resolved = 0;

        for (Item item : items) {

            if ("LOST".equalsIgnoreCase(item.getItemType())) {
                lost++;
            }

            if ("FOUND".equalsIgnoreCase(item.getItemType())) {
                found++;
            }

            if ("CLAIMED".equalsIgnoreCase(item.getStatus())) {
                claimed++;
            }

            if ("RESOLVED".equalsIgnoreCase(item.getStatus())) {
                resolved++;
            }
        }

        totalLabel.setText(String.valueOf(total));
        lostLabel.setText(String.valueOf(lost));
        foundLabel.setText(String.valueOf(found));
        claimedLabel.setText(String.valueOf(claimed));
        resolvedLabel.setText(String.valueOf(resolved));

        lostProgress.setValue(total == 0 ? 0 : lost * 100 / total);
        foundProgress.setValue(total == 0 ? 0 : found * 100 / total);
        claimedProgress.setValue(total == 0 ? 0 : claimed * 100 / total);
    }
}
