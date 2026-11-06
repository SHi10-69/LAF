package com.lostfound.ui;

import com.lostfound.service.UserService;
import com.lostfound.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RegisterFrame extends JFrame implements Theme.ThemeChangeListener {

    private JTextField nameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JPanel card;
    private JButton registerButton;
    private JButton clearButton;

    private final UserService userService;

    public RegisterFrame() {
        userService = new UserService();

        setTitle("Lost & Found System - Create Account");
        setSize(440, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createGUI();
        Theme.addThemeChangeListener(this);
        applyThemeStyling();

        setVisible(true);
    }

    private void createGUI() {
        getContentPane().setBackground(Theme.getBackground());
        getContentPane().setLayout(new GridBagLayout());

        card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 450));
        Theme.styleCard(card);

        // Header
        titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Theme.getPrimaryAccent());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Create Your System Account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.getTextMuted());
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(14));

        // Form Fields
        nameField = new JTextField(18);
        usernameField = new JTextField(18);
        emailField = new JTextField(18);
        passwordField = new JPasswordField(18);
        confirmPasswordField = new JPasswordField(18);

        Theme.styleTextField(nameField);
        Theme.styleTextField(usernameField);
        Theme.styleTextField(emailField);
        Theme.stylePasswordField(passwordField);
        Theme.stylePasswordField(confirmPasswordField);

        addField(card, "Full Name *", nameField);
        addField(card, "Username *", usernameField);
        addField(card, "Email Address *", emailField);
        addField(card, "Password (min 6 characters) *", passwordField);
        addField(card, "Confirm Password *", confirmPasswordField);

        card.add(Box.createVerticalStrut(12));

        // Buttons
        registerButton = new JButton("CREATE NEW ACCOUNT");
        Theme.stylePrimaryButton(registerButton);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        clearButton = new JButton("Reset Form");
        Theme.styleSecondaryButton(clearButton);
        clearButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(registerButton);
        card.add(Box.createVerticalStrut(6));
        card.add(clearButton);

        registerButton.addActionListener(e -> register());
        clearButton.addActionListener(e -> clearForm());

        add(card);
    }

    private void addField(JPanel parent, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(Theme.getTextMuted());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        parent.add(label);
        parent.add(Box.createVerticalStrut(3));
        parent.add(field);
        parent.add(Box.createVerticalStrut(6));
    }

    private void applyThemeStyling() {
        getContentPane().setBackground(Theme.getBackground());
        card.setBackground(Theme.getCard());
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(18, 26, 18, 26)
        ));

        titleLabel.setForeground(Theme.getPrimaryAccent());
        subtitleLabel.setForeground(Theme.getTextMuted());

        Theme.styleTextField(nameField);
        Theme.styleTextField(usernameField);
        Theme.styleTextField(emailField);
        Theme.stylePasswordField(passwordField);
        Theme.stylePasswordField(confirmPasswordField);

        Theme.stylePrimaryButton(registerButton);
        Theme.styleSecondaryButton(clearButton);
        repaint();
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        applyThemeStyling();
    }

    private void register() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = userService.registerUser(name, username, email, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Account Created", JOptionPane.INFORMATION_MESSAGE);
            Theme.removeThemeChangeListener(this);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username or email is already registered.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    @Override
    public void dispose() {
        Theme.removeThemeChangeListener(this);
        super.dispose();
    }
}
