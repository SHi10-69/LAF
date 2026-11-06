package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageItemPanel extends JPanel {

    private JTable itemTable;
    private DefaultTableModel tableModel;

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewButton;
    private JButton refreshButton;

    private final ItemService itemService;

    public ManageItemPanel() {

        itemService = new ItemService();

        createGUI();

        loadItems();
    }

    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        // =====================================
        // TITLE
        // =====================================

        JLabel titleLabel =
                new JLabel(
                        "MANAGE LOST & FOUND ITEMS",
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        add(
                titleLabel,
                BorderLayout.NORTH
        );


        // =====================================
        // TABLE
        // =====================================

        String[] columns = {
                "ID",
                "Item Name",
                "Category",
                "Type",
                "Location",
                "Owner",
                "Status",
                "Quantity"
        };


        tableModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };


        itemTable =
                new JTable(tableModel);


        itemTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        itemTable.setRowHeight(25);


        JScrollPane scrollPane =
                new JScrollPane(itemTable);


        add(
                scrollPane,
                BorderLayout.CENTER
        );


        // =====================================
        // BUTTONS
        // =====================================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                10
                        )
                );


        addButton =
                new JButton("ADD");


        editButton =
                new JButton("EDIT");


        deleteButton =
                new JButton("DELETE");


        viewButton =
                new JButton("VIEW");


        refreshButton =
                new JButton("REFRESH");


        buttonPanel.add(addButton);

        buttonPanel.add(editButton);

        buttonPanel.add(deleteButton);

        buttonPanel.add(viewButton);

        buttonPanel.add(refreshButton);


        add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        // =====================================
        // EVENTS
        // =====================================

        addButton.addActionListener(
                e -> addItem()
        );


        editButton.addActionListener(
                e -> editItem()
        );


        deleteButton.addActionListener(
                e -> deleteItem()
        );


        viewButton.addActionListener(
                e -> viewItem()
        );


        refreshButton.addActionListener(
                e -> loadItems()
        );
    }


    // =========================================
    // LOAD ITEMS
    // =========================================

    private void loadItems() {

        tableModel.setRowCount(0);

        List<Item> items =
                itemService.getAllItems();


        for (Item item : items) {

            tableModel.addRow(
                    new Object[]{
                            item.getId(),
                            item.getItemName(),
                            item.getCategory(),
                            item.getItemType(),
                            item.getLocation(),
                            item.getOwnerName(),
                            item.getStatus(),
                            item.getQuantity()
                    }
            );
        }
    }


    // =========================================
    // GET SELECTED ITEM
    // =========================================

    private Item getSelectedItem() {

        int row =
                itemTable.getSelectedRow();


        if (row == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an item.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }


        int id =
                (Integer)
                        tableModel.getValueAt(
                                row,
                                0
                        );


        List<Item> items =
                itemService.getAllItems();


        for (Item item : items) {

            if (item.getId() == id) {

                return item;
            }
        }


        return null;
    }


    // =========================================
    // ADD
    // =========================================

    private void addItem() {

        JDialog dialog =
                new JDialog(
                        SwingUtilities
                                .getWindowAncestor(this),
                        "Add Item",
                        Dialog.ModalityType.APPLICATION_MODAL
                );


        ReportItemPanel panel =
                new ReportItemPanel();


        dialog.add(panel);


        dialog.setSize(
                850,
                650
        );


        dialog.setLocationRelativeTo(this);


        dialog.setVisible(true);


        loadItems();
    }


    // =========================================
    // EDIT
    // =========================================

    private void editItem() {

        Item item =
                getSelectedItem();


        if (item == null) {
            return;
        }


        EditItemDialog dialog =
                new EditItemDialog(
                        SwingUtilities
                                .getWindowAncestor(this),
                        item,
                        itemService
                );


        dialog.setVisible(true);


        loadItems();
    }


    // =========================================
    // DELETE
    // =========================================

    private void deleteItem() {

        Item item =
                getSelectedItem();


        if (item == null) {
            return;
        }


        int answer =
                JOptionPane.showConfirmDialog(
                        this,

                        "Are you sure you want to delete:\n"
                                + item.getItemName()
                                + "?",

                        "Confirm Delete",

                        JOptionPane.YES_NO_OPTION,

                        JOptionPane.WARNING_MESSAGE
                );


        if (
                answer ==
                        JOptionPane.YES_OPTION
        ) {

            itemService.deleteItem(
                    item.getId()
            );


            JOptionPane.showMessageDialog(
                    this,

                    "Item deleted successfully.",

                    "Deleted",

                    JOptionPane.INFORMATION_MESSAGE
            );


            loadItems();
        }
    }


    // =========================================
    // VIEW
    // =========================================

    private void viewItem() {

        Item item =
                getSelectedItem();


        if (item == null) {
            return;
        }


        JTextArea details =
                new JTextArea();


        details.setEditable(false);

        details.setLineWrap(true);

        details.setWrapStyleWord(true);


        details.setText(
                "ITEM DETAILS\n\n"
                        + "ID: "
                        + item.getId()
                        + "\n\n"
                        + "Name: "
                        + item.getItemName()
                        + "\n\n"
                        + "Category: "
                        + item.getCategory()
                        + "\n\n"
                        + "Type: "
                        + item.getItemType()
                        + "\n\n"
                        + "Location: "
                        + item.getLocation()
                        + "\n\n"
                        + "Owner: "
                        + item.getOwnerName()
                        + "\n\n"
                        + "Contact: "
                        + item.getContact()
                        + "\n\n"
                        + "Quantity: "
                        + item.getQuantity()
                        + "\n\n"
                        + "Accessories: "
                        + item.getAccessories()
                        + "\n\n"
                        + "Urgency: "
                        + item.getUrgency()
                        + "\n\n"
                        + "Status: "
                        + item.getStatus()
                        + "\n\n"
                        + "Description:\n"
                        + item.getDescription()
        );


        JScrollPane scrollPane =
                new JScrollPane(details);


        scrollPane.setPreferredSize(
                new Dimension(
                        450,
                        450
                )
        );


        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Item Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}