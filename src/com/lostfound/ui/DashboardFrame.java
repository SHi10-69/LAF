package com.lostfound.ui;

import com.lostfound.model.User;
import com.lostfound.service.ClaimService;
import com.lostfound.service.ItemService;
import com.lostfound.service.UserService;
import com.lostfound.util.Theme;
import com.lostfound.web.WebServer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame implements Theme.ThemeChangeListener {

    private final User loggedInUser;

    private final ItemService itemService;
    private final UserService userService;
    private final ClaimService claimService;

    private JLabel totalItemsLabel;
    private JLabel lostItemsLabel;
    private JLabel foundItemsLabel;
    private JLabel totalUsersLabel;
    private JLabel pendingClaimsLabel;
    private JLabel approvedClaimsLabel;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton selectedNavButton;

    // References to child panels for dynamic reload
    private AddItemPanel addItemPanel;
    private BrowseItemsPanel browseItemsPanel;
    private MatchesPanel matchesPanel;
    private SubmitClaimPanel submitClaimPanel;
    private MyClaimsPanel myClaimsPanel;
    private SettingsPanel settingsPanel;
    private ManageClaimsPanel manageClaimsPanel;
    private Runnable userManagementReloader;

    // Dynamic brand controls
    private JMenuBar appMenuBar;
    private JPanel sidebarPanel;
    private JPanel topBarPanel;
    private JLabel logoLabel;
    private JLabel brandPill;
    private JLabel userRoleLabel;
    private JLabel welcomeUserLabel;
    private JLabel pageTitle;
    private JPanel heroBanner;
    private JPanel heroBannerText;
    private JLabel heroBannerTitle;
    private JLabel heroBannerSubtitle;
    private JButton nvidiaNavToggle;
    private JButton netflixNavToggle;
    private JButton webAppNavToggle;
    private final List<JLabel> dynamicAccentLabels = new ArrayList<>();
    private final List<JPanel> statCards = new ArrayList<>();
    private final List<JButton> navButtons = new ArrayList<>();

    public DashboardFrame(User user) {
        this.loggedInUser = user;

        this.itemService = new ItemService();
        this.userService = new UserService();
        this.claimService = new ClaimService();

        updateWindowTitle();
        setSize(1080, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Theme.addThemeChangeListener(this);

        getContentPane().setBackground(Theme.getBackground());
        getContentPane().setLayout(new BorderLayout());

        createMenuBar();
        createMainLayout();

        setVisible(true);
    }

    private void updateWindowTitle() {
        setTitle("Lost & Found [" + Theme.getCurrentTheme().getDisplayName() + "]");
    }

    // =====================================================
    // MENU BAR
    // =====================================================

    private void createMenuBar() {
        appMenuBar = new JMenuBar();
        appMenuBar.setBackground(Theme.getSidebar());
        appMenuBar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.getBorder()));

        JMenu accountMenu = new JMenu("Account");
        JMenu themeMenu = new JMenu("Visual Engine");
        JMenu webMenu = new JMenu("Web Integration");
        JMenu helpMenu = new JMenu("Help");

        accountMenu.setForeground(Theme.getTextPrimary());
        themeMenu.setForeground(Theme.getTextPrimary());
        webMenu.setForeground(Theme.getTextPrimary());
        helpMenu.setForeground(Theme.getTextPrimary());

        JMenuItem profileItem = new JMenuItem("Operator Profile");
        JMenuItem logoutItem = new JMenuItem("Sign Out");
        JMenuItem exitItem = new JMenuItem("Exit Application");

        JMenuItem nvidiaItem = new JMenuItem("🟢 NVIDIA GeForce RTX (Electric Neon Green)");
        JMenuItem netflixItem = new JMenuItem("🔴 Netflix Cinematic (Crimson Red)");

        nvidiaItem.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NVIDIA));
        netflixItem.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NETFLIX));

        themeMenu.add(nvidiaItem);
        themeMenu.add(netflixItem);

        JMenuItem openWebItem = new JMenuItem("🌐 Open Web App (localhost:" + WebServer.getActivePort() + ")");
        openWebItem.addActionListener(e -> WebServer.openInBrowser());
        webMenu.add(openWebItem);

        JMenuItem aboutItem = new JMenuItem("About Lost & Found");

        profileItem.addActionListener(e -> showProfile());
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Lost & Found System\n"
                        + "Dual Theme Engine: NVIDIA GeForce RTX & Netflix Cinematic\n"
                        + "Features: Precision Text Matching, Photo Proof Verification, Role-based Controls\n"
                        + "Synchronized Web Server: " + WebServer.getUrl() + "\n"
                        + "Active Mode: " + Theme.getCurrentTheme().getDisplayName(),
                "About Lost & Found",
                JOptionPane.INFORMATION_MESSAGE
        ));

        accountMenu.add(profileItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);
        accountMenu.add(exitItem);

        helpMenu.add(aboutItem);

        appMenuBar.add(accountMenu);
        appMenuBar.add(themeMenu);
        appMenuBar.add(webMenu);
        appMenuBar.add(helpMenu);

        setJMenuBar(appMenuBar);
    }

    // =====================================================
    // MAIN LAYOUT
    // =====================================================

    private void createMainLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.getBackground());

        sidebarPanel = createSidebar();
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.getBackground());

        topBarPanel = createTopBar();
        rightPanel.add(topBarPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.getBackground());

        // Panels
        addItemPanel = new AddItemPanel(loggedInUser);
        browseItemsPanel = new BrowseItemsPanel(loggedInUser);
        matchesPanel = new MatchesPanel(loggedInUser);
        submitClaimPanel = new SubmitClaimPanel(loggedInUser);
        myClaimsPanel = new MyClaimsPanel(loggedInUser);
        settingsPanel = new SettingsPanel();

        contentPanel.add(createOverviewPanel(), "Dashboard");
        contentPanel.add(addItemPanel, "Report Item");
        contentPanel.add(browseItemsPanel, "Browse Items");
        contentPanel.add(matchesPanel, "Matches");
        contentPanel.add(submitClaimPanel, "Submit Claim");
        contentPanel.add(myClaimsPanel, "My Claims");
        contentPanel.add(settingsPanel, "Settings");

        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            contentPanel.add(createUserManagementPanel(), "Manage Users");
            manageClaimsPanel = new ManageClaimsPanel(loggedInUser);
            contentPanel.add(manageClaimsPanel, "Manage Claims");
        }

        rightPanel.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(rightPanel, BorderLayout.CENTER);
        add(rootPanel);

        cardLayout.show(contentPanel, "Dashboard");
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Theme.getSidebar());
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.getBorder()));

        // Logo section - clean, professional
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Theme.getSidebar());
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(18, 16, 14, 14));

        brandPill = new JLabel(Theme.isNvidia() ? "⚡ NVIDIA GEFORCE RTX" : "🍿 NETFLIX CINEMATIC");
        brandPill.setForeground(Theme.getPrimaryAccent());
        brandPill.setFont(new Font("Segoe UI", Font.BOLD, 10));

        logoLabel = new JLabel("LOST & FOUND");
        logoLabel.setForeground(Theme.getTextPrimary());
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));

        logoPanel.add(brandPill);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoLabel);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Navigation section
        JPanel navigationPanel = new JPanel();
        navigationPanel.setBackground(Theme.getSidebar());
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

        addSidebarSection(navigationPanel, "NAVIGATION");
        addNavigationButton(navigationPanel, "▦   Dashboard", "Dashboard", true);
        addNavigationButton(navigationPanel, "＋   Report Item", "Report Item", false);
        addNavigationButton(navigationPanel, "⌕   Browse Items", "Browse Items", false);
        addNavigationButton(navigationPanel, "✦   Matches", "Matches", false);
        addNavigationButton(navigationPanel, "✓   Submit Claim", "Submit Claim", false);
        addNavigationButton(navigationPanel, "▤   My Claims", "My Claims", false);
        addNavigationButton(navigationPanel, "⚙   Settings", "Settings", false);

        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            addSidebarSection(navigationPanel, "ADMINISTRATION");
            addNavigationButton(navigationPanel, "♙   Manage Users", "Manage Users", false);
            addNavigationButton(navigationPanel, "🛡   Manage Claims", "Manage Claims", false);
        }

        JScrollPane navigationScroll = new JScrollPane(navigationPanel);
        navigationScroll.setBorder(null);
        navigationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScroll.getViewport().setBackground(Theme.getSidebar());

        sidebar.add(navigationScroll, BorderLayout.CENTER);

        // Bottom user badge & logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Theme.getSidebar());
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.getBorder()),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel userName = new JLabel("  " + loggedInUser.getFullName());
        userName.setForeground(Theme.getTextPrimary());
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        userRoleLabel = new JLabel("  ROLE: " + loggedInUser.getRole());
        userRoleLabel.setForeground(Theme.getPrimaryAccent());
        userRoleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));

        JButton profileButton = new JButton("My Profile");
        Theme.styleSecondaryButton(profileButton);
        profileButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        profileButton.addActionListener(e -> showProfile());

        JButton logoutButton = new JButton("Sign Out");
        Theme.styleDangerButton(logoutButton);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(userName);
        bottomPanel.add(Box.createVerticalStrut(2));
        bottomPanel.add(userRoleLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(profileButton);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(logoutButton);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addSidebarSection(JPanel panel, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLabel.setForeground(Theme.getTextMuted());
        sectionLabel.setBorder(new EmptyBorder(10, 8, 4, 8));
        panel.add(sectionLabel);
    }

    private void addNavigationButton(JPanel panel, String text, String cardName, boolean isDefault) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        Theme.styleSidebarButton(button, isDefault);

        if (isDefault) {
            selectedNavButton = button;
        }

        button.addActionListener(e -> {
            setSelectedNavigation(button);
            cardLayout.show(contentPanel, cardName);

            // Trigger dynamic reloads
            if ("Browse Items".equals(cardName) && browseItemsPanel != null) {
                browseItemsPanel.loadItems();
            } else if ("Matches".equals(cardName) && matchesPanel != null) {
                matchesPanel.loadMatches();
            } else if ("Submit Claim".equals(cardName) && submitClaimPanel != null) {
                submitClaimPanel.refreshData();
            } else if ("My Claims".equals(cardName) && myClaimsPanel != null) {
                myClaimsPanel.loadClaims();
            } else if ("Manage Claims".equals(cardName) && manageClaimsPanel != null) {
                manageClaimsPanel.loadClaims();
            } else if ("Manage Users".equals(cardName) && userManagementReloader != null) {
                userManagementReloader.run();
            } else if ("Dashboard".equals(cardName)) {
                refreshStatistics();
            }
        });

        navButtons.add(button);
        panel.add(button);
        panel.add(Box.createVerticalStrut(2));
    }

    private void setSelectedNavigation(JButton button) {
        if (selectedNavButton != null) {
            Theme.updateSidebarButtonState(selectedNavButton, false);
        }
        selectedNavButton = button;
        Theme.updateSidebarButtonState(selectedNavButton, true);
    }

    // =====================================================
    // TOP BAR
    // =====================================================

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.getBackground());
        topBar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.getBorder()),
                new EmptyBorder(10, 18, 10, 18)
        ));

        pageTitle = new JLabel("LOST & FOUND");
        pageTitle.setForeground(Theme.getTextPrimary());
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        welcomeUserLabel = new JLabel("Welcome back, " + loggedInUser.getFullName());
        welcomeUserLabel.setForeground(Theme.getPrimaryAccent());
        welcomeUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightControls.setBackground(Theme.getBackground());

        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setForeground(Theme.getTextMuted());
        themeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        nvidiaNavToggle = new JButton("🟢 NVIDIA");
        netflixNavToggle = new JButton("🔴 Netflix");
        Theme.styleTopBarButton(nvidiaNavToggle, Theme.isNvidia());
        Theme.styleTopBarButton(netflixNavToggle, Theme.isNetflix());

        nvidiaNavToggle.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NVIDIA));
        netflixNavToggle.addActionListener(e -> Theme.setTheme(Theme.ThemeMode.NETFLIX));

        webAppNavToggle = new JButton("🌐 Web App");
        Theme.styleTopBarButton(webAppNavToggle, false);
        webAppNavToggle.setToolTipText("Open synchronized web portal (" + WebServer.getUrl() + ")");
        webAppNavToggle.addActionListener(e -> WebServer.openInBrowser());

        JButton topLogoutBtn = new JButton("Logout");
        Theme.styleDangerButton(topLogoutBtn);
        topLogoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        topLogoutBtn.addActionListener(e -> logout());

        rightControls.add(welcomeUserLabel);
        rightControls.add(Box.createHorizontalStrut(10));
        rightControls.add(themeLabel);
        rightControls.add(nvidiaNavToggle);
        rightControls.add(netflixNavToggle);
        rightControls.add(Box.createHorizontalStrut(6));
        rightControls.add(webAppNavToggle);
        rightControls.add(Box.createHorizontalStrut(6));
        rightControls.add(topLogoutBtn);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        return topBar;
    }

    // =====================================================
    // OVERVIEW PANEL (HERO + STATS)
    // =====================================================

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 14));
        panel.setBackground(Theme.getBackground());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topContent = new JPanel();
        topContent.setBackground(Theme.getBackground());
        topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));

        // Featured Hero Banner
        heroBanner = new JPanel(new BorderLayout());
        heroBanner.setBackground(Theme.getBannerBackground());
        heroBanner.setBorder(new CompoundBorder(
                new LineBorder(Theme.getAccentDark(), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        heroBannerTitle = new JLabel(Theme.isNvidia()
                ? "PRECISION RECOVERY SYSTEM "
                : "CINEMATIC RECOVERY PLATFORM ");
        heroBannerTitle.setForeground(Theme.getPrimaryAccent());
        heroBannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        heroBannerSubtitle = new JLabel(
                "Report lost belongings, browse found items, attach photo verification proof, and synchronize seamlessly with the integrated web app."
        );
        heroBannerSubtitle.setForeground(Theme.getTextPrimary());
        heroBannerSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        heroBannerText = new JPanel();
        heroBannerText.setBackground(Theme.getBannerBackground());
        heroBannerText.setLayout(new BoxLayout(heroBannerText, BoxLayout.Y_AXIS));
        heroBannerText.add(heroBannerTitle);
        heroBannerText.add(Box.createVerticalStrut(4));
        heroBannerText.add(heroBannerSubtitle);

        heroBanner.add(heroBannerText, BorderLayout.CENTER);
        topContent.add(heroBanner);
        topContent.add(Box.createVerticalStrut(14));

        JLabel heading = new JLabel("INVENTORY & VERIFICATION METRICS");
        heading.setForeground(Theme.getTextMuted());
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topContent.add(heading);
        topContent.add(Box.createVerticalStrut(8));

        panel.add(topContent, BorderLayout.NORTH);

        // 6 Stat Cards Grid
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        cardsPanel.setBackground(Theme.getBackground());

        totalItemsLabel = new JLabel("0");
        lostItemsLabel = new JLabel("0");
        foundItemsLabel = new JLabel("0");
        totalUsersLabel = new JLabel("0");
        pendingClaimsLabel = new JLabel("0");
        approvedClaimsLabel = new JLabel("0");

        cardsPanel.add(createStatCard("TOTAL INVENTORY", "📦", totalItemsLabel));
        cardsPanel.add(createStatCard("LOST BELONGINGS", "🔍", lostItemsLabel));
        cardsPanel.add(createStatCard("FOUND ITEMS", "✓", foundItemsLabel));
        cardsPanel.add(createStatCard("REGISTERED USERS", "♙", totalUsersLabel));
        cardsPanel.add(createStatCard("PENDING CLAIMS", "◷", pendingClaimsLabel));
        cardsPanel.add(createStatCard("VERIFIED CLAIMS", "★", approvedClaimsLabel));

        panel.add(cardsPanel, BorderLayout.CENTER);

        // Bottom action bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomPanel.setBackground(Theme.getBackground());

        JButton quickReportBtn = new JButton("＋ Report Lost/Found");
        JButton quickClaimBtn = new JButton("✓ Submit Claim");
        JButton openWebBtn = new JButton("🌐 Open Web App");
        JButton refreshBtn = new JButton("↻ Refresh Metrics");

        Theme.styleSecondaryButton(quickReportBtn);
        Theme.styleSecondaryButton(quickClaimBtn);
        Theme.styleSecondaryButton(openWebBtn);
        Theme.stylePrimaryButton(refreshBtn);

        quickReportBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Report Item");
            for (JButton b : navButtons) {
                if (b.getText().contains("Report Item")) setSelectedNavigation(b);
            }
        });

        quickClaimBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Submit Claim");
            submitClaimPanel.refreshData();
            for (JButton b : navButtons) {
                if (b.getText().contains("Submit Claim")) setSelectedNavigation(b);
            }
        });

        openWebBtn.addActionListener(e -> WebServer.openInBrowser());
        refreshBtn.addActionListener(e -> refreshStatistics());

        bottomPanel.add(quickReportBtn);
        bottomPanel.add(quickClaimBtn);
        bottomPanel.add(openWebBtn);
        bottomPanel.add(refreshBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        refreshStatistics();
        return panel;
    }

    private JPanel createStatCard(String title, String icon, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        Theme.styleCard(card);
        statCards.add(card);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(Theme.getPrimaryAccent());
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dynamicAccentLabels.add(iconLabel);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.getTextMuted());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        valueLabel.setForeground(Theme.getTextPrimary());
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JPanel upper = new JPanel(new BorderLayout());
        upper.setBackground(Theme.getCard());
        upper.add(titleLabel, BorderLayout.WEST);
        upper.add(iconLabel, BorderLayout.EAST);

        card.add(upper, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshStatistics() {
        totalItemsLabel.setText(String.valueOf(itemService.countItems()));
        lostItemsLabel.setText(String.valueOf(itemService.countLostItems()));
        foundItemsLabel.setText(String.valueOf(itemService.countFoundItems()));
        totalUsersLabel.setText(String.valueOf(userService.getAllUsers().size()));
        pendingClaimsLabel.setText(String.valueOf(claimService.countPendingClaims()));
        approvedClaimsLabel.setText(String.valueOf(claimService.countApprovedClaims()));
    }

    // =====================================================
    // USER MANAGEMENT (ADMIN ONLY)
    // =====================================================

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        Theme.stylePanel(panel);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel heading = new JLabel("USER ACCOUNTS MANAGEMENT");
        Theme.styleBrandHeading(heading);
        panel.add(heading, BorderLayout.NORTH);

        String[] columns = {"ID", "Full Name", "Username", "Email Address", "Role", "Account Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable userTable = new JTable(model);
        userTable.setRowHeight(34);
        Theme.styleTable(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        Theme.styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("↻ Refresh");
        JButton blockButton = new JButton("Toggle Block Status");
        Theme.stylePrimaryButton(refreshButton);
        Theme.styleSecondaryButton(blockButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(Theme.getBackground());
        buttonPanel.add(blockButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        userManagementReloader = () -> {
            model.setRowCount(0);
            for (User user : userService.getAllUsers()) {
                String status = user.isBlocked() ? "Blocked" : "Active";
                model.addRow(new Object[]{
                        user.getId(), user.getFullName(), user.getUsername(),
                        user.getEmail(), user.getRole(), status
                });
            }
        };

        userManagementReloader.run();
        refreshButton.addActionListener(e -> userManagementReloader.run());

        blockButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an account row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int userId = (int) model.getValueAt(selectedRow, 0);
            String username = (String) model.getValueAt(selectedRow, 2);
            if (username.equalsIgnoreCase(loggedInUser.getUsername())) {
                JOptionPane.showMessageDialog(this, "You cannot block your own active administrator account.", "Action Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }
            userService.toggleBlock(userId);
            userManagementReloader.run();
            refreshStatistics();
        });

        return panel;
    }

    // =====================================================
    // THEME CHANGE LISTENER (DYNAMIC SYNC)
    // =====================================================

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        updateWindowTitle();
        getContentPane().setBackground(Theme.getBackground());

        if (appMenuBar != null) {
            appMenuBar.setBackground(Theme.getSidebar());
            appMenuBar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.getBorder()));
            for (int i = 0; i < appMenuBar.getMenuCount(); i++) {
                JMenu m = appMenuBar.getMenu(i);
                if (m != null) m.setForeground(Theme.getTextPrimary());
            }
        }

        if (sidebarPanel != null) {
            sidebarPanel.setBackground(Theme.getSidebar());
            sidebarPanel.setBorder(new MatteBorder(0, 0, 0, 1, Theme.getBorder()));
        }

        if (topBarPanel != null) {
            topBarPanel.setBackground(Theme.getBackground());
            topBarPanel.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, Theme.getBorder()),
                    new EmptyBorder(10, 18, 10, 18)
            ));
        }

        if (pageTitle != null) pageTitle.setForeground(Theme.getTextPrimary());

        if (brandPill != null) {
            brandPill.setText(Theme.isNvidia() ? "⚡ NVIDIA GEFORCE RTX" : "🍿 NETFLIX CINEMATIC");
            brandPill.setForeground(Theme.getPrimaryAccent());
        }

        if (logoLabel != null) logoLabel.setForeground(Theme.getTextPrimary());
        if (userRoleLabel != null) userRoleLabel.setForeground(Theme.getPrimaryAccent());
        if (welcomeUserLabel != null) welcomeUserLabel.setForeground(Theme.getPrimaryAccent());

        for (JButton b : navButtons) {
            Theme.updateSidebarButtonState(b, b == selectedNavButton);
        }

        if (heroBanner != null) {
            heroBanner.setBackground(Theme.getBannerBackground());
            heroBanner.setBorder(new CompoundBorder(
                    new LineBorder(Theme.getAccentDark(), 1, true),
                    new EmptyBorder(16, 20, 16, 20)
            ));
        }

        if (heroBannerText != null) {
            heroBannerText.setBackground(Theme.getBannerBackground());
        }

        if (heroBannerTitle != null) {
            heroBannerTitle.setText(Theme.isNvidia()
                    ? "PRECISION RECOVERY SYSTEM "
                    : "CINEMATIC RECOVERY PLATFORM ");
            heroBannerTitle.setForeground(Theme.getPrimaryAccent());
        }

        if (heroBannerSubtitle != null) {
            heroBannerSubtitle.setForeground(Theme.getTextPrimary());
        }

        for (JLabel lbl : dynamicAccentLabels) {
            lbl.setForeground(Theme.getPrimaryAccent());
        }

        for (JPanel card : statCards) {
            Theme.styleCard(card);
        }

        if (nvidiaNavToggle != null) Theme.updateTopBarButtonState(nvidiaNavToggle, Theme.isNvidia());
        if (netflixNavToggle != null) Theme.updateTopBarButtonState(netflixNavToggle, Theme.isNetflix());
        if (webAppNavToggle != null) Theme.updateTopBarButtonState(webAppNavToggle, false);

        repaint();
    }

    private void showProfile() {
        JOptionPane.showMessageDialog(
                this,
                "Operator Identity Profile\n"
                        + "─────────────────────────\n"
                        + "Name: " + loggedInUser.getFullName() + "\n"
                        + "Username: @" + loggedInUser.getUsername() + "\n"
                        + "Email: " + loggedInUser.getEmail() + "\n"
                        + "Role: " + loggedInUser.getRole() + "\n"
                        + "Active Visual Engine: " + Theme.getCurrentTheme().getDisplayName(),
                "Operator Profile",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out from the system?",
                "Sign Out",
                JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            Theme.removeThemeChangeListener(this);
            dispose();
            new LoginFrame();
        }
    }
}
