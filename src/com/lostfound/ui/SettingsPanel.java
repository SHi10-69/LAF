package com.lostfound.ui;

import com.lostfound.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SettingsPanel extends JPanel implements Theme.ThemeChangeListener {

    private JRadioButton nvidiaRadio;
    private JRadioButton netflixRadio;
    private JLabel statusLabel;
    private JPanel themeCard;
    private JPanel previewCard;
    private JPanel sysInfoCard;
    private JLabel heading;
    private JLabel themeHeader;
    private JLabel themeDesc;
    private JLabel previewTitle;
    private JButton previewPrimaryBtn;
    private JButton previewSecondaryBtn;
    private JLabel previewBadge;
    private JTextField previewField;

    public SettingsPanel() {
        createGUI();
        Theme.addThemeChangeListener(this);
        applyThemeStyling();
    }

    private void createGUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.getBackground());
        setBorder(new EmptyBorder(18, 24, 18, 24));

        // Heading
        heading = new JLabel("SYSTEM SETTINGS & VISUAL PREFERENCES");
        Theme.styleBrandHeading(heading);
        add(heading, BorderLayout.NORTH);

        // Center Content with Cards
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.getBackground());

        // --- Theme Selection Card ---
        themeCard = new JPanel();
        themeCard.setLayout(new BoxLayout(themeCard, BoxLayout.Y_AXIS));
        Theme.styleCard(themeCard);

        themeHeader = new JLabel("Interface Theme & Brand Aesthetic");
        themeHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        themeHeader.setForeground(Theme.getTextPrimary());
        themeHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeDesc = new JLabel("Switch seamlessly between NVIDIA GeForce RTX neon aesthetics and Netflix Cinematic dark mode. Changes apply in real time.");
        themeDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        themeDesc.setForeground(Theme.getTextMuted());
        themeDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeCard.add(themeHeader);
        themeCard.add(Box.createVerticalStrut(4));
        themeCard.add(themeDesc);
        themeCard.add(Box.createVerticalStrut(14));

        // Radio options
        nvidiaRadio = new JRadioButton("🟢  NVIDIA GeForce RTX  —  Obsidian Titanium & Electric Neon Green (#76B900)");
        nvidiaRadio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nvidiaRadio.setFocusPainted(false);
        nvidiaRadio.setSelected(Theme.isNvidia());
        nvidiaRadio.setAlignmentX(Component.LEFT_ALIGNMENT);

        netflixRadio = new JRadioButton("🔴  Netflix Cinematic  —  Pure Pitch Black & Cinematic Crimson Red (#E50914)");
        netflixRadio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        netflixRadio.setFocusPainted(false);
        netflixRadio.setSelected(Theme.isNetflix());
        netflixRadio.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(nvidiaRadio);
        group.add(netflixRadio);

        nvidiaRadio.addActionListener(e -> {
            Theme.setTheme(Theme.ThemeMode.NVIDIA);
            updateStatus("Theme switched to NVIDIA GeForce RTX (Electric Neon Green).");
        });

        netflixRadio.addActionListener(e -> {
            Theme.setTheme(Theme.ThemeMode.NETFLIX);
            updateStatus("Theme switched to Netflix Cinematic (Crimson Red).");
        });

        themeCard.add(nvidiaRadio);
        themeCard.add(Box.createVerticalStrut(8));
        themeCard.add(netflixRadio);
        themeCard.add(Box.createVerticalStrut(12));

        statusLabel = new JLabel("Active: " + Theme.getCurrentTheme().getDisplayName() + " (" + Theme.getCurrentTheme().getSubtitle() + ")");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(Theme.getPrimaryAccent());
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeCard.add(statusLabel);

        content.add(themeCard);
        content.add(Box.createVerticalStrut(14));

        // --- Live Component Palette Preview Card ---
        previewCard = new JPanel();
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));
        Theme.styleCard(previewCard);

        previewTitle = new JLabel("Live Theme Palette Showcase");
        previewTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        previewTitle.setForeground(Theme.getTextPrimary());
        previewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel previewSub = new JLabel("Sample controls rendered dynamically with active theme styling:");
        previewSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewSub.setForeground(Theme.getTextMuted());
        previewSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        previewCard.add(previewTitle);
        previewCard.add(Box.createVerticalStrut(4));
        previewCard.add(previewSub);
        previewCard.add(Box.createVerticalStrut(12));

        JPanel sampleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        sampleRow.setBackground(Theme.getCard());
        sampleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        previewPrimaryBtn = new JButton("Primary Action");
        previewSecondaryBtn = new JButton("Secondary Action");
        Theme.stylePrimaryButton(previewPrimaryBtn);
        Theme.styleSecondaryButton(previewSecondaryBtn);

        previewBadge = new JLabel("●  STATUS: VERIFIED");
        previewBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewBadge.setForeground(Theme.STATUS_APPROVED);

        previewField = new JTextField("Sample Form Input", 16);
        Theme.styleTextField(previewField);

        sampleRow.add(previewPrimaryBtn);
        sampleRow.add(previewSecondaryBtn);
        sampleRow.add(previewField);
        sampleRow.add(previewBadge);

        previewCard.add(sampleRow);
        content.add(previewCard);
        content.add(Box.createVerticalStrut(14));

        // --- System Diagnostics Card ---
        sysInfoCard = new JPanel();
        sysInfoCard.setLayout(new BoxLayout(sysInfoCard, BoxLayout.Y_AXIS));
        Theme.styleCard(sysInfoCard);

        JLabel sysHeader = new JLabel("Environment & Data Storage");
        sysHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sysHeader.setForeground(Theme.getTextPrimary());
        sysHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        sysInfoCard.add(sysHeader);
        sysInfoCard.add(Box.createVerticalStrut(10));

        addInfoRow(sysInfoCard, "Java Runtime:", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        addInfoRow(sysInfoCard, "Operating System:", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        addInfoRow(sysInfoCard, "Storage Engine:", "Java Object Stream (Serializable .dat format)");
        addInfoRow(sysInfoCard, "Proof Image Storage:", "data/claim_proofs/ (Secure Local Repository)");
        addInfoRow(sysInfoCard, "Item Image Storage:", "data/item_images/ (Local Catalog)");

        content.add(sysInfoCard);

        JScrollPane scrollPane = new JScrollPane(content);
        Theme.styleScrollPane(scrollPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Theme.getCard());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.getTextMuted());

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(Theme.getTextPrimary());

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(3));
    }

    private void updateStatus(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(Theme.getPrimaryAccent());
    }

    private void applyThemeStyling() {
        setBackground(Theme.getBackground());
        Theme.styleBrandHeading(heading);
        Theme.styleCard(themeCard);
        Theme.styleCard(previewCard);
        Theme.styleCard(sysInfoCard);

        themeHeader.setForeground(Theme.getTextPrimary());
        themeDesc.setForeground(Theme.getTextMuted());

        nvidiaRadio.setBackground(Theme.getCard());
        nvidiaRadio.setForeground(Theme.getTextPrimary());
        netflixRadio.setBackground(Theme.getCard());
        netflixRadio.setForeground(Theme.getTextPrimary());

        nvidiaRadio.setSelected(Theme.isNvidia());
        netflixRadio.setSelected(Theme.isNetflix());

        statusLabel.setForeground(Theme.getPrimaryAccent());
        previewTitle.setForeground(Theme.getTextPrimary());

        Theme.stylePrimaryButton(previewPrimaryBtn);
        Theme.styleSecondaryButton(previewSecondaryBtn);
        Theme.styleTextField(previewField);

        repaint();
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        applyThemeStyling();
    }
}
