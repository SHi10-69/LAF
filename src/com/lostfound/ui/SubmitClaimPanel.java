package com.lostfound.ui;

import com.lostfound.model.Claim;
import com.lostfound.model.Item;
import com.lostfound.model.User;
import com.lostfound.service.ClaimService;
import com.lostfound.service.ItemService;
import com.lostfound.util.ImageUtil;
import com.lostfound.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.List;

public class SubmitClaimPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ItemService itemService;
    private final ClaimService claimService;

    private JComboBox<Item> itemBox;
    private JComboBox<Item> lostItemBox;

    private JTextField nameField;
    private JTextField contactField;
    private JTextArea proofArea;

    // Image Proof Verification Components (Optional)
    private File selectedProofImage = null;
    private JLabel imagePreviewLabel;
    private JLabel imageFileNameLabel;
    private JButton browseImageButton;
    private JButton removeImageButton;

    private JPanel formCard;
    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JButton submitButton;
    private JButton clearButton;

    public SubmitClaimPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.itemService = new ItemService();
        this.claimService = new ClaimService();

        setLayout(new BorderLayout(15, 15));
        Theme.stylePanel(this);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        createHeading();
        createForm();

        Theme.addThemeChangeListener(this);
        refreshData();
    }

    private void createHeading() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(Theme.getBackground());

        headingLabel = new JLabel("SUBMIT ITEM CLAIM & VERIFICATION");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Submit a claim for a found item. Attach an optional photo proof (receipt, serial number, photo with item) to fast-track admin verification."
        );
        Theme.styleLabel(subtitleLabel);

        headerPanel.add(headingLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createForm() {
        formCard = new JPanel(new GridBagLayout());
        Theme.styleCard(formCard);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        itemBox = new JComboBox<>();
        lostItemBox = new JComboBox<>();

        nameField = new JTextField(20);
        nameField.setText(loggedInUser.getFullName());

        contactField = new JTextField(20);

        proofArea = new JTextArea(4, 20);
        proofArea.setLineWrap(true);
        proofArea.setWrapStyleWord(true);
        JScrollPane proofScroll = new JScrollPane(proofArea);

        // Verification Image Proof Panel (Optional)
        JPanel imageSection = new JPanel(new BorderLayout(10, 8));
        imageSection.setBackground(Theme.getCard());
        imageSection.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(Theme.getBackground());
        previewBox.setPreferredSize(new Dimension(110, 80));
        previewBox.setBorder(new LineBorder(Theme.getBorder(), 1, true));

        imagePreviewLabel = new JLabel("No Image", SwingConstants.CENTER);
        imagePreviewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        imagePreviewLabel.setForeground(Theme.getTextMuted());
        previewBox.add(imagePreviewLabel, BorderLayout.CENTER);

        JPanel imageDetails = new JPanel();
        imageDetails.setLayout(new BoxLayout(imageDetails, BoxLayout.Y_AXIS));
        imageDetails.setBackground(Theme.getCard());

        imageFileNameLabel = new JLabel("Optional: Attach receipt, photo proof, or serial number");
        imageFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        imageFileNameLabel.setForeground(Theme.getTextMuted());

        JPanel imageButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        imageButtonRow.setBackground(Theme.getCard());

        browseImageButton = new JButton("📷 Attach Proof Image...");
        removeImageButton = new JButton("✖ Remove");
        removeImageButton.setEnabled(false);

        Theme.styleSecondaryButton(browseImageButton);
        Theme.styleDangerButton(removeImageButton);

        browseImageButton.addActionListener(e -> chooseProofImage());
        removeImageButton.addActionListener(e -> clearProofImage());

        imageButtonRow.add(browseImageButton);
        imageButtonRow.add(removeImageButton);

        imageDetails.add(imageFileNameLabel);
        imageDetails.add(Box.createVerticalStrut(4));
        imageDetails.add(imageButtonRow);

        imageSection.add(previewBox, BorderLayout.WEST);
        imageSection.add(imageDetails, BorderLayout.CENTER);

        // Add form rows
        addFormRow(formCard, gbc, 0, "Select Found Item *:", itemBox);
        addFormRow(formCard, gbc, 1, "Your Lost Item (Link if reported):", lostItemBox);
        addFormRow(formCard, gbc, 2, "Claimant Name *:", nameField);
        addFormRow(formCard, gbc, 3, "Contact Phone / Email *:", contactField);
        addFormRow(formCard, gbc, 4, "Ownership Details / Proof *:", proofScroll);
        addFormRow(formCard, gbc, 5, "Verification Image (Optional):", imageSection);

        // Action Buttons
        submitButton = new JButton("✓ Submit Claim for Verification");
        clearButton = new JButton("Reset Form");

        submitButton.addActionListener(e -> submitClaim());
        clearButton.addActionListener(e -> clearForm());

        Theme.stylePrimaryButton(submitButton);
        Theme.styleSecondaryButton(clearButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        buttonPanel.setBackground(Theme.getCard());
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formCard.add(buttonPanel, gbc);

        applyFieldStyling();

        JScrollPane cardScroll = new JScrollPane(formCard);
        Theme.styleScrollPane(cardScroll);
        cardScroll.setBorder(null);
        add(cardScroll, BorderLayout.CENTER);
    }

    private void applyFieldStyling() {
        Theme.styleComboBox(itemBox);
        Theme.styleComboBox(lostItemBox);
        Theme.styleTextField(nameField);
        Theme.styleTextField(contactField);
        proofArea.setBackground(Theme.getCard());
        proofArea.setForeground(Theme.getTextPrimary());
        proofArea.setCaretColor(Theme.getPrimaryAccent());
        proofArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        proofArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.28;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(labelText.contains("*") ? Theme.getTextPrimary() : Theme.getTextMuted());
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.72;
        panel.add(comp, gbc);
    }

    private void chooseProofImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Verification Proof Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.webp, *.gif, *.bmp)",
                "jpg", "jpeg", "png", "webp", "gif", "bmp"
        ));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedProofImage = fileChooser.getSelectedFile();
            if (selectedProofImage != null && selectedProofImage.exists()) {
                ImageIcon thumbnail = ImageUtil.loadScaledIcon(selectedProofImage.getAbsolutePath(), 110, 80);
                if (thumbnail != null) {
                    imagePreviewLabel.setIcon(thumbnail);
                    imagePreviewLabel.setText("");
                } else {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Selected");
                }
                long sizeKb = selectedProofImage.length() / 1024;
                imageFileNameLabel.setText(selectedProofImage.getName() + " (" + sizeKb + " KB) - Ready to upload");
                imageFileNameLabel.setForeground(Theme.getPrimaryAccent());
                removeImageButton.setEnabled(true);
            }
        }
    }

    private void clearProofImage() {
        selectedProofImage = null;
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("No Image");
        imageFileNameLabel.setText("Optional: Attach receipt, photo proof, or serial number");
        imageFileNameLabel.setForeground(Theme.getTextMuted());
        removeImageButton.setEnabled(false);
    }

    public void refreshData() {
        loadFoundItems();
        loadLostItems();
    }

    private void loadFoundItems() {
        itemBox.removeAllItems();
        List<Item> items = itemService.getAllItems();
        for (Item item : items) {
            if ("FOUND".equalsIgnoreCase(item.getItemType())
                    && !"RESOLVED".equalsIgnoreCase(item.getStatus())
                    && !"CLOSED".equalsIgnoreCase(item.getStatus())) {
                itemBox.addItem(item);
            }
        }
        itemBox.setRenderer(createItemRenderer("Select an active found item to claim"));
        itemBox.setEnabled(itemBox.getItemCount() > 0);
    }

    private void loadLostItems() {
        lostItemBox.removeAllItems();
        lostItemBox.addItem(null); // Option for no linked item

        List<Item> items = itemService.getAllItems();
        for (Item item : items) {
            if ("LOST".equalsIgnoreCase(item.getItemType())
                    && loggedInUser.getUsername().equalsIgnoreCase(item.getReportedByUsername())
                    && !"RESOLVED".equalsIgnoreCase(item.getStatus())
                    && !"CLOSED".equalsIgnoreCase(item.getStatus())) {
                lostItemBox.addItem(item);
            }
        }
        lostItemBox.setRenderer(createItemRenderer("— None (No linked lost report) —"));
        lostItemBox.setSelectedIndex(0);
    }

    private DefaultListCellRenderer createItemRenderer(String emptyText) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Item) {
                    Item item = (Item) value;
                    setText("ID #" + item.getId() + " - " + item.getItemName() + " (" + item.getCategory() + " @ " + item.getLocation() + ")");
                } else {
                    setText(emptyText);
                }
                setBackground(isSelected ? Theme.getAccentDark() : Theme.getCard());
                setForeground(isSelected ? Color.WHITE : Theme.getTextPrimary());
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        };
    }

    private void submitClaim() {
        Item selectedItem = (Item) itemBox.getSelectedItem();
        Item selectedLostItem = (Item) lostItemBox.getSelectedItem();

        String claimantName = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String proof = proofArea.getText().trim();

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an available found item to claim.", "Item Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (claimantName.isEmpty() || contact.isEmpty() || proof.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all required fields (Claimant Name, Contact, and Proof Details).", "Required Fields Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prevent duplicate pending claims for the same item by the same user
        for (Claim existing : claimService.getClaimsForItem(selectedItem.getId())) {
            if (existing.getClaimantUsername() != null
                    && existing.getClaimantUsername().equalsIgnoreCase(loggedInUser.getUsername())
                    && "PENDING".equalsIgnoreCase(existing.getClaimStatus())) {
                JOptionPane.showMessageDialog(this, "You already have a pending claim for this item. Please wait for administrator verification.", "Duplicate Claim", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Claim claim = new Claim();
        claim.setItemId(selectedItem.getId());
        if (selectedLostItem != null) {
            claim.setLostItemId(selectedLostItem.getId());
        } else {
            claim.setLostItemId(0);
        }

        claim.setClaimantName(claimantName);
        claim.setClaimantUsername(loggedInUser.getUsername());
        claim.setContact(contact);
        claim.setProof(proof);
        claim.setClaimStatus("PENDING");
        claim.setClaimDate(new Date());

        // Process Optional Verification Image
        if (selectedProofImage != null && selectedProofImage.exists()) {
            String savedImagePath = ImageUtil.saveProofImage(selectedProofImage);
            claim.setImagePath(savedImagePath);
        } else {
            claim.setImagePath(null);
        }

        claimService.addClaim(claim);

        String successMsg = "Your claim has been submitted successfully!\n"
                + (claim.getImagePath() != null
                ? "✓ Attached verification image proof was securely uploaded.\n"
                : "")
                + "An administrator will review your ownership details and verification image.";

        JOptionPane.showMessageDialog(this, successMsg, "Claim Submitted", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
        refreshData();
    }

    private void clearForm() {
        nameField.setText(loggedInUser.getFullName());
        contactField.setText("");
        proofArea.setText("");
        clearProofImage();

        if (itemBox.getItemCount() > 0) {
            itemBox.setSelectedIndex(0);
        }
        lostItemBox.setSelectedIndex(0);
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        Theme.stylePanel(this);
        Theme.styleCard(formCard);
        Theme.styleBrandHeading(headingLabel);
        Theme.styleLabel(subtitleLabel);
        applyFieldStyling();
        Theme.stylePrimaryButton(submitButton);
        Theme.styleSecondaryButton(clearButton);
        Theme.styleSecondaryButton(browseImageButton);
        Theme.styleDangerButton(removeImageButton);
        repaint();
    }
}
