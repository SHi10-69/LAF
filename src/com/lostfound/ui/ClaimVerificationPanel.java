
package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimVerificationPanel extends JPanel {

    private static final String DATA_FILE = "data/verification.dat";

    private final List<ItemVerification> items = new ArrayList<>();
    private final List<ClaimRecord> claims = new ArrayList<>();

    private JTextField itemIdField;
    private JTextField itemNameField;
    private JTextField questionField;
    private JPasswordField expectedAnswerField;

    private JTextField claimItemIdField;
    private JTextField claimantNameField;
    private JTextField contactField;
    private JLabel questionLabel;
    private JPasswordField claimantAnswerField;

    private JTable claimsTable;
    private DefaultTableModel tableModel;

    public ClaimVerificationPanel() {
        setLayout(new BorderLayout());

        loadData();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Register Item", createRegisterPanel());
        tabs.addTab("Submit Claim", createClaimPanel());
        tabs.addTab("Review Claims", createReviewPanel());

        add(tabs, BorderLayout.CENTER);
    }

    // =========================================
    // REGISTER ITEM
    // =========================================

    private JPanel createRegisterPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Register Private Ownership Details");
        heading.setFont(new Font("Arial", Font.BOLD, 20));

        itemIdField = new JTextField(20);
        itemNameField = new JTextField(20);
        questionField = new JTextField(20);
        expectedAnswerField = new JPasswordField(20);

        JButton saveButton = new JButton("Register Item");
        JButton clearButton = new JButton("Clear");

        int row = 0;

        addFormRow(panel, gbc, row++, heading, new JLabel(""));
        addFormRow(panel, gbc, row++, new JLabel("Item ID:"), itemIdField);
        addFormRow(panel, gbc, row++, new JLabel("Item Name:"), itemNameField);
        addFormRow(panel, gbc, row++, new JLabel("Private Question:"), questionField);
        addFormRow(panel, gbc, row++, new JLabel("Expected Answer:"), expectedAnswerField);
        addFormRow(panel, gbc, row++, saveButton, clearButton);

        JLabel note = new JLabel(
                "<html>Register a private ownership detail that is not publicly visible.</html>"
        );

        addFormRow(panel, gbc, row, note, new JLabel(""));

        saveButton.addActionListener(e -> registerItem());

        clearButton.addActionListener(e -> {
            itemIdField.setText("");
            itemNameField.setText("");
            questionField.setText("");
            expectedAnswerField.setText("");
        });

        return panel;
    }

    private void registerItem() {

        String id = itemIdField.getText().trim();
        String name = itemNameField.getText().trim();
        String question = questionField.getText().trim();
        String answer = new String(expectedAnswerField.getPassword()).trim();

        if (id.isEmpty() || name.isEmpty()
                || question.isEmpty() || answer.isEmpty()) {
            showMessage("Please complete every field.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            showMessage("Item ID must be a number.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (ItemVerification item : items) {
            if (item.itemId.equalsIgnoreCase(id)) {
                showMessage("This Item ID is already registered.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        ItemVerification verification =
                new ItemVerification(id, name, question, answer);

        items.add(verification);

        if (saveData()) {
            showMessage("Verification details registered successfully.",
                    JOptionPane.INFORMATION_MESSAGE);

            itemIdField.setText("");
            itemNameField.setText("");
            questionField.setText("");
            expectedAnswerField.setText("");
        } else {
            items.remove(verification);
        }
    }

    // =========================================
    // SUBMIT CLAIM
    // =========================================

    private JPanel createClaimPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        claimItemIdField = new JTextField(20);
        claimantNameField = new JTextField(20);
        contactField = new JTextField(20);
        questionLabel = new JLabel("Enter an Item ID to load its question.");
        claimantAnswerField = new JPasswordField(20);

        JButton loadQuestionButton = new JButton("Load Question");
        JButton submitButton = new JButton("Submit Claim");

        int row = 0;

        addFormRow(panel, gbc, row++, new JLabel("Item ID:"), claimItemIdField);
        addFormRow(panel, gbc, row++, loadQuestionButton, new JLabel(""));
        addFormRow(panel, gbc, row++, new JLabel("Ownership Question:"), questionLabel);
        addFormRow(panel, gbc, row++, new JLabel("Your Name:"), claimantNameField);
        addFormRow(panel, gbc, row++, new JLabel("Contact Number:"), contactField);
        addFormRow(panel, gbc, row++, new JLabel("Your Answer:"), claimantAnswerField);
        addFormRow(panel, gbc, row, submitButton, new JLabel(""));

        loadQuestionButton.addActionListener(e -> loadQuestion());
        submitButton.addActionListener(e -> submitClaim());

        return panel;
    }

    private void loadQuestion() {

        String id = claimItemIdField.getText().trim();
        ItemVerification item = findItem(id);

        if (item == null) {
            questionLabel.setText("Item not found. Register its verification details first.");
            return;
        }

        questionLabel.setText(item.question);
    }

    private void submitClaim() {

        String id = claimItemIdField.getText().trim();
        String name = claimantNameField.getText().trim();
        String contact = contactField.getText().trim();
        String answer = new String(claimantAnswerField.getPassword()).trim();

        if (findItem(id) == null) {
            showMessage("No verification details exist for this Item ID.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty() || contact.isEmpty() || answer.isEmpty()) {
            showMessage("Please complete all claimant fields.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (ClaimRecord existing : claims) {
            if (existing.itemId.equalsIgnoreCase(id)
                    && existing.claimantName.equalsIgnoreCase(name)
                    && "PENDING".equals(existing.status)) {
                showMessage("This claimant already has a pending claim for this item.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        ClaimRecord claim = new ClaimRecord(
                id, name, contact, answer, "PENDING"
        );

        claims.add(claim);

        if (saveData()) {
            showMessage("Claim submitted successfully. Status: PENDING",
                    JOptionPane.INFORMATION_MESSAGE);

            claimantNameField.setText("");
            contactField.setText("");
            claimantAnswerField.setText("");

            refreshClaimsTable();
        } else {
            claims.remove(claim);
        }
    }

    // =========================================
    // REVIEW CLAIMS
    // =========================================

    private JPanel createReviewPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {
                "Item ID", "Claimant", "Contact",
                "Submitted Answer", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        claimsTable = new JTable(tableModel);
        claimsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(claimsTable);

        JButton approveButton = new JButton("Approve Claim");
        JButton rejectButton = new JButton("Reject Claim");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(approveButton);
        buttons.add(rejectButton);
        buttons.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> reviewSelectedClaim(true));
        rejectButton.addActionListener(e -> reviewSelectedClaim(false));
        refreshButton.addActionListener(e -> refreshClaimsTable());

        refreshClaimsTable();

        return panel;
    }

    private void refreshClaimsTable() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        for (ClaimRecord claim : claims) {
            tableModel.addRow(new Object[]{
                    claim.itemId,
                    claim.claimantName,
                    claim.contact,
                    claim.answer,
                    claim.status
            });
        }
    }

    private void reviewSelectedClaim(boolean approve) {

        int selectedRow = claimsTable.getSelectedRow();

        if (selectedRow < 0) {
            showMessage("Select a claim first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = claimsTable.convertRowIndexToModel(selectedRow);
        ClaimRecord claim = claims.get(modelRow);

        if (!"PENDING".equals(claim.status)) {
            showMessage("This claim has already been reviewed.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ItemVerification verification = findItem(claim.itemId);

        if (verification == null) {
            showMessage("Verification details for this item were not found.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reviewDetails =
                "Item: " + verification.itemName
                        + "\nClaimant: " + claim.claimantName
                        + "\nQuestion: " + verification.question
                        + "\nExpected answer: " + verification.expectedAnswer
                        + "\nSubmitted answer: " + claim.answer
                        + "\n\nConfirm " + (approve ? "approval" : "rejection") + "?";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                reviewDetails,
                "Review Ownership Claim",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        String oldClaimStatus = claim.status;
        claim.status = approve ? "APPROVED" : "REJECTED";
        if (approve) {
            try {
                int itemId = Integer.parseInt(claim.itemId);

                com.lostfound.service.ItemService itemService =
                        new com.lostfound.service.ItemService();

                java.util.List<com.lostfound.model.Item> allItems =
                        itemService.getAllItems();

                com.lostfound.model.Item selectedItem = null;

                for (com.lostfound.model.Item item : allItems) {
                    if (item.getId() == itemId) {
                        selectedItem = item;
                        break;
                    }
                }

                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Item not found. Status was not changed.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String oldStatus = selectedItem.getStatus();

                selectedItem.setStatus("Claimed");
                itemService.updateItem(selectedItem);

                claim.status = "APPROVED";

                if (!saveData()) {
                    selectedItem.setStatus(oldStatus);
                    claim.status = "PENDING";

                    JOptionPane.showMessageDialog(
                            this,
                            "Could not save claim verification data.",
                            "Storage Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                refreshClaimsTable();

                JOptionPane.showMessageDialog(
                        this,
                        "Claim approved. Item status set to Claimed."
                );

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid item ID.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        if (!saveData()) {
            claim.status = oldClaimStatus;
            return;
        }

        if (approve) {

            try {
                int itemId = Integer.parseInt(claim.itemId);
                ItemService itemService = new ItemService();

                List<Item> allItems = itemService.getAllItems();
                Item selectedItem = null;

                for (Item item : allItems) {
                    if (item.getId() == itemId) {
                        selectedItem = item;
                        break;
                    }
                }

                if (selectedItem == null) {
                    claim.status = oldClaimStatus;
                    saveData();

                    showMessage(
                            "Item not found. Claim approval was cancelled.",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String oldItemStatus = selectedItem.getStatus();
                selectedItem.setStatus("Claimed");

                try {
                    itemService.updateItem(selectedItem);
                } catch (RuntimeException ex) {
                    selectedItem.setStatus(oldItemStatus);
                    claim.status = oldClaimStatus;
                    saveData();

                    showMessage(
                            "Could not update item status:\n" + ex.getMessage(),
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                refreshClaimsTable();

                showMessage(
                        "Claim approved successfully!\n"
                                + "Claim status: APPROVED\n"
                                + "Item status: Claimed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (NumberFormatException ex) {
                claim.status = oldClaimStatus;
                saveData();

                showMessage(
                        "Invalid item ID. The claim was not approved.",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } else {
            refreshClaimsTable();

            showMessage(
                    "Claim rejected.\nItem status remains unchanged.",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // =========================================
    // HELPERS
    // =========================================

    private ItemVerification findItem(String id) {

        for (ItemVerification item : items) {
            if (item.itemId.equalsIgnoreCase(id)) {
                return item;
            }
        }

        return null;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            Component left,
            Component right) {

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        panel.add(left, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(right, gbc);
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "Lost & Found", type);
    }

    // =========================================
    // FILE STORAGE
    // =========================================

    private void loadData() {

        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(file))) {

            Object data = input.readObject();

            if (data instanceof SavedData) {
                SavedData saved = (SavedData) data;

                if (saved.items != null) {
                    items.addAll(saved.items);
                }

                if (saved.claims != null) {
                    claims.addAll(saved.claims);
                }
            }

        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not read verification data:\n" + ex.getMessage(),
                    "Storage Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean saveData() {

        File file = new File(DATA_FILE);
        File directory = file.getParentFile();

        if (directory != null && !directory.exists()
                && !directory.mkdirs()) {
            showMessage("Could not create the data directory.",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        SavedData saved = new SavedData(items, claims);

        try (ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(file))) {

            output.writeObject(saved);
            return true;

        } catch (IOException ex) {
            showMessage("Could not save verification data:\n" + ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // =========================================
    // SERIALIZABLE DATA CLASSES
    // =========================================

    private static class ItemVerification implements Serializable {

        private static final long serialVersionUID = 1L;

        String itemId;
        String itemName;
        String question;
        String expectedAnswer;

        ItemVerification(
                String itemId,
                String itemName,
                String question,
                String expectedAnswer) {

            this.itemId = itemId;
            this.itemName = itemName;
            this.question = question;
            this.expectedAnswer = expectedAnswer;
        }
    }

    private static class ClaimRecord implements Serializable {

        private static final long serialVersionUID = 1L;

        String itemId;
        String claimantName;
        String contact;
        String answer;
        String status;

        ClaimRecord(
                String itemId,
                String claimantName,
                String contact,
                String answer,
                String status) {

            this.itemId = itemId;
            this.claimantName = claimantName;
            this.contact = contact;
            this.answer = answer;
            this.status = status;
        }
    }

    private static class SavedData implements Serializable {

        private static final long serialVersionUID = 1L;

        List<ItemVerification> items;
        List<ClaimRecord> claims;

        SavedData(
                List<ItemVerification> items,
                List<ClaimRecord> claims) {

            this.items = new ArrayList<>(items);
            this.claims = new ArrayList<>(claims);
        }
    }
}