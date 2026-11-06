package com.lostfound.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageUtil {

    public static final String PROOF_DIR = "data/claim_proofs";
    public static final String ITEM_IMG_DIR = "data/item_images";

    static {
        new File(PROOF_DIR).mkdirs();
        new File(ITEM_IMG_DIR).mkdirs();
    }

    private ImageUtil() {}

    /**
     * Safely copy an uploaded verification proof image to the local data/claim_proofs folder.
     */
    public static String saveProofImage(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        try {
            File dir = new File(PROOF_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalName = sourceFile.getName();
            String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = "proof_" + System.currentTimeMillis() + "_" + cleanName;
            File dest = new File(dir, fileName);
            Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getPath().replace('\\', '/');
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Safely copy an uploaded item image to data/item_images folder.
     */
    public static String saveItemImage(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        try {
            File dir = new File(ITEM_IMG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalName = sourceFile.getName();
            String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = "item_" + System.currentTimeMillis() + "_" + cleanName;
            File dest = new File(dir, fileName);
            Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getPath().replace('\\', '/');
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load an image scaled proportionally to fit within maxWidth and maxHeight.
     */
    public static ImageIcon loadScaledIcon(String path, int maxWidth, int maxHeight) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            BufferedImage orig = ImageIO.read(file);
            if (orig == null) {
                return null;
            }
            int origW = orig.getWidth();
            int origH = orig.getHeight();

            double scale = Math.min((double) maxWidth / origW, (double) maxHeight / origH);
            int newW = Math.max(1, (int) Math.round(origW * scale));
            int newH = Math.max(1, (int) Math.round(origH * scale));

            BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(orig, 0, 0, newW, newH, null);
            g2.dispose();

            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Open image in system default image viewer.
     */
    public static void openInSystemViewer(String path, Component parent) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No image file specified.", "Image Viewer", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(parent, "Image file not found:\n" + path, "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(parent, "Desktop viewer not supported. File path:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Could not open system viewer: " + e.getMessage(), "Viewer Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show a sleek full-resolution modal preview dialog.
     */
    public static void showFullResolutionViewer(Window owner, String path, String title) {
        if (path == null || !new File(path).exists()) {
            JOptionPane.showMessageDialog(owner, "Image file does not exist.", "Image Viewer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog viewerDialog = new JDialog(owner, "Image Verification - " + (title != null ? title : "Proof View"), Dialog.ModalityType.APPLICATION_MODAL);
        viewerDialog.setSize(750, 620);
        viewerDialog.setLocationRelativeTo(owner);
        viewerDialog.getContentPane().setBackground(Theme.getBackground());
        viewerDialog.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.getCard());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("AUTHENTICATION PROOF INSPECTION");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Theme.getPrimaryAccent());

        JLabel pathLbl = new JLabel(new File(path).getName());
        pathLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pathLbl.setForeground(Theme.getTextMuted());

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(pathLbl, BorderLayout.EAST);
        viewerDialog.add(headerPanel, BorderLayout.NORTH);

        // Image in center with scroll pane
        ImageIcon icon = loadScaledIcon(path, 700, 480);
        JLabel imageLabel;
        if (icon != null) {
            imageLabel = new JLabel(icon, SwingConstants.CENTER);
        } else {
            imageLabel = new JLabel("Unable to render image at path: " + path, SwingConstants.CENTER);
            imageLabel.setForeground(Theme.getTextMuted());
        }
        imageLabel.setBackground(Theme.getBackground());
        imageLabel.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.getBorder(), 1));
        scrollPane.getViewport().setBackground(Theme.getBackground());
        viewerDialog.add(scrollPane, BorderLayout.CENTER);

        // Bottom toolbar
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(Theme.getCard());

        JButton openExternalBtn = new JButton("Open in System Viewer");
        Theme.styleSecondaryButton(openExternalBtn);
        openExternalBtn.addActionListener(e -> openInSystemViewer(path, viewerDialog));

        JButton closeBtn = new JButton("Close View");
        Theme.stylePrimaryButton(closeBtn);
        closeBtn.addActionListener(e -> viewerDialog.dispose());

        footerPanel.add(openExternalBtn);
        footerPanel.add(closeBtn);
        viewerDialog.add(footerPanel, BorderLayout.SOUTH);

        viewerDialog.setVisible(true);
    }
}
