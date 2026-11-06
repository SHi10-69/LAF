package com.lostfound.ui;

import com.lostfound.model.User;
import com.lostfound.service.UserService;
import com.lostfound.util.Theme;
import com.lostfound.web.WebServer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame implements Theme.ThemeChangeListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;

    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;

    private JLabel brandBadge;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JPanel mainCard;
    private JButton nvidiaThemeBtn;
    private JButton netflixThemeBtn;
    private JButton webAppBtn;
    private JPanel themeBar;

    private UserService userService;

    public LoginFrame() {
        userService = new UserService();

        setTitle("Lost & Found — Sign In");
        setSize(440, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        JPanel outerPanel = new JPanel(new BorderLayout(0, 10));
        outerPanel.setBackground(Theme.getBackground());

        // --- Top Bar: Theme Switcher & Web Portal ---
        themeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        themeBar.setBackground(Theme.getBackground());

        JLabel themeLabel = new JLabel("Theme: ");
        themeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        themeLabel.setForeground(Theme.getTextMuted());

        nvidiaThemeBtn = new JButton("🟢 NVIDIA");
        netflixThemeBtn = new JButton("🔴 Netflix");
        webAppBtn = new JButton("🌐 Web App");

        nvidiaThemeBtn.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NVIDIA));
        netflixThemeBtn.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NETFLIX));
        webAppBtn.addActionListener(e -> WebServer.openInBrowser());

        Theme.styleTopBarButton(nvidiaThemeBtn, Theme.isNvidia());
        Theme.styleTopBarButton(netflixThemeBtn, Theme.isNetflix());
        Theme.styleTopBarButton(webAppBtn, false);

        themeBar.add(themeLabel);
        themeBar.add(nvidiaThemeBtn);
        themeBar.add(netflixThemeBtn);
        themeBar.add(webAppBtn);
        outerPanel.add(themeBar, BorderLayout.NORTH);

        // --- Main Login Card ---
        mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setPreferredSize(new Dimension(380, 410));
        Theme.styleCard(mainCard);
        mainCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(18, 26, 18, 26)
        ));

        // Brand Badge & Header
        brandBadge = new JLabel(Theme.isNvidia() ? "⚡ GEFORCE RTX PLATFORM" : "🍿 CINEMATIC ENTERPRISE");
        brandBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandBadge.setForeground(Theme.getPrimaryAccent());
        brandBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLabel = new JLabel("LOST & FOUND");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Theme.getTextPrimary());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Authenticate with your credentials");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Theme.getTextMuted());
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainCard.add(brandBadge);
        mainCard.add(Box.createVerticalStrut(3));
        mainCard.add(titleLabel);
        mainCard.add(Box.createVerticalStrut(2));
        mainCard.add(subtitleLabel);
        mainCard.add(Box.createVerticalStrut(16));

        // Form Fields
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        usernameLabel.setForeground(Theme.getTextMuted());
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField(18);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.styleTextField(usernameField);

        passwordLabel = new JLabel("Security Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passwordLabel.setForeground(Theme.getTextMuted());
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField(18);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.stylePasswordField(passwordField);

        showPasswordCheckBox = new JCheckBox("Show Password Characters");
        showPasswordCheckBox.setBackground(Theme.getCard());
        showPasswordCheckBox.setForeground(Theme.getTextMuted());
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        mainCard.add(usernameLabel);
        mainCard.add(Box.createVerticalStrut(4));
        mainCard.add(usernameField);
        mainCard.add(Box.createVerticalStrut(10));

        mainCard.add(passwordLabel);
        mainCard.add(Box.createVerticalStrut(4));
        mainCard.add(passwordField);
        mainCard.add(Box.createVerticalStrut(6));
        mainCard.add(showPasswordCheckBox);
        mainCard.add(Box.createVerticalStrut(16));

        // Primary Action (Login)
        loginButton = new JButton("SIGN IN TO DASHBOARD");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        Theme.stylePrimaryButton(loginButton);
        mainCard.add(loginButton);
        mainCard.add(Box.createVerticalStrut(8));

        // Secondary Actions Row (Register & Exit)
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonRow.setBackground(Theme.getCard());
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        registerButton = new JButton("Create Account");
        exitButton = new JButton("Exit");
        Theme.styleSecondaryButton(registerButton);
        Theme.styleDangerButton(exitButton);

        buttonRow.add(registerButton);
        buttonRow.add(exitButton);
        mainCard.add(buttonRow);

        outerPanel.add(mainCard, BorderLayout.CENTER);
        add(outerPanel);

        // Actions
        loginButton.addActionListener(e -> loginUser());
        passwordField.addActionListener(e -> loginUser());
        usernameField.addActionListener(e -> loginUser());

        registerButton.addActionListener(e -> new RegisterFrame());

        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit the application?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    private void applyThemeStyling() {
        getContentPane().setBackground(Theme.getBackground());
        mainCard.setBackground(Theme.getCard());
        mainCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(18, 26, 18, 26)
        ));

        brandBadge.setText(Theme.isNvidia() ? "⚡ GEFORCE RTX PLATFORM" : "🍿 CINEMATIC ENTERPRISE");
        brandBadge.setForeground(Theme.getPrimaryAccent());

        titleLabel.setForeground(Theme.getTextPrimary());
        subtitleLabel.setForeground(Theme.getTextMuted());
        usernameLabel.setForeground(Theme.getTextMuted());
        passwordLabel.setForeground(Theme.getTextMuted());
        showPasswordCheckBox.setBackground(Theme.getCard());
        showPasswordCheckBox.setForeground(Theme.getTextMuted());

        Theme.styleTextField(usernameField);
        Theme.stylePasswordField(passwordField);
        Theme.stylePrimaryButton(loginButton);
        Theme.styleSecondaryButton(registerButton);
        Theme.styleDangerButton(exitButton);

        Theme.styleTopBarButton(nvidiaThemeBtn, Theme.isNvidia());
        Theme.styleTopBarButton(netflixThemeBtn, Theme.isNetflix());
        Theme.styleTopBarButton(webAppBtn, false);

        themeBar.setBackground(Theme.getBackground());
        repaint();
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        applyThemeStyling();
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userService.login(username, password);

        if (user != null) {
            Theme.removeThemeChangeListener(this);
            new DashboardFrame(user);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials or account has been suspended.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}
