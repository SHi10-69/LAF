package com.lostfound.util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Theme {

    public enum ThemeMode {
        NVIDIA("NVIDIA GeForce RTX", "Titanium Carbon & Electric Neon Green", "RTX ON"),
        NETFLIX("Netflix Cinematic", "Pitch Black & Cinematic Crimson Red", "NETFLIX");

        private final String displayName;
        private final String subtitle;
        private final String badge;

        ThemeMode(String displayName, String subtitle, String badge) {
            this.displayName = displayName;
            this.subtitle = subtitle;
            this.badge = badge;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getBadge() {
            return badge;
        }
    }

    // =====================================================
    // BRAND CONSTANTS (STRICTLY ZERO BLUE COLOR ANYWHERE)
    // =====================================================
    // NVIDIA Signature Palette (Pure Neutral Carbon Obsidian)
    public static final Color NVIDIA_GREEN       = new Color(118, 185, 0);   // #76B900 Official GeForce RTX Green
    public static final Color NVIDIA_GREEN_DARK  = new Color(65, 110, 0);
    public static final Color NVIDIA_GREEN_HOVER = new Color(140, 219, 0);
    public static final Color NVIDIA_BG          = new Color(14, 14, 14);    // Pure Carbon Obsidian (No Blue)
    public static final Color NVIDIA_SIDEBAR     = new Color(10, 10, 10);    // Pure Pitch Charcoal (No Blue)
    public static final Color NVIDIA_CARD        = new Color(22, 22, 22);    // Dark Titanium Graphite (No Blue)
    public static final Color NVIDIA_CARD_HOVER  = new Color(34, 34, 34);
    public static final Color NVIDIA_BORDER      = new Color(44, 44, 44);    // Neutral Gunmetal Border (No Blue)
    public static final Color NVIDIA_MUTED       = new Color(185, 185, 185); // High-visibility bright silver

    // Netflix Signature Palette (Pure Neutral Cinema Black)
    public static final Color NETFLIX_RED        = new Color(229, 9, 20);    // #E50914 Official Netflix Red
    public static final Color NETFLIX_RED_DARK   = new Color(153, 0, 0);
    public static final Color NETFLIX_RED_HOVER  = new Color(255, 30, 39);
    public static final Color NETFLIX_BG         = new Color(16, 16, 16);    // Cinematic Black (No Blue)
    public static final Color NETFLIX_SIDEBAR    = new Color(10, 10, 10);    // Pure Cinema Pitch (No Blue)
    public static final Color NETFLIX_CARD       = new Color(24, 24, 24);    // Matte Cinema Card (No Blue)
    public static final Color NETFLIX_CARD_HOVER = new Color(36, 36, 36);
    public static final Color NETFLIX_BORDER     = new Color(48, 48, 48);    // Neutral Dark Border (No Blue)
    public static final Color NETFLIX_MUTED      = new Color(185, 185, 185); // High-visibility bright silver

    // Shared Status Badges (Strictly Zero Blue Colors)
    public static final Color STATUS_APPROVED    = new Color(16, 185, 129);  // Emerald Green
    public static final Color STATUS_PENDING     = new Color(245, 158, 11);  // Amber Gold
    public static final Color STATUS_REJECTED    = new Color(239, 68, 68);   // Crimson Red
    public static final Color STATUS_LOST        = new Color(244, 63, 94);   // Rose Coral
    public static final Color STATUS_FOUND       = new Color(34, 197, 94);   // Vivid Emerald (Never Blue)

    // Ultra High-Readability Crisp White Typography
    public static final Color TEXT_WHITE         = new Color(255, 255, 255); // 100% Pure Crisp White
    public static final Color TEXT_LIGHT         = new Color(240, 240, 240); // Off-White
    public static final Color TEXT_SILVER        = new Color(195, 195, 195); // High-contrast silver for labels

    // Component input background (Dark charcoal)
    public static final Color INPUT_SURFACE      = new Color(28, 28, 28);

    // Backward compatibility constants
    public static final Color GREEN              = NVIDIA_GREEN;
    public static final Color GREEN_DARK         = NVIDIA_GREEN_DARK;
    public static final Color RED                = NETFLIX_RED;
    public static final Color RED_DARK           = NETFLIX_RED_DARK;
    public static final Color YELLOW             = STATUS_PENDING;
    public static final Color YELLOW_LIGHT       = new Color(253, 224, 71);
    public static final Color ORANGE_DARK        = new Color(234, 88, 12);
    public static final Color ORANGE_DEEP        = new Color(194, 65, 12);
    public static final Color TEXT_YELLOW        = YELLOW;
    public static final Color TEXT_ORANGE        = ORANGE_DARK;

    // Dynamically mutated surface references
    public static Color WHITE                    = TEXT_WHITE;
    public static Color MUTED                    = NVIDIA_MUTED;
    public static Color BACKGROUND               = NVIDIA_BG;
    public static Color SIDEBAR                  = NVIDIA_SIDEBAR;
    public static Color CARD                     = NVIDIA_CARD;
    public static Color CARD_HOVER               = NVIDIA_CARD_HOVER;
    public static Color BORDER                   = NVIDIA_BORDER;

    // Active Theme State
    private static ThemeMode currentMode = ThemeMode.NVIDIA;
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();
    private static final String CONFIG_FILE = "data/theme.cfg";

    static {
        loadSavedTheme();
        updateDynamicConstants();
    }

    public interface ThemeChangeListener {
        void onThemeChanged(ThemeMode newMode);
    }

    private Theme() {}

    // =====================================================
    // THEME STATE & GETTERS
    // =====================================================

    public static ThemeMode getCurrentTheme() {
        return currentMode;
    }

    public static boolean isNvidia() {
        return currentMode == ThemeMode.NVIDIA;
    }

    public static boolean isNetflix() {
        return currentMode == ThemeMode.NETFLIX;
    }

    public static void setTheme(ThemeMode mode) {
        if (mode == null) return;
        currentMode = mode;
        updateDynamicConstants();
        saveTheme(mode);
        applyGlobalDefaults();
        notifyListeners();
    }

    public static void toggleTheme() {
        setTheme(currentMode == ThemeMode.NVIDIA ? ThemeMode.NETFLIX : ThemeMode.NVIDIA);
    }

    private static void updateDynamicConstants() {
        if (currentMode == ThemeMode.NVIDIA) {
            BACKGROUND  = NVIDIA_BG;
            SIDEBAR     = NVIDIA_SIDEBAR;
            CARD        = NVIDIA_CARD;
            CARD_HOVER  = NVIDIA_CARD_HOVER;
            BORDER      = NVIDIA_BORDER;
            MUTED       = NVIDIA_MUTED;
        } else {
            BACKGROUND  = NETFLIX_BG;
            SIDEBAR     = NETFLIX_SIDEBAR;
            CARD        = NETFLIX_CARD;
            CARD_HOVER  = NETFLIX_CARD_HOVER;
            BORDER      = NETFLIX_BORDER;
            MUTED       = NETFLIX_MUTED;
        }
        WHITE = TEXT_WHITE;
    }

    public static void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (ThemeChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onThemeChanged(currentMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Dynamic getters
    public static Color getPrimaryAccent() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_GREEN : NETFLIX_RED;
    }

    public static Color getAccentDark() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_GREEN_DARK : NETFLIX_RED_DARK;
    }

    public static Color getAccentHover() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_GREEN_HOVER : NETFLIX_RED_HOVER;
    }

    public static Color getButtonTextColor() {
        return currentMode == ThemeMode.NVIDIA ? Color.BLACK : TEXT_WHITE;
    }

    public static Color getBackground() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_BG : NETFLIX_BG;
    }

    public static Color getSidebar() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_SIDEBAR : NETFLIX_SIDEBAR;
    }

    public static Color getCard() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_CARD : NETFLIX_CARD;
    }

    public static Color getCardHover() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_CARD_HOVER : NETFLIX_CARD_HOVER;
    }

    public static Color getBorder() {
        return currentMode == ThemeMode.NVIDIA ? NVIDIA_BORDER : NETFLIX_BORDER;
    }

    public static Color getTextPrimary() {
        return TEXT_WHITE;
    }

    public static Color getTextMuted() {
        return TEXT_SILVER;
    }

    public static Color getActiveNavBackground() {
        return currentMode == ThemeMode.NVIDIA
                ? new Color(24, 40, 12)
                : new Color(48, 14, 16);
    }

    public static Color getBannerBackground() {
        return currentMode == ThemeMode.NVIDIA
                ? new Color(18, 28, 12)
                : new Color(36, 12, 14);
    }

    // =====================================================
    // GLOBAL UIMANAGER INITIALIZATION
    // =====================================================

    public static void applyGlobalDefaults() {
        updateDynamicConstants();

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Color bg = getBackground();
        Color card = getCard();
        Color fg = TEXT_WHITE;
        Color accent = getPrimaryAccent();
        Color accentDark = getAccentDark();
        Color inputBg = INPUT_SURFACE;
        Color border = getBorder();

        UIManager.put("Panel.background", bg);
        UIManager.put("Panel.foreground", fg);
        UIManager.put("Label.foreground", fg);
        UIManager.put("Label.background", bg);

        // JOptionPane Dark Defaults
        UIManager.put("OptionPane.background", card);
        UIManager.put("OptionPane.foreground", fg);
        UIManager.put("OptionPane.messageForeground", fg);
        UIManager.put("OptionPane.messageArea.background", card);

        UIManager.put("Button.background", card);
        UIManager.put("Button.foreground", fg);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        // Form Fields Dark Styling
        UIManager.put("TextField.background", inputBg);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("TextField.caretForeground", accent);
        UIManager.put("TextField.selectionBackground", accentDark);
        UIManager.put("TextField.selectionForeground", fg);

        UIManager.put("PasswordField.background", inputBg);
        UIManager.put("PasswordField.foreground", fg);
        UIManager.put("PasswordField.caretForeground", accent);

        UIManager.put("TextArea.background", inputBg);
        UIManager.put("TextArea.foreground", fg);
        UIManager.put("TextArea.caretForeground", accent);

        // ComboBox & List Styling
        UIManager.put("ComboBox.background", inputBg);
        UIManager.put("ComboBox.foreground", fg);
        UIManager.put("ComboBox.selectionBackground", accentDark);
        UIManager.put("ComboBox.selectionForeground", fg);

        UIManager.put("List.background", card);
        UIManager.put("List.foreground", fg);
        UIManager.put("List.selectionBackground", accentDark);
        UIManager.put("List.selectionForeground", fg);

        // Table Styling (Strictly No Blue)
        UIManager.put("Table.background", card);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.selectionBackground", accentDark);
        UIManager.put("Table.selectionForeground", fg);
        UIManager.put("Table.gridColor", new Color(38, 38, 38));

        UIManager.put("TableHeader.background", getSidebar());
        UIManager.put("TableHeader.foreground", accent);

        UIManager.put("ScrollPane.background", bg);
        UIManager.put("Viewport.background", bg);

        UIManager.put("CheckBox.background", card);
        UIManager.put("CheckBox.foreground", fg);
        UIManager.put("RadioButton.background", card);
        UIManager.put("RadioButton.foreground", fg);
    }

    // =====================================================
    // CUSTOM MODERN DARK BUTTON UI (ELIMINATES WINDOWS WHITE BUTTON SKIN)
    // =====================================================

    public enum ButtonStyle {
        PRIMARY, SECONDARY, DANGER, SIDEBAR, TOPBAR
    }

    public static class DarkButtonUI extends BasicButtonUI {
        private final ButtonStyle style;
        private boolean isSelected;

        public DarkButtonUI(ButtonStyle style) {
            this(style, false);
        }

        public DarkButtonUI(ButtonStyle style, boolean isSelected) {
            this.style = style;
            this.isSelected = isSelected;
        }

        public void setSelectedState(boolean selected) {
            this.isSelected = selected;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            Color bg;
            Color text;
            Color border = null;

            switch (style) {
                case PRIMARY:
                    bg = getPrimaryAccent();
                    text = getButtonTextColor();
                    if (model.isPressed()) {
                        bg = getAccentDark();
                    } else if (model.isRollover()) {
                        bg = getAccentHover();
                    }
                    break;

                case DANGER:
                    bg = new Color(185, 28, 28);
                    text = TEXT_WHITE;
                    border = new Color(220, 38, 38);
                    if (model.isPressed()) {
                        bg = new Color(153, 27, 27);
                    } else if (model.isRollover()) {
                        bg = new Color(220, 38, 38);
                    }
                    break;

                case SIDEBAR:
                    if (isSelected) {
                        bg = getActiveNavBackground();
                        text = getPrimaryAccent();
                        border = getPrimaryAccent();
                    } else {
                        bg = getSidebar();
                        text = model.isRollover() ? TEXT_WHITE : TEXT_LIGHT;
                        if (model.isRollover()) {
                            bg = getCardHover();
                        }
                    }
                    break;

                case TOPBAR:
                    if (isSelected) {
                        bg = getCardHover();
                        text = getPrimaryAccent();
                        border = getPrimaryAccent();
                    } else {
                        bg = getCard();
                        text = model.isRollover() ? TEXT_WHITE : TEXT_LIGHT;
                        border = getBorder();
                        if (model.isRollover()) {
                            bg = getCardHover();
                        }
                    }
                    break;

                case SECONDARY:
                default:
                    bg = getCard();
                    text = TEXT_WHITE;
                    border = getBorder();
                    if (model.isPressed()) {
                        bg = new Color(Math.max(0, getCard().getRed() - 6),
                                       Math.max(0, getCard().getGreen() - 6),
                                       Math.max(0, getCard().getBlue() - 6));
                    } else if (model.isRollover()) {
                        bg = getCardHover();
                        border = getPrimaryAccent();
                    }
                    break;
            }

            b.setForeground(text);

            // Paint background shape
            int arc = style == ButtonStyle.SIDEBAR ? 4 : 6;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Paint custom border
            if (style == ButtonStyle.SIDEBAR && isSelected) {
                // Accent indicator bar on left edge
                g2.setColor(getPrimaryAccent());
                g2.fillRect(0, 0, 4, h);
            } else if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }

            g2.dispose();
            super.paint(g, c);
        }
    }

    // =====================================================
    // CUSTOM MODERN DARK COMBOBOX UI (ELIMINATES WINDOWS WHITE DROPDOWNS & BLUE ARROWS)
    // =====================================================

    public static class DarkComboBoxUI extends BasicComboBoxUI {
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            JComboBox<?> combo = (JComboBox<?>) c;
            combo.setOpaque(true);
            combo.setBackground(INPUT_SURFACE);
            combo.setForeground(TEXT_WHITE);
        }

        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(INPUT_SURFACE);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Clean accent chevron (Never blue!)
                    g2.setColor(getPrimaryAccent());
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    int[] xPoints = {cx - 4, cx + 4, cx};
                    int[] yPoints = {cy - 2, cy - 2, cy + 3};
                    g2.fillPolygon(xPoints, yPoints, 3);
                    g2.dispose();
                }
            };
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setOpaque(true);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            return btn;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(INPUT_SURFACE);
            g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.dispose();
        }
    }

    // =====================================================
    // COMPONENT STYLING HELPERS
    // =====================================================

    public static void stylePanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(getBackground());
        panel.setForeground(TEXT_WHITE);
    }

    public static void styleHeading(JLabel label) {
        label.setForeground(TEXT_WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
    }

    public static void styleBrandHeading(JLabel label) {
        label.setForeground(getPrimaryAccent());
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT_SILVER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public static void stylePrimaryButton(JButton button) {
        button.setUI(new DarkButtonUI(ButtonStyle.PRIMARY));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setUI(new DarkButtonUI(ButtonStyle.SECONDARY));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(7, 14, 7, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleDangerButton(JButton button) {
        button.setUI(new DarkButtonUI(ButtonStyle.DANGER));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(7, 14, 7, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSidebarButton(JButton button, boolean isSelected) {
        button.setUI(new DarkButtonUI(ButtonStyle.SIDEBAR, isSelected));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void updateSidebarButtonState(JButton button, boolean isSelected) {
        if (button.getUI() instanceof DarkButtonUI) {
            ((DarkButtonUI) button.getUI()).setSelectedState(isSelected);
            button.repaint();
        } else {
            button.setUI(new DarkButtonUI(ButtonStyle.SIDEBAR, isSelected));
        }
    }

    public static void styleTopBarButton(JButton button, boolean isActive) {
        button.setUI(new DarkButtonUI(ButtonStyle.TOPBAR, isActive));
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void updateTopBarButtonState(JButton button, boolean isActive) {
        if (button.getUI() instanceof DarkButtonUI) {
            ((DarkButtonUI) button.getUI()).setSelectedState(isActive);
            button.repaint();
        } else {
            button.setUI(new DarkButtonUI(ButtonStyle.TOPBAR, isActive));
        }
    }

    public static void styleTextField(JTextField field) {
        field.setOpaque(true);
        field.setBackground(INPUT_SURFACE);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(getPrimaryAccent());
        field.setSelectedTextColor(TEXT_WHITE);
        field.setSelectionColor(getAccentDark());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(getBorder(), 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setOpaque(true);
        field.setBackground(INPUT_SURFACE);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(getPrimaryAccent());
        field.setSelectedTextColor(TEXT_WHITE);
        field.setSelectionColor(getAccentDark());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(getBorder(), 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setUI(new DarkComboBoxUI());
        combo.setOpaque(true);
        combo.setBackground(INPUT_SURFACE);
        combo.setForeground(TEXT_WHITE);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        combo.setBorder(new CompoundBorder(
                new LineBorder(getBorder(), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));

        // High-contrast dark popup cell renderer (Never white, never blue)
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (index == -1) {
                    l.setBackground(INPUT_SURFACE);
                    l.setForeground(TEXT_WHITE);
                } else if (isSelected) {
                    l.setBackground(getPrimaryAccent());
                    l.setForeground(getButtonTextColor());
                } else {
                    l.setBackground(INPUT_SURFACE);
                    l.setForeground(TEXT_WHITE);
                }
                return l;
            }
        });
    }

    public static void styleCard(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(getCard());
        panel.setBorder(new CompoundBorder(
                new LineBorder(getBorder(), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new LineBorder(getBorder(), 1));
        scrollPane.getViewport().setBackground(getBackground());
        scrollPane.setBackground(getBackground());

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 50, 50);
                this.trackColor = getSidebar();
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // =====================================================
    // PROFESSIONAL TABLE STYLING WITH BADGES & HOVER
    // =====================================================

    public static void styleTable(JTable table) {
        table.setOpaque(true);
        table.setBackground(getCard());
        table.setForeground(TEXT_WHITE);
        table.setGridColor(new Color(38, 38, 38)); // Strictly Zero Blue!
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setSelectionBackground(getAccentDark());
        table.setSelectionForeground(TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(getSidebar());
        header.setForeground(getPrimaryAccent());
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(new MatteBorder(0, 0, 2, 0, getPrimaryAccent()));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));

        // Enforce dark table header renderer (avoids native Windows silver gradient)
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setOpaque(true);
                l.setBackground(getSidebar());
                l.setForeground(getPrimaryAccent());
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 2, 0, getPrimaryAccent()),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
    }

    /**
     * Advanced Table Cell Renderer with High-Contrast Text and Status Pills
     */
    public static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            String text = value != null ? value.toString().trim() : "";

            if (!isSelected) {
                if (row % 2 == 0) {
                    label.setBackground(getCard());
                } else {
                    label.setBackground(new Color(
                            Math.max(0, getCard().getRed() - 4),
                            Math.max(0, getCard().getGreen() - 4),
                            Math.max(0, getCard().getBlue() - 4)
                    ));
                }
                label.setForeground(TEXT_WHITE);
            } else {
                label.setBackground(getAccentDark());
                label.setForeground(TEXT_WHITE);
            }

            // Custom Status Pill Highlights
            if ("APPROVED".equalsIgnoreCase(text) || "RESOLVED".equalsIgnoreCase(text)) {
                label.setText("●  " + text);
                if (!isSelected) label.setForeground(STATUS_APPROVED);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if ("PENDING".equalsIgnoreCase(text)) {
                label.setText("◐  " + text);
                if (!isSelected) label.setForeground(STATUS_PENDING);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if ("REJECTED".equalsIgnoreCase(text)) {
                label.setText("✕  " + text);
                if (!isSelected) label.setForeground(STATUS_REJECTED);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if ("LOST".equalsIgnoreCase(text)) {
                label.setText("▼  LOST");
                if (!isSelected) label.setForeground(STATUS_LOST);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if ("FOUND".equalsIgnoreCase(text)) {
                label.setText("▲  FOUND");
                if (!isSelected) label.setForeground(STATUS_FOUND); // Vivid Emerald, NEVER blue!
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (text.startsWith("📷")) {
                if (!isSelected) label.setForeground(getPrimaryAccent());
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            return label;
        }
    }

    // =====================================================
    // DARK DIALOG HELPERS (ELIMINATES LIGHT GRAY JOPTIONPANE)
    // =====================================================

    public static void showMessage(Component parent, String message, String title, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        styleDialogComponents(pane);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(getCard());
        styleDeepComponents(dialog);
        dialog.setVisible(true);
    }

    public static int showConfirm(Component parent, String message, String title, int optionType) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType);
        styleDialogComponents(pane);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(getCard());
        styleDeepComponents(dialog);
        dialog.setVisible(true);

        Object val = pane.getValue();
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    private static void styleDialogComponents(JOptionPane pane) {
        pane.setOpaque(true);
        pane.setBackground(getCard());
        pane.setForeground(TEXT_WHITE);
    }

    private static void styleDeepComponents(Component c) {
        if (c instanceof JPanel || c instanceof JOptionPane) {
            c.setBackground(getCard());
            c.setForeground(TEXT_WHITE);
        } else if (c instanceof JLabel) {
            c.setForeground(TEXT_WHITE);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        } else if (c instanceof JButton) {
            styleSecondaryButton((JButton) c);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                styleDeepComponents(child);
            }
        }
    }

    // =====================================================
    // PERSISTENCE (SAVE / LOAD PREFERRED THEME)
    // =====================================================

    private static void saveTheme(ThemeMode mode) {
        try {
            File file = new File(CONFIG_FILE);
            file.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("theme=" + mode.name());
            }
        } catch (Exception ignored) {
        }
    }

    private static void loadSavedTheme() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("theme=")) {
                    String name = line.substring(6).trim();
                    currentMode = ThemeMode.valueOf(name);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
