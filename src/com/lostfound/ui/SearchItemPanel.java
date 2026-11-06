
package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SearchItemPanel extends JPanel {

    private JTextField searchField;

    private JComboBox<String> categoryComboBox;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> statusComboBox;

    private JTable itemTable;
    private DefaultTableModel tableModel;

    private JButton searchButton;
    private JButton clearButton;
    private JButton viewButton;
    private JButton claimButton;

    private final ItemService itemService;

    public SearchItemPanel() {
        itemService = new ItemService();

        createGUI();
        loadAllItems();
    }

    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15
        ));

        // =====================================
        // TITLE
        // =====================================

        JLabel titleLabel = new JLabel(
                "SEARCH LOST & FOUND ITEMS",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        add(titleLabel, BorderLayout.NORTH);

        // =====================================
        // FILTER PANEL
        // =====================================

        JPanel filterPanel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        filterPanel.add(new JLabel("Search:"), gbc);

        searchField = new JTextField(20);

        gbc.gridx = 1;
        gbc.weightx = 1;

        filterPanel.add(searchField, gbc);

        // Category

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        filterPanel.add(new JLabel("Category:"), gbc);

        categoryComboBox = new JComboBox<>(
                new String[]{
                        "All",
                        "Electronics",
                        "Documents",
                        "Wallet",
                        "Keys",
                        "Clothing",
                        "Jewellery",
                        "Books",
                        "Bags",
                        "Other"
                }
        );

        gbc.gridx = 1;

        filterPanel.add(categoryComboBox, gbc);

        // Type

        gbc.gridx = 0;
        gbc.gridy = 2;

        filterPanel.add(new JLabel("Type:"), gbc);

        typeComboBox = new JComboBox<>(
                new String[]{
                        "All",
                        "Lost",
                        "Found"
                }
        );

        gbc.gridx = 1;

        filterPanel.add(typeComboBox, gbc);

        // Status

        gbc.gridx = 0;
        gbc.gridy = 3;

        filterPanel.add(new JLabel("Status:"), gbc);

        statusComboBox = new JComboBox<>(
                new String[]{
                        "All",
                        "Reported",
                        "Claimed",
                        "Resolved"
                }
        );

        gbc.gridx = 1;

        filterPanel.add(statusComboBox, gbc);

        // Search and Clear buttons

        JPanel filterButtonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        searchButton = new JButton("SEARCH");
        clearButton = new JButton("CLEAR");

        filterButtonPanel.add(searchButton);
        filterButtonPanel.add(clearButton);

        gbc.gridx = 1;
        gbc.gridy = 4;

        filterPanel.add(filterButtonPanel, gbc);

        add(filterPanel, BorderLayout.WEST);

        // =====================================
        // ITEMS TABLE
        // =====================================

        String[] columns = {
                "ID",
                "Item Name",
                "Category",
                "Type",
                "Location",
                "Status",
                "Quantity"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);

        itemTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        itemTable.setRowHeight(25);
        itemTable.setAutoCreateRowSorter(true);

        itemTable.getTableHeader()
                .setReorderingAllowed(false);

        JScrollPane tableScrollPane =
                new JScrollPane(itemTable);

        add(tableScrollPane, BorderLayout.CENTER);

        // =====================================
        // BOTTOM BUTTONS
        // =====================================

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER)
        );

        viewButton = new JButton("VIEW DETAILS");
        claimButton = new JButton("CLAIM ITEM");

        bottomPanel.add(viewButton);
        bottomPanel.add(claimButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // =====================================
        // BUTTON EVENTS
        // =====================================

        searchButton.addActionListener(
                e -> performSearch()
        );

        clearButton.addActionListener(
                e -> clearSearch()
        );

        viewButton.addActionListener(
                e -> viewSelectedItem()
        );

        claimButton.addActionListener(
                e -> claimSelectedItem()
        );

        searchField.addActionListener(
                e -> performSearch()
        );
    }

    // =====================================
    // LOAD ALL ITEMS
    // =====================================

    private void loadAllItems() {

        List<Item> items = itemService.getAllItems();

        displayItems(items);
    }

    // =====================================
    // SEARCH ITEMS
    // =====================================

    private void performSearch() {

        String keyword = searchField.getText()
                .trim()
                .toLowerCase();

        String category =
                (String) categoryComboBox.getSelectedItem();

        String type =
                (String) typeComboBox.getSelectedItem();

        String status =
                (String) statusComboBox.getSelectedItem();

        List<Item> allItems = itemService.getAllItems();

        List<Item> results = allItems.stream()
                .filter(item -> {

                    boolean matchesKeyword =
                            keyword.isEmpty()
                                    || safe(item.getItemName())
                                    .toLowerCase().contains(keyword)
                                    || safe(item.getDescription())
                                    .toLowerCase().contains(keyword)
                                    || safe(item.getLocation())
                                    .toLowerCase().contains(keyword);

                    boolean matchesCategory =
                            "All".equals(category)
                                    || safe(item.getCategory())
                                    .equalsIgnoreCase(category);

                    boolean matchesType =
                            "All".equals(type)
                                    || safe(item.getItemType())
                                    .equalsIgnoreCase(type);

                    boolean matchesStatus =
                            "All".equals(status)
                                    || safe(item.getStatus())
                                    .equalsIgnoreCase(status);

                    return matchesKeyword
                            && matchesCategory
                            && matchesType
                            && matchesStatus;
                })
                .collect(Collectors.toList());

        displayItems(results);
    }

    // =====================================
    // SAFE STRING HELPER
    // =====================================

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // =====================================
    // DISPLAY ITEMS
    // =====================================

    private void displayItems(List<Item> items) {

        tableModel.setRowCount(0);

        for (Item item : items) {

            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getItemName(),
                    item.getCategory(),
                    item.getItemType(),
                    item.getLocation(),
                    item.getStatus(),
                    item.getQuantity()
            });
        }
    }

    // =====================================
    // CLEAR SEARCH
    // =====================================

    private void clearSearch() {

        searchField.setText("");

        categoryComboBox.setSelectedIndex(0);
        typeComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);

        loadAllItems();
    }

    // =====================================
    // GET SELECTED ITEM
    // =====================================

    private Item getSelectedItem() {

        int selectedRow = itemTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an item first.",
                    "No Item Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        // Convert the selected view row to the model row.
        int modelRow =
                itemTable.convertRowIndexToModel(selectedRow);

        Object idValue = tableModel.getValueAt(modelRow, 0);

        int itemId = ((Number) idValue).intValue();

        List<Item> items = itemService.getAllItems();

        for (Item item : items) {

            if (item.getId() == itemId) {
                return item;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "The selected item could not be found.",
                "Item Not Found",
                JOptionPane.WARNING_MESSAGE
        );

        return null;
    }

    // =====================================
    // VIEW ITEM DETAILS
    // =====================================

    private void viewSelectedItem() {

        Item item = getSelectedItem();

        if (item == null) {
            return;
        }

        String message =
                "ITEM DETAILS\n\n"
                        + "ID: " + item.getId()
                        + "\nName: " + safe(item.getItemName())
                        + "\nCategory: " + safe(item.getCategory())
                        + "\nType: " + safe(item.getItemType())
                        + "\nLocation: " + safe(item.getLocation())
                        + "\nOwner: " + safe(item.getOwnerName())
                        + "\nContact: " + safe(item.getContact())
                        + "\nQuantity: " + item.getQuantity()
                        + "\nAccessories: " + safe(item.getAccessories())
                        + "\nUrgency: " + item.getUrgency()
                        + "\nStatus: " + safe(item.getStatus())
                        + "\n\nDescription:\n"
                        + safe(item.getDescription());

        JTextArea textArea = new JTextArea(message, 15, 40);

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Item Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =====================================
    // CLAIM ITEM - POPUP
    // =====================================

    private void claimSelectedItem() {

        Item item = getSelectedItem();

        if (item == null) {
            return;
        }

        String status = safe(item.getStatus());

        if ("Claimed".equalsIgnoreCase(status)
                || "Resolved".equalsIgnoreCase(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "This item is no longer available for claiming.",
                    "Item Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Get the main application window.
        Window window =
                SwingUtilities.getWindowAncestor(this);

        Frame owner = window instanceof Frame
                ? (Frame) window
                : null;

        // Open the existing ClaimVerificationPanel in a popup.
        JDialog dialog = new JDialog(
                owner,
                "Claim Verification",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        ClaimVerificationPanel verificationPanel =
                new ClaimVerificationPanel();

        dialog.setContentPane(verificationPanel);

        dialog.setSize(750, 550);
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(this);

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        dialog.setVisible(true);

        // Refresh items after closing the popup.
        loadAllItems();
    }
    public void refreshItems() {
        loadAllItems();
    }
}