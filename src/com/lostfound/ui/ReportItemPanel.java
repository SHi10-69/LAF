package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;

public class ReportItemPanel extends JPanel {

    private JTextField itemNameField;
    private JComboBox<String> categoryComboBox;

    private JRadioButton lostRadioButton;
    private JRadioButton foundRadioButton;

    private JTextField locationField;

    private JTextField ownerNameField;
    private JTextField contactField;

    private JSpinner quantitySpinner;

    private JTextArea descriptionArea;

    private JCheckBox chargerCheckBox;
    private JCheckBox earphonesCheckBox;
    private JCheckBox coverCheckBox;
    private JCheckBox idCardCheckBox;

    private JSlider urgencySlider;

    private JProgressBar progressBar;

    private JLabel imagePathLabel;

    private JButton chooseImageButton;
    private JButton saveButton;
    private JButton clearButton;

    private File selectedImage;

    private final ItemService itemService;

    public ReportItemPanel() {

        itemService = new ItemService();

        createGUI();
    }

    private void createGUI() {

        setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        // =====================================
        // TITLE
        // =====================================

        JLabel titleLabel =
                new JLabel(
                        "REPORT LOST / FOUND ITEM",
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
        // FORM PANEL
        // =====================================

        JPanel formPanel =
                new JPanel(
                        new GridBagLayout()
                );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        6,
                        6,
                        6,
                        6
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1;


        // =====================================
        // ITEM NAME
        // =====================================

        addField(
                formPanel,
                gbc,
                0,
                "Item Name:",
                itemNameField =
                        new JTextField()
        );


        // =====================================
        // CATEGORY
        // =====================================

        categoryComboBox =
                new JComboBox<>(
                        new String[]{
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

        addField(
                formPanel,
                gbc,
                1,
                "Category:",
                categoryComboBox
        );


        // =====================================
        // ITEM TYPE
        // =====================================

        JPanel typePanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        lostRadioButton =
                new JRadioButton(
                        "Lost",
                        true
                );

        foundRadioButton =
                new JRadioButton(
                        "Found"
                );

        ButtonGroup typeGroup =
                new ButtonGroup();

        typeGroup.add(
                lostRadioButton
        );

        typeGroup.add(
                foundRadioButton
        );

        typePanel.add(
                lostRadioButton
        );

        typePanel.add(
                foundRadioButton
        );

        addField(
                formPanel,
                gbc,
                2,
                "Item Type:",
                typePanel
        );


        // =====================================
        // LOCATION
        // =====================================

        addField(
                formPanel,
                gbc,
                3,
                "Location:",
                locationField =
                        new JTextField()
        );


        // =====================================
        // OWNER
        // =====================================

        addField(
                formPanel,
                gbc,
                4,
                "Owner Name:",
                ownerNameField =
                        new JTextField()
        );


        // =====================================
        // CONTACT
        // =====================================

        addField(
                formPanel,
                gbc,
                5,
                "Contact:",
                contactField =
                        new JTextField()
        );


        // =====================================
        // QUANTITY
        // =====================================

        quantitySpinner =
                new JSpinner(
                        new SpinnerNumberModel(
                                1,
                                1,
                                100,
                                1
                        )
                );

        addField(
                formPanel,
                gbc,
                6,
                "Quantity:",
                quantitySpinner
        );


        // =====================================
        // ACCESSORIES
        // =====================================

        JPanel accessoriesPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        chargerCheckBox =
                new JCheckBox(
                        "Charger"
                );

        earphonesCheckBox =
                new JCheckBox(
                        "Earphones"
                );

        coverCheckBox =
                new JCheckBox(
                        "Cover"
                );

        idCardCheckBox =
                new JCheckBox(
                        "ID Card"
                );

        accessoriesPanel.add(
                chargerCheckBox
        );

        accessoriesPanel.add(
                earphonesCheckBox
        );

        accessoriesPanel.add(
                coverCheckBox
        );

        accessoriesPanel.add(
                idCardCheckBox
        );

        addField(
                formPanel,
                gbc,
                7,
                "Accessories:",
                accessoriesPanel
        );


        // =====================================
        // URGENCY SLIDER
        // =====================================

        urgencySlider =
                new JSlider(
                        0,
                        100,
                        50
                );

        urgencySlider.setMajorTickSpacing(
                25
        );

        urgencySlider.setMinorTickSpacing(
                5
        );

        urgencySlider.setPaintTicks(
                true
        );

        urgencySlider.setPaintLabels(
                true
        );

        addField(
                formPanel,
                gbc,
                8,
                "Urgency:",
                urgencySlider
        );


        // =====================================
        // DESCRIPTION
        // =====================================

        descriptionArea =
                new JTextArea(
                        5,
                        30
                );

        descriptionArea.setLineWrap(
                true
        );

        descriptionArea.setWrapStyleWord(
                true
        );

        JScrollPane descriptionScrollPane =
                new JScrollPane(
                        descriptionArea
                );

        addField(
                formPanel,
                gbc,
                9,
                "Description:",
                descriptionScrollPane
        );


        // =====================================
        // IMAGE
        // =====================================

        JPanel imagePanel =
                new JPanel(
                        new BorderLayout(
                                5,
                                5
                        )
                );

        imagePathLabel =
                new JLabel(
                        "No image selected"
                );

        chooseImageButton =
                new JButton(
                        "Choose Image"
                );

        imagePanel.add(
                imagePathLabel,
                BorderLayout.CENTER
        );

        imagePanel.add(
                chooseImageButton,
                BorderLayout.EAST
        );

        addField(
                formPanel,
                gbc,
                10,
                "Image:",
                imagePanel
        );


        // =====================================
        // PROGRESS BAR
        // =====================================

        progressBar =
                new JProgressBar(
                        0,
                        100
                );

        progressBar.setValue(0);

        progressBar.setStringPainted(
                true
        );

        addField(
                formPanel,
                gbc,
                11,
                "Form Progress:",
                progressBar
        );


        // =====================================
        // SCROLL PANE
        // =====================================

        JScrollPane formScrollPane =
                new JScrollPane(
                        formPanel
                );

        add(
                formScrollPane,
                BorderLayout.CENTER
        );


        // =====================================
        // BUTTON PANEL
        // =====================================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        saveButton =
                new JButton(
                        "SAVE ITEM"
                );

        clearButton =
                new JButton(
                        "CLEAR"
                );

        buttonPanel.add(
                saveButton
        );

        buttonPanel.add(
                clearButton
        );

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        // =====================================
        // EVENTS
        // =====================================

        chooseImageButton
                .addActionListener(
                        e -> chooseImage()
                );

        saveButton
                .addActionListener(
                        e -> saveItem()
                );

        clearButton
                .addActionListener(
                        e -> clearForm()
                );


        // Update progress
        // when the user changes fields.

        urgencySlider
                .addChangeListener(
                        e -> updateProgress()
                );
    }


    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;

        panel.add(
                new JLabel(labelText),
                gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 0.8;

        panel.add(
                component,
                gbc
        );
    }


    private void chooseImage() {

        JFileChooser fileChooser =
                new JFileChooser();

        int result =
                fileChooser.showOpenDialog(
                        this
                );

        if (
                result ==
                        JFileChooser.APPROVE_OPTION
        ) {

            selectedImage =
                    fileChooser.getSelectedFile();

            imagePathLabel.setText(
                    selectedImage.getAbsolutePath()
            );
        }
    }


    private void saveItem() {

        if (
                itemNameField
                        .getText()
                        .trim()
                        .isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter the item name.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        if (
                locationField
                        .getText()
                        .trim()
                        .isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter the location.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        // =====================================
        // CREATE ITEM OBJECT
        // =====================================

        Item item =
                new Item();


        item.setItemName(
                itemNameField
                        .getText()
                        .trim()
        );


        item.setCategory(
                (String)
                        categoryComboBox
                                .getSelectedItem()
        );


        if (
                lostRadioButton
                        .isSelected()
        ) {

            item.setItemType(
                    "Lost"
            );

        } else {

            item.setItemType(
                    "Found"
            );
        }


        item.setLocation(
                locationField
                        .getText()
                        .trim()
        );


        item.setOwnerName(
                ownerNameField
                        .getText()
                        .trim()
        );


        item.setContact(
                contactField
                        .getText()
                        .trim()
        );


        item.setQuantity(
                (Integer)
                        quantitySpinner
                                .getValue()
        );


        item.setDescription(
                descriptionArea
                        .getText()
                        .trim()
        );


        item.setUrgency(
                urgencySlider
                        .getValue()
        );


        item.setItemDate(
                new Date()
        );


        item.setStatus(
                "Reported"
        );


        if (selectedImage != null) {

            item.setImagePath(
                    selectedImage
                            .getAbsolutePath()
            );
        }


        item.setAccessories(
                getSelectedAccessories()
        );


        // =====================================
        // SAVE
        // =====================================

        itemService.addItem(item);


        progressBar.setValue(100);


        JOptionPane.showMessageDialog(
                this,
                "Item saved successfully!\n"
                        + "Item ID: "
                        + item.getId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );


        clearForm();
    }


    private String getSelectedAccessories() {

        StringBuilder accessories =
                new StringBuilder();


        if (
                chargerCheckBox
                        .isSelected()
        ) {

            accessories.append(
                    "Charger, "
            );
        }


        if (
                earphonesCheckBox
                        .isSelected()
        ) {

            accessories.append(
                    "Earphones, "
            );
        }


        if (
                coverCheckBox
                        .isSelected()
        ) {

            accessories.append(
                    "Cover, "
            );
        }


        if (
                idCardCheckBox
                        .isSelected()
        ) {

            accessories.append(
                    "ID Card, "
            );
        }


        return accessories.toString();
    }


    private void clearForm() {

        itemNameField.setText("");

        categoryComboBox
                .setSelectedIndex(0);

        lostRadioButton
                .setSelected(true);

        locationField.setText("");

        ownerNameField.setText("");

        contactField.setText("");

        quantitySpinner
                .setValue(1);

        descriptionArea.setText("");

        chargerCheckBox
                .setSelected(false);

        earphonesCheckBox
                .setSelected(false);

        coverCheckBox
                .setSelected(false);

        idCardCheckBox
                .setSelected(false);

        urgencySlider.setValue(50);

        progressBar.setValue(0);

        selectedImage = null;

        imagePathLabel.setText(
                "No image selected"
        );
    }


    private void updateProgress() {

        int progress = 0;

        if (!itemNameField.getText().trim().isEmpty()) {
            progress += 15;
        }

        if (!locationField.getText().trim().isEmpty()) {
            progress += 15;
        }

        if (!ownerNameField.getText().trim().isEmpty()) {
            progress += 10;
        }

        if (!contactField.getText().trim().isEmpty()) {
            progress += 10;
        }

        if (!descriptionArea.getText().trim().isEmpty()) {
            progress += 20;
        }

        if (selectedImage != null) {
            progress += 15;
        }

        progress += 15;

        progressBar.setValue(
                Math.min(progress, 100)
        );
    }
}