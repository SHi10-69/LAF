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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Date;

public class AddItemPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ItemService itemService;

    private JTextField itemNameField;
    private JComboBox<String> categoryBox;
    private JComboBox<String> typeBox;
    private JTextField locationField;
    private JTextField ownerField;
    private JTextField contactField;
    private JTextArea descriptionArea;
    private JTextField accessoriesField;
    private JSpinner quantitySpinner;
    private JComboBox<String> urgencyBox;

    // Optional Item Photo
    private File selectedImageFile = null;
    private JLabel imagePreviewLabel;
    private JLabel imageFileNameLabel;
    private JButton chooseImageButton;
    private JButton removeImageButton;

    private JPanel formCard;
    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JButton submitButton;
    private JButton clearButton;

    public AddItemPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.itemService = new ItemService();

        setLayout(new BorderLayout(15, 15));
        Theme.stylePanel(this);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        createHeading();
        createForm();

        Theme.addThemeChangeListener(this);
    }

    private void createHeading() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(Theme.getBackground());

        headingLabel = new JLabel("REPORT LOST OR FOUND BELONGING");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Submit accurate details of lost or found belongings to accelerate automated precision matching."
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
        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        itemNameField = new JTextField(20);
        categoryBox = new JComboBox<>(new String[]{
                "Electronics", "Books & Stationery", "ID Card & Badges",
                "Wallet & Purses", "Keys", "Clothing & Apparel", "Bags & Backpacks", "Other"
        });

        typeBox = new JComboBox<>(new String[]{"LOST", "FOUND"});
        locationField = new JTextField(20);
        ownerField = new JTextField(20);
        ownerField.setText(loggedInUser != null ? loggedInUser.getFullName() : "");
        contactField = new JTextField(20);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        accessoriesField = new JTextField(20);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        urgencyBox = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});

        // Photo Upload Section (Optional)
        JPanel photoSection = new JPanel(new BorderLayout(10, 8));
        photoSection.setBackground(Theme.getCard());
        photoSection.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(Theme.getBackground());
        previewBox.setPreferredSize(new Dimension(100, 75));
        previewBox.setBorder(new LineBorder(Theme.getBorder(), 1, true));

        imagePreviewLabel = new JLabel("No Photo", SwingConstants.CENTER);
        imagePreviewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        imagePreviewLabel.setForeground(Theme.getTextMuted());
        previewBox.add(imagePreviewLabel, BorderLayout.CENTER);

        JPanel photoDetails = new JPanel();
        photoDetails.setLayout(new BoxLayout(photoDetails, BoxLayout.Y_AXIS));
        photoDetails.setBackground(Theme.getCard());

        imageFileNameLabel = new JLabel("Optional: Attach clear photo of the item");
        imageFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        imageFileNameLabel.setForeground(Theme.getTextMuted());

        JPanel photoButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        photoButtonRow.setBackground(Theme.getCard());

        chooseImageButton = new JButton("📷 Choose Photo...");
        removeImageButton = new JButton("✖ Remove");
        removeImageButton.setEnabled(false);

        Theme.styleSecondaryButton(chooseImageButton);
        Theme.styleDangerButton(removeImageButton);

        chooseImageButton.addActionListener(e -> choosePhoto());
        removeImageButton.addActionListener(e -> clearPhoto());

        photoButtonRow.add(chooseImageButton);
        photoButtonRow.add(removeImageButton);

        photoDetails.add(imageFileNameLabel);
        photoDetails.add(Box.createVerticalStrut(4));
        photoDetails.add(photoButtonRow);

        photoSection.add(previewBox, BorderLayout.WEST);
        photoSection.add(photoDetails, BorderLayout.CENTER);

        // Add form rows
        int row = 0;
        addFormRow(formCard, gbc, row++, "Item Name *:", itemNameField);
        addFormRow(formCard, gbc, row++, "Category *:", categoryBox);
        addFormRow(formCard, gbc, row++, "Report Type *:", typeBox);
        addFormRow(formCard, gbc, row++, "Location *:", locationField);
        addFormRow(formCard, gbc, row++, "Reporter / Owner Name *:", ownerField);
        addFormRow(formCard, gbc, row++, "Contact Number *:", contactField);
        addFormRow(formCard, gbc, row++, "Detailed Description:", descScroll);
        addFormRow(formCard, gbc, row++, "Accessories / Contents:", accessoriesField);
        addFormRow(formCard, gbc, row++, "Quantity:", quantitySpinner);
        addFormRow(formCard, gbc, row++, "Priority / Urgency:", urgencyBox);
        addFormRow(formCard, gbc, row++, "Item Photo (Optional):", photoSection);

        // Action Buttons
        submitButton = new JButton("✓ Submit Report");
        clearButton = new JButton("Reset Form");

        submitButton.addActionListener(e -> submitItem());
        clearButton.addActionListener(e -> clearForm());

        Theme.stylePrimaryButton(submitButton);
        Theme.styleSecondaryButton(clearButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        buttonPanel.setBackground(Theme.getCard());
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formCard.add(buttonPanel, gbc);

        applyFieldStyling();

        JScrollPane cardScroll = new JScrollPane(formCard);
        Theme.styleScrollPane(cardScroll);
        cardScroll.setBorder(null);
        add(cardScroll, BorderLayout.CENTER);
    }

    private void applyFieldStyling() {
        Theme.styleTextField(itemNameField);
        Theme.styleTextField(locationField);
        Theme.styleTextField(ownerField);
        Theme.styleTextField(contactField);
        Theme.styleTextField(accessoriesField);
        Theme.styleComboBox(categoryBox);
        Theme.styleComboBox(typeBox);
        Theme.styleComboBox(urgencyBox);
        descriptionArea.setBackground(Theme.getCard());
        descriptionArea.setForeground(Theme.getTextPrimary());
        descriptionArea.setCaretColor(Theme.getPrimaryAccent());
        descriptionArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

    private void choosePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Item Photo");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.webp, *.gif, *.bmp)",
                "jpg", "jpeg", "png", "webp", "gif", "bmp"
        ));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            if (selectedImageFile != null && selectedImageFile.exists()) {
                ImageIcon thumbnail = ImageUtil.loadScaledIcon(selectedImageFile.getAbsolutePath(), 100, 75);
                if (thumbnail != null) {
                    imagePreviewLabel.setIcon(thumbnail);
                    imagePreviewLabel.setText("");
                } else {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Selected");
                }
                long sizeKb = selectedImageFile.length() / 1024;
                imageFileNameLabel.setText(selectedImageFile.getName() + " (" + sizeKb + " KB)");
                imageFileNameLabel.setForeground(Theme.getPrimaryAccent());
                removeImageButton.setEnabled(true);
            }
        }
    }

    private void clearPhoto() {
        selectedImageFile = null;
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("No Photo");
        imageFileNameLabel.setText("Optional: Attach clear photo of the item");
        imageFileNameLabel.setForeground(Theme.getTextMuted());
        removeImageButton.setEnabled(false);
    }

    private void submitItem() {
        String itemName = itemNameField.getText().trim();
        String location = locationField.getText().trim();
        String owner = ownerField.getText().trim();
        String contact = contactField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (itemName.isEmpty() || location.isEmpty() || owner.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Item Name, Location, Name, Contact).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Item item = new Item();
        item.setReportedByUsername(loggedInUser != null ? loggedInUser.getUsername() : "anonymous");
        item.setItemName(itemName);
        item.setCategory((String) categoryBox.getSelectedItem());
        item.setItemType((String) typeBox.getSelectedItem());
        item.setLocation(location);
        item.setOwnerName(owner);
        item.setContact(contact);
        item.setDescription(description);
        item.setAccessories(accessoriesField.getText().trim());
        item.setQuantity((Integer) quantitySpinner.getValue());

        String urgency = (String) urgencyBox.getSelectedItem();
        int urgencyValue = "HIGH".equals(urgency) ? 3 : ("MEDIUM".equals(urgency) ? 2 : 1);
        item.setUrgency(urgencyValue);
        item.setStatus("PENDING");
        item.setItemDate(new Date());

        // Process item photo
        if (selectedImageFile != null && selectedImageFile.exists()) {
            String savedPath = ImageUtil.saveItemImage(selectedImageFile);
            item.setImagePath(savedPath);
        } else {
            item.setImagePath("");
        }

        try {
            itemService.addItem(item);
            JOptionPane.showMessageDialog(this, "Item reported successfully! It is now active for discovery and matching.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        itemNameField.setText("");
        locationField.setText("");
        ownerField.setText(loggedInUser != null ? loggedInUser.getFullName() : "");
        contactField.setText("");
        descriptionArea.setText("");
        accessoriesField.setText("");
        categoryBox.setSelectedIndex(0);
        typeBox.setSelectedIndex(0);
        urgencyBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        clearPhoto();
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
        Theme.styleSecondaryButton(chooseImageButton);
        Theme.styleDangerButton(removeImageButton);
        repaint();
    }
}
