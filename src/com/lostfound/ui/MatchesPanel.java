package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.model.User;
import com.lostfound.service.ItemService;
import com.lostfound.service.MatchingService;
import com.lostfound.service.MatchingService.MatchResult;
import com.lostfound.util.ImageUtil;
import com.lostfound.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchesPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ItemService itemService;
    private final MatchingService matchingService;

    private JTable matchTable;
    private DefaultTableModel tableModel;

    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JLabel statusBannerLabel;
    private JButton viewButton;
    private JButton refreshButton;
    private JPanel buttonPanel;

    public MatchesPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.itemService = new ItemService();
        this.matchingService = new MatchingService();

        setLayout(new BorderLayout(12, 12));
        Theme.stylePanel(this);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        createHeading();
        createTable();
        createButtons();

        Theme.addThemeChangeListener(this);
        loadMatches();
    }

    private void createHeading() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(Theme.getBackground());

        headingLabel = new JLabel("MATCHING ENGINE");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Automated similarity matching comparing names, categories, locations, and descriptions across Lost & Found records."
        );
        Theme.styleLabel(subtitleLabel);

        headerPanel.add(headingLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {
                "Lost ID", "Lost Item", "Found ID", "Found Item", "Category", "Lost Location", "Found Location", "Match Confidence"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        matchTable = new JTable(tableModel);
        matchTable.setRowHeight(34);
        matchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchTable.setAutoCreateRowSorter(true);

        Theme.styleTable(matchTable);

        matchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && matchTable.getSelectedRow() != -1) {
                    viewSelectedMatch();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(matchTable);
        Theme.styleScrollPane(scrollPane);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setBackground(Theme.getBackground());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        statusBannerLabel = new JLabel("", SwingConstants.LEFT);
        statusBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBannerLabel.setForeground(Theme.getTextPrimary());
        centerPanel.add(statusBannerLabel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void createButtons() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(Theme.getBackground());

        viewButton = new JButton("🔍 Inspect Match Comparison");
        refreshButton = new JButton("↻ Re-run Matching");

        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(refreshButton);

        viewButton.addActionListener(e -> viewSelectedMatch());
        refreshButton.addActionListener(e -> loadMatches());

        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadMatches() {
        tableModel.setRowCount(0);
        List<Item> allItems = itemService.getAllItems();
        Set<String> displayedPairs = new HashSet<>();

        for (Item item : allItems) {
            if (!"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                if (item.getReportedByUsername() == null
                        || !item.getReportedByUsername().equalsIgnoreCase(loggedInUser.getUsername())) {
                    continue;
                }
            }

            List<MatchResult> results = matchingService.findMatches(item);
            for (MatchResult result : results) {
                Item lost = result.getLostItem();
                Item found = result.getFoundItem();
                String pairKey = lost.getId() + "-" + found.getId();

                if (!displayedPairs.add(pairKey)) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        lost.getId(),
                        lost.getItemName(),
                        found.getId(),
                        found.getItemName(),
                        lost.getCategory(),
                        lost.getLocation(),
                        found.getLocation(),
                        String.format("%.1f%%", result.getPercentage())
                });
            }
        }

        if (tableModel.getRowCount() == 0) {
            statusBannerLabel.setText("ℹ No potential matches discovered yet with >= 30% similarity score.");
            statusBannerLabel.setForeground(Theme.getTextMuted());
        } else {
            statusBannerLabel.setText("✦ Found " + tableModel.getRowCount() + " potential match pair(s) based on multi-parameter text analysis.");
            statusBannerLabel.setForeground(Theme.getTextPrimary());
        }
    }

    private void viewSelectedMatch() {
        int selectedRow = matchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a match row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = matchTable.convertRowIndexToModel(selectedRow);
        int lostId = (int) tableModel.getValueAt(modelRow, 0);
        int foundId = (int) tableModel.getValueAt(modelRow, 2);
        String confidence = String.valueOf(tableModel.getValueAt(modelRow, 7));

        Item lostItem = null;
        Item foundItem = null;
        for (Item item : itemService.getAllItems()) {
            if (item.getId() == lostId) lostItem = item;
            if (item.getId() == foundId) foundItem = item;
        }

        if (lostItem == null || foundItem == null) {
            JOptionPane.showMessageDialog(this, "Item records could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Match Inspection (" + confidence + " Confidence)",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.getBackground());
        dialog.setLayout(new BorderLayout(12, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.getCard());
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JLabel title = new JLabel("AUTOMATED MATCH COMPARISON");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.getPrimaryAccent());

        JLabel confBadge = new JLabel("CONFIDENCE: " + confidence);
        confBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confBadge.setForeground(Theme.STATUS_APPROVED);

        header.add(title, BorderLayout.WEST);
        header.add(confBadge, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // Center: 2 Cards Side-by-Side
        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 14));
        grid.setBackground(Theme.getBackground());
        grid.setBorder(new EmptyBorder(0, 16, 0, 16));

        grid.add(createItemComparisonCard("LOST ITEM SPECIFICATION", lostItem, Theme.STATUS_LOST));
        grid.add(createItemComparisonCard("FOUND ITEM SPECIFICATION", foundItem, Theme.STATUS_FOUND));

        dialog.add(grid, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Theme.getCard());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.getBorder()));

        JButton closeBtn = new JButton("Close Inspection");
        Theme.stylePrimaryButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createItemComparisonCard(String title, Item item, Color titleColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.getCard());
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(titleColor);
        card.add(header);
        card.add(Box.createVerticalStrut(6));

        addDetailRow(card, "Item Name:", item.getItemName());
        addDetailRow(card, "Item ID:", "#" + item.getId());
        addDetailRow(card, "Category:", item.getCategory());
        addDetailRow(card, "Location:", item.getLocation());
        addDetailRow(card, "Status:", item.getStatus());
        addDetailRow(card, "Contact:", item.getContact());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        addDetailRow(card, "Date Reported:", item.getItemDate() != null ? df.format(item.getItemDate()) : "—");

        card.add(Box.createVerticalStrut(6));
        JLabel descHeader = new JLabel("Description:");
        descHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        descHeader.setForeground(Theme.getTextMuted());
        card.add(descHeader);
        card.add(Box.createVerticalStrut(2));

        JTextArea descArea = new JTextArea(item.getDescription() != null ? item.getDescription() : "None", 3, 16);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(Theme.getBackground());
        descArea.setForeground(Theme.getTextPrimary());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane sp = new JScrollPane(descArea);
        Theme.styleScrollPane(sp);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sp);

        // Photo thumbnail if exists
        String path = item.getImagePath();
        if (path != null && !path.trim().isEmpty() && new File(path).exists()) {
            card.add(Box.createVerticalStrut(6));
            ImageIcon icon = ImageUtil.loadScaledIcon(path, 100, 70);
            if (icon != null) {
                JLabel imgLabel = new JLabel(icon);
                imgLabel.setBorder(new LineBorder(Theme.getBorder(), 1, true));
                imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(imgLabel);
            }
        }

        return card;
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(Theme.getCard());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.getTextMuted());

        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(Theme.getTextPrimary());

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(2));
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        Theme.stylePanel(this);
        Theme.styleBrandHeading(headingLabel);
        Theme.styleLabel(subtitleLabel);
        Theme.styleTable(matchTable);
        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(refreshButton);
        buttonPanel.setBackground(Theme.getBackground());
        repaint();
    }
}
