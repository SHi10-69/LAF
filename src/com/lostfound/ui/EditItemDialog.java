package com.lostfound.ui;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;
import com.lostfound.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class EditItemDialog extends JDialog {

    private final Item item;
    private final ItemService itemService;

    private JTextField nameField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> typeComboBox;
    private JTextField locationField;
    private JTextField ownerField;
    private JTextField contactField;
    private JSpinner quantitySpinner;
    private JTextArea descriptionArea;
    private JSlider urgencySlider;
    private JComboBox<String> statusComboBox;

    public EditItemDialog(Window parent, Item item, ItemService itemService) {
        super(parent, "Edit Item #" + (item != null ? item.getId() : ""), ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.itemService = itemService;

        setSize(580, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.getBackground());

        createGUI();
        loadItemData();
    }

    private void createGUI() {
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.getCard());
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel("EDIT ITEM RECORD #" + (item != null ? item.getId() : ""));
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.getPrimaryAccent());
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        Theme.styleCard(form);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        Theme.styleTextField(nameField);

        categoryComboBox = new JComboBox<>(new String[]{
                "Electronics", "Books & Stationery", "ID Card & Badges",
                "Wallet & Purses", "Keys", "Clothing & Apparel", "Bags & Backpacks", "Other"
        });
        Theme.styleComboBox(categoryComboBox);

        typeComboBox = new JComboBox<>(new String[]{"LOST", "FOUND"});
        Theme.styleComboBox(typeComboBox);

        locationField = new JTextField(20);
        Theme.styleTextField(locationField);

        ownerField = new JTextField(20);
        Theme.styleTextField(ownerField);

        contactField = new JTextField(20);
        Theme.styleTextField(contactField);

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        statusComboBox = new JComboBox<>(new String[]{"PENDING", "APPROVED", "REJECTED", "RESOLVED"});
        Theme.styleComboBox(statusComboBox);

        urgencySlider = new JSlider(1, 3, 1);
        urgencySlider.setBackground(Theme.getCard());
        urgencySlider.setForeground(Theme.getTextPrimary());
        urgencySlider.setMajorTickSpacing(1);
        urgencySlider.setPaintTicks(true);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(Theme.getCard());
        descriptionArea.setForeground(Theme.getTextPrimary());
        descriptionArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        Theme.styleScrollPane(descScroll);

        int row = 0;
        addField(form, gbc, row++, "Item Name *:", nameField);
        addField(form, gbc, row++, "Category *:", categoryComboBox);
        addField(form, gbc, row++, "Report Type *:", typeComboBox);
        addField(form, gbc, row++, "Location *:", locationField);
        addField(form, gbc, row++, "Reporter / Owner *:", ownerField);
        addField(form, gbc, row++, "Contact *:", contactField);
        addField(form, gbc, row++, "Quantity:", quantitySpinner);
        addField(form, gbc, row++, "Status:", statusComboBox);
        addField(form, gbc, row++, "Priority (1-3):", urgencySlider);
        addField(form, gbc, row++, "Description:", descScroll);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        Theme.styleScrollPane(formScroll);
        add(formScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Theme.getCard());

        JButton saveBtn = new JButton("✓ Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        Theme.stylePrimaryButton(saveBtn);
        Theme.styleSecondaryButton(cancelBtn);

        saveBtn.addActionListener(e -> saveChanges());
        cancelBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.getTextPrimary());
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void loadItemData() {
        if (item == null) return;
        nameField.setText(item.getItemName() != null ? item.getItemName() : "");
        categoryComboBox.setSelectedItem(item.getCategory());
        typeComboBox.setSelectedItem(item.getItemType());
        locationField.setText(item.getLocation() != null ? item.getLocation() : "");
        ownerField.setText(item.getOwnerName() != null ? item.getOwnerName() : "");
        contactField.setText(item.getContact() != null ? item.getContact() : "");
        quantitySpinner.setValue(Math.max(1, item.getQuantity()));
        statusComboBox.setSelectedItem(item.getStatus() != null ? item.getStatus() : "PENDING");
        urgencySlider.setValue(Math.max(1, Math.min(3, item.getUrgency())));
        descriptionArea.setText(item.getDescription() != null ? item.getDescription() : "");
    }

    private void saveChanges() {
        String name = nameField.getText().trim();
        String loc = locationField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || loc.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name, Location, and Contact cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        item.setItemName(name);
        item.setCategory((String) categoryComboBox.getSelectedItem());
        item.setItemType((String) typeComboBox.getSelectedItem());
        item.setLocation(loc);
        item.setOwnerName(ownerField.getText().trim());
        item.setContact(contact);
        item.setQuantity((Integer) quantitySpinner.getValue());
        item.setStatus((String) statusComboBox.getSelectedItem());
        item.setUrgency(urgencySlider.getValue());
        item.setDescription(descriptionArea.getText().trim());

        itemService.updateItem(item);
        JOptionPane.showMessageDialog(this, "Item #" + item.getId() + " updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
