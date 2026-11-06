package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.model.User;
import com.lostfound.service.ItemService;
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
import java.util.ArrayList;
import java.util.List;

public class BrowseItemsPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ItemService itemService;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> typeFilter;

    private JTable itemTable;
    private DefaultTableModel tableModel;
    private List<Item> allItems;

    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JButton searchButton;
    private JButton resetButton;
    private JButton viewButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton statusButton;
    private JButton refreshButton;
    private JPanel searchPanel;
    private JPanel buttonPanel;

    public BrowseItemsPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.itemService = new ItemService();

        setLayout(new BorderLayout(12, 12));
        Theme.stylePanel(this);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        createHeaderAndSearch();
        createTable();
        createButtonPanel();

        Theme.addThemeChangeListener(this);
        loadItems();
    }

    private void createHeaderAndSearch() {
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(Theme.getBackground());

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout(4, 4));
        titlePanel.setBackground(Theme.getBackground());
        headingLabel = new JLabel("BROWSE LOST & FOUND INVENTORY");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Filter and inspect all reported items across the system. Double-click or select an item to inspect photos and details."
        );
        Theme.styleLabel(subtitleLabel);

        titlePanel.add(headingLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        northContainer.add(titlePanel);
        northContainer.add(Box.createVerticalStrut(10));

        // Search Bar Card
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        Theme.styleCard(searchPanel);

        searchField = new JTextField(16);
        Theme.styleTextField(searchField);

        categoryFilter = new JComboBox<>(new String[]{
                "All Categories", "Electronics", "Books & Stationery", "ID Card & Badges",
                "Wallet & Purses", "Keys", "Clothing & Apparel", "Bags & Backpacks", "Other"
        });
        Theme.styleComboBox(categoryFilter);

        typeFilter = new JComboBox<>(new String[]{"All Types", "LOST", "FOUND"});
        Theme.styleComboBox(typeFilter);

        searchButton = new JButton("⌕ Search");
        resetButton = new JButton("↻ Reset");
        Theme.stylePrimaryButton(searchButton);
        Theme.styleSecondaryButton(resetButton);

        JLabel sLabel = new JLabel("Search:");
        sLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sLabel.setForeground(Theme.getTextPrimary());
        searchPanel.add(sLabel);
        searchPanel.add(searchField);

        JLabel cLabel = new JLabel("Category:");
        cLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cLabel.setForeground(Theme.getTextPrimary());
        searchPanel.add(cLabel);
        searchPanel.add(categoryFilter);

        JLabel tLabel = new JLabel("Type:");
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLabel.setForeground(Theme.getTextPrimary());
        searchPanel.add(tLabel);
        searchPanel.add(typeFilter);

        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        searchButton.addActionListener(e -> applyFilters());
        searchField.addActionListener(e -> applyFilters());
        categoryFilter.addActionListener(e -> applyFilters());
        typeFilter.addActionListener(e -> applyFilters());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            typeFilter.setSelectedIndex(0);
            applyFilters();
        });

        northContainer.add(searchPanel);
        add(northContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {
                "ID", "Item Name", "Category", "Type", "Location", "Contact", "Photo", "Status", "Urgency"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(34);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoCreateRowSorter(true);

        Theme.styleTable(itemTable);

        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && itemTable.getSelectedRow() != -1) {
                    viewSelectedItem();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        Theme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(Theme.getBackground());

        viewButton = new JButton("🔍 View Details & Photo");
        editButton = new JButton("✎ Edit My Item");
        deleteButton = new JButton("✕ Delete Item");
        statusButton = new JButton("⚙ Update Status");
        refreshButton = new JButton("↻ Refresh");

        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(editButton);
        Theme.styleDangerButton(deleteButton);
        Theme.styleSecondaryButton(statusButton);
        Theme.styleSecondaryButton(refreshButton);

        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);

        if (loggedInUser != null && "ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            buttonPanel.add(deleteButton);
            buttonPanel.add(statusButton);
        }

        buttonPanel.add(refreshButton);

        viewButton.addActionListener(e -> viewSelectedItem());
        editButton.addActionListener(e -> editSelectedItem());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        statusButton.addActionListener(e -> updateSelectedStatus());
        refreshButton.addActionListener(e -> loadItems());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadItems() {
        allItems = itemService.getAllItems();
        if (allItems == null) {
            allItems = new ArrayList<>();
        }
        applyFilters();
    }

    private void applyFilters() {
        if (allItems == null) return;
        tableModel.setRowCount(0);

        String keyword = searchField.getText().trim().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedType = (String) typeFilter.getSelectedItem();

        for (Item item : allItems) {
            String name = safe(item.getItemName());
            String category = safe(item.getCategory());
            String location = safe(item.getLocation());
            String description = safe(item.getDescription());
            String type = safe(item.getItemType());

            boolean matchesKeyword = keyword.isEmpty()
                    || name.toLowerCase().contains(keyword)
                    || category.toLowerCase().contains(keyword)
                    || location.toLowerCase().contains(keyword)
                    || description.toLowerCase().contains(keyword);

            boolean matchesCategory = "All Categories".equals(selectedCategory)
                    || selectedCategory.equalsIgnoreCase(category);

            boolean matchesType = "All Types".equals(selectedType)
                    || selectedType.equalsIgnoreCase(type);

            if (matchesKeyword && matchesCategory && matchesType) {
                boolean hasPhoto = item.getImagePath() != null
                        && !item.getImagePath().trim().isEmpty()
                        && new File(item.getImagePath()).exists();

                tableModel.addRow(new Object[]{
                        item.getId(),
                        name,
                        category,
                        type,
                        location,
                        safe(item.getContact()),
                        hasPhoto ? "📷 Photo" : "— None",
                        safe(item.getStatus()),
                        getUrgencyText(item.getUrgency())
                });
            }
        }
    }

    private void viewSelectedItem() {
        Item item = getSelectedItem();
        if (item == null) return;

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Item Specification - #" + item.getId() + " (" + safe(item.getItemName()) + ")",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(680, 560);
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

        JLabel title = new JLabel(safe(item.getItemName()).toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Theme.getPrimaryAccent());

        JLabel badge = new JLabel("TYPE: " + safe(item.getItemType()) + "  |  STATUS: " + safe(item.getStatus()));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground("FOUND".equalsIgnoreCase(item.getItemType()) ? Theme.STATUS_FOUND : Theme.STATUS_LOST);

        header.add(title, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // Center split
        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.setBackground(Theme.getBackground());
        center.setBorder(new EmptyBorder(0, 16, 0, 16));

        // Left: Details
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Theme.getCard());
        left.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        addDetailRow(left, "Item ID:", "#" + item.getId());
        addDetailRow(left, "Category:", safe(item.getCategory()));
        addDetailRow(left, "Location:", safe(item.getLocation()));
        addDetailRow(left, "Reporter / Owner:", safe(item.getOwnerName()));
        addDetailRow(left, "Contact:", safe(item.getContact()));
        addDetailRow(left, "Quantity:", String.valueOf(item.getQuantity()));
        addDetailRow(left, "Priority:", getUrgencyText(item.getUrgency()));
        addDetailRow(left, "Accessories:", safe(item.getAccessories()));

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        addDetailRow(left, "Report Date:", item.getItemDate() != null ? df.format(item.getItemDate()) : "—");

        left.add(Box.createVerticalStrut(8));
        JLabel descHeader = new JLabel("Description:");
        descHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        descHeader.setForeground(Theme.getTextMuted());
        left.add(descHeader);
        left.add(Box.createVerticalStrut(4));

        JTextArea descArea = new JTextArea(safe(item.getDescription()), 3, 20);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(Theme.getBackground());
        descArea.setForeground(Theme.getTextPrimary());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane dScroll = new JScrollPane(descArea);
        Theme.styleScrollPane(dScroll);
        dScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        dScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(dScroll);

        center.add(left);

        // Right: Photo
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(Theme.getCard());
        right.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel photoTitle = new JLabel("ITEM PHOTOGRAPH");
        photoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        photoTitle.setForeground(Theme.getPrimaryAccent());
        right.add(photoTitle, BorderLayout.NORTH);

        String imgPath = item.getImagePath();
        boolean hasImg = imgPath != null && !imgPath.trim().isEmpty() && new File(imgPath).exists();

        if (hasImg) {
            JPanel imgHolder = new JPanel(new BorderLayout());
            imgHolder.setBackground(Theme.getBackground());
            imgHolder.setBorder(new LineBorder(Theme.getBorder(), 1));

            ImageIcon icon = ImageUtil.loadScaledIcon(imgPath, 280, 260);
            JLabel imgLabel = new JLabel(icon != null ? icon : null, SwingConstants.CENTER);
            imgHolder.add(imgLabel, BorderLayout.CENTER);
            right.add(imgHolder, BorderLayout.CENTER);

            JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            toolBar.setBackground(Theme.getCard());

            JButton fullBtn = new JButton("🔍 Full View");
            JButton extBtn = new JButton("🖥 External Viewer");
            Theme.styleSecondaryButton(fullBtn);
            Theme.styleSecondaryButton(extBtn);

            final String fPath = imgPath;
            fullBtn.addActionListener(e -> ImageUtil.showFullResolutionViewer(dialog, fPath, "Item #" + item.getId()));
            extBtn.addActionListener(e -> ImageUtil.openInSystemViewer(fPath, dialog));

            toolBar.add(fullBtn);
            toolBar.add(extBtn);
            right.add(toolBar, BorderLayout.SOUTH);
        } else {
            JPanel noImgPanel = new JPanel(new GridBagLayout());
            noImgPanel.setBackground(Theme.getBackground());
            noImgPanel.setBorder(new LineBorder(Theme.getBorder(), 1));

            JLabel noPhotoLbl = new JLabel("📷  No Photo Uploaded", SwingConstants.CENTER);
            noPhotoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noPhotoLbl.setForeground(Theme.getTextMuted());
            noImgPanel.add(noPhotoLbl);
            right.add(noImgPanel, BorderLayout.CENTER);
        }

        center.add(right);
        dialog.add(center, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Theme.getCard());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.getBorder()));

        JButton closeBtn = new JButton("Close");
        Theme.stylePrimaryButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(Theme.getCard());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.getTextMuted());

        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(Theme.getTextPrimary());

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(2));
    }

    private Item getSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int modelRow = itemTable.convertRowIndexToModel(selectedRow);
        int selectedId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

        for (Item item : allItems) {
            if (item.getId() == selectedId) {
                return item;
            }
        }
        return null;
    }

    private void deleteSelectedItem() {
        Item item = getSelectedItem();
        if (item == null) return;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete Item #" + item.getId() + " (" + safe(item.getItemName()) + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            itemService.deleteItem(item.getId());
            JOptionPane.showMessageDialog(this, "Item #" + item.getId() + " was deleted successfully.");
            loadItems();
        }
    }

    private void updateSelectedStatus() {
        Item item = getSelectedItem();
        if (item == null) return;

        String[] statuses = {"PENDING", "APPROVED", "REJECTED", "RESOLVED"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status for Item #" + item.getId() + ":",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                item.getStatus()
        );

        if (newStatus != null) {
            boolean updated = itemService.updateItemStatus(item.getId(), newStatus);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Status updated to " + newStatus + ".");
                loadItems();
            }
        }
    }

    private void editSelectedItem() {
        Item item = getSelectedItem();
        if (item == null) return;

        if (loggedInUser == null || item.getReportedByUsername() == null ||
                (!item.getReportedByUsername().equalsIgnoreCase(loggedInUser.getUsername())
                        && !"ADMIN".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You can edit only the items reported by your account (or admin accounts).", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EditItemDialog dialog = new EditItemDialog(SwingUtilities.getWindowAncestor(this), item, itemService);
        dialog.setVisible(true);
        loadItems();
    }

    private String getUrgencyText(int urgency) {
        switch (urgency) {
            case 1: return "LOW";
            case 2: return "MEDIUM";
            case 3: return "HIGH";
            default: return String.valueOf(urgency);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        Theme.stylePanel(this);
        Theme.styleBrandHeading(headingLabel);
        Theme.styleLabel(subtitleLabel);
        Theme.styleCard(searchPanel);
        Theme.styleTable(itemTable);
        Theme.stylePrimaryButton(searchButton);
        Theme.styleSecondaryButton(resetButton);
        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(editButton);
        Theme.styleDangerButton(deleteButton);
        Theme.styleSecondaryButton(statusButton);
        Theme.styleSecondaryButton(refreshButton);
        buttonPanel.setBackground(Theme.getBackground());
        repaint();
    }
}
