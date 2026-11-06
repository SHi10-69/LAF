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
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class ManageClaimsPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ClaimService claimService;
    private final ItemService itemService;

    private JTable claimsTable;
    private DefaultTableModel tableModel;

    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JButton viewButton;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JPanel buttonPanel;

    public ManageClaimsPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.claimService = new ClaimService();
        this.itemService = new ItemService();

        setLayout(new BorderLayout(12, 12));
        Theme.stylePanel(this);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        createHeading();
        createTable();
        createButtons();

        Theme.addThemeChangeListener(this);
        loadClaims();
    }

    private void createHeading() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(Theme.getBackground());

        headingLabel = new JLabel("CLAIMS VERIFICATION & AUTHENTICATION");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Review user ownership claims, inspect attached photo verification proof, and authenticate or reject claims."
        );
        Theme.styleLabel(subtitleLabel);

        headerPanel.add(headingLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {
                "Claim ID",
                "FOUND Item",
                "LOST Item",
                "Claimant",
                "Contact",
                "Date",
                "Photo Proof",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        claimsTable = new JTable(tableModel);
        claimsTable.setRowHeight(34);
        claimsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        claimsTable.setAutoCreateRowSorter(true);

        Theme.styleTable(claimsTable);

        // Double-click to view proof and verify
        claimsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && claimsTable.getSelectedRow() != -1) {
                    viewClaimVerification();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(claimsTable);
        Theme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtons() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(Theme.getBackground());

        viewButton = new JButton("🔍 View Proof & Authenticate");
        approveButton = new JButton("✓ Quick Approve");
        rejectButton = new JButton("✕ Quick Reject");
        refreshButton = new JButton("↻ Refresh");

        viewButton.addActionListener(e -> viewClaimVerification());
        approveButton.addActionListener(e -> updateSelectedClaim("APPROVED"));
        rejectButton.addActionListener(e -> updateSelectedClaim("REJECTED"));
        refreshButton.addActionListener(e -> loadClaims());

        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(approveButton);
        Theme.styleDangerButton(rejectButton);
        Theme.styleSecondaryButton(refreshButton);

        buttonPanel.add(viewButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadClaims() {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        for (Claim claim : claimService.getAllClaims()) {
            Item foundItem = findItem(claim.getItemId());
            Item lostItem = claim.getLostItemId() > 0 ? findItem(claim.getLostItemId()) : null;

            String foundDesc = foundItem != null
                    ? "#" + foundItem.getId() + " - " + foundItem.getItemName()
                    : "#" + claim.getItemId() + " (Unknown)";

            String lostDesc = lostItem != null
                    ? "#" + lostItem.getId() + " - " + lostItem.getItemName()
                    : (claim.getLostItemId() > 0 ? "#" + claim.getLostItemId() : "— None");

            String date = claim.getClaimDate() != null
                    ? dateFormat.format(claim.getClaimDate())
                    : "Not available";

            boolean hasImage = claim.getImagePath() != null
                    && !claim.getImagePath().trim().isEmpty()
                    && new File(claim.getImagePath()).exists();

            String imageBadge = hasImage ? "📷 Verified Attached" : "— None";

            tableModel.addRow(new Object[]{
                    claim.getId(),
                    foundDesc,
                    lostDesc,
                    claim.getClaimantName(),
                    claim.getContact(),
                    date,
                    imageBadge,
                    claim.getClaimStatus()
            });
        }
    }

    private Item findItem(int itemId) {
        for (Item item : itemService.getAllItems()) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }

    private Claim getSelectedClaim() {
        int selectedRow = claimsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int modelRow = claimsTable.convertRowIndexToModel(selectedRow);
        int claimId = (int) tableModel.getValueAt(modelRow, 0);

        for (Claim claim : claimService.getAllClaims()) {
            if (claim.getId() == claimId) {
                return claim;
            }
        }
        return null;
    }

    /**
     * Professional Verification & Authentication Modal Dialog
     */
    private void viewClaimVerification() {
        Claim claim = getSelectedClaim();
        if (claim == null) return;

        Item foundItem = findItem(claim.getItemId());
        Item lostItem = claim.getLostItemId() > 0 ? findItem(claim.getLostItemId()) : null;

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Claim Verification & Authentication - Claim #" + claim.getId(),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(800, 640);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.getBackground());
        dialog.setLayout(new BorderLayout(12, 12));

        // Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.getCard());
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("CLAIM #" + claim.getId() + " AUTHENTICATION DOSSIER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Theme.getPrimaryAccent());

        JLabel statusBadge = new JLabel("STATUS: " + claim.getClaimStatus());
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if ("APPROVED".equalsIgnoreCase(claim.getClaimStatus())) {
            statusBadge.setForeground(Theme.STATUS_APPROVED);
        } else if ("REJECTED".equalsIgnoreCase(claim.getClaimStatus())) {
            statusBadge.setForeground(Theme.STATUS_REJECTED);
        } else {
            statusBadge.setForeground(Theme.STATUS_PENDING);
        }

        header.add(title, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // Center Content: 2-column dossier
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 12, 12));
        centerGrid.setBackground(Theme.getBackground());
        centerGrid.setBorder(new EmptyBorder(0, 16, 0, 16));

        // Column 1: Details & Textual Proof
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setBackground(Theme.getCard());
        leftCol.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        addSectionTitle(leftCol, "CLAIMANT IDENTITY");
        addDetailRow(leftCol, "Claimant Name:", claim.getClaimantName());
        addDetailRow(leftCol, "Account Username:", claim.getClaimantUsername());
        addDetailRow(leftCol, "Contact Information:", claim.getContact());

        leftCol.add(Box.createVerticalStrut(10));
        addSectionTitle(leftCol, "MATCHED ITEMS COMPARISON");
        String foundInfo = foundItem != null
                ? foundItem.getItemName() + " (" + foundItem.getCategory() + " @ " + foundItem.getLocation() + ")"
                : "ID #" + claim.getItemId();
        addDetailRow(leftCol, "FOUND Item:", foundInfo);

        String lostInfo = lostItem != null
                ? lostItem.getItemName() + " (" + lostItem.getCategory() + " @ " + lostItem.getLocation() + ")"
                : (claim.getLostItemId() > 0 ? "ID #" + claim.getLostItemId() : "None linked by user");
        addDetailRow(leftCol, "Linked LOST Item:", lostInfo);

        leftCol.add(Box.createVerticalStrut(10));
        addSectionTitle(leftCol, "CLAIMANT OWNERSHIP PROOF");
        JTextArea proofDisplay = new JTextArea(claim.getProof(), 6, 20);
        proofDisplay.setEditable(false);
        proofDisplay.setLineWrap(true);
        proofDisplay.setWrapStyleWord(true);
        proofDisplay.setBackground(Theme.getBackground());
        proofDisplay.setForeground(Theme.getTextPrimary());
        proofDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        proofDisplay.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane proofScroll = new JScrollPane(proofDisplay);
        Theme.styleScrollPane(proofScroll);
        proofScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        proofScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(proofScroll);

        centerGrid.add(leftCol);

        // Column 2: Photo Verification Proof Section
        JPanel rightCol = new JPanel(new BorderLayout(8, 8));
        rightCol.setBackground(Theme.getCard());
        rightCol.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel rightColHeader = new JPanel(new BorderLayout());
        rightColHeader.setBackground(Theme.getCard());
        JLabel photoTitle = new JLabel("PHOTO VERIFICATION PROOF");
        photoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        photoTitle.setForeground(Theme.getPrimaryAccent());
        rightColHeader.add(photoTitle, BorderLayout.WEST);
        rightCol.add(rightColHeader, BorderLayout.NORTH);

        String imagePath = claim.getImagePath();
        boolean hasImage = imagePath != null && !imagePath.trim().isEmpty() && new File(imagePath).exists();

        if (hasImage) {
            JPanel imageHolder = new JPanel(new BorderLayout());
            imageHolder.setBackground(Theme.getBackground());
            imageHolder.setBorder(new LineBorder(Theme.getBorder(), 1));

            ImageIcon previewIcon = ImageUtil.loadScaledIcon(imagePath, 340, 310);
            JLabel imgLabel;
            if (previewIcon != null) {
                imgLabel = new JLabel(previewIcon, SwingConstants.CENTER);
            } else {
                imgLabel = new JLabel("Image load error", SwingConstants.CENTER);
                imgLabel.setForeground(Theme.getTextMuted());
            }
            imageHolder.add(imgLabel, BorderLayout.CENTER);
            rightCol.add(imageHolder, BorderLayout.CENTER);

            // Image Toolbar
            JPanel imgToolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            imgToolbar.setBackground(Theme.getCard());

            JButton fullResBtn = new JButton("🔍 Full View");
            JButton extViewerBtn = new JButton("🖥 External Viewer");
            Theme.styleSecondaryButton(fullResBtn);
            Theme.styleSecondaryButton(extViewerBtn);

            fullResBtn.addActionListener(e -> ImageUtil.showFullResolutionViewer(dialog, imagePath, "Claim #" + claim.getId()));
            extViewerBtn.addActionListener(e -> ImageUtil.openInSystemViewer(imagePath, dialog));

            imgToolbar.add(fullResBtn);
            imgToolbar.add(extViewerBtn);

            JLabel fileInfo = new JLabel(new File(imagePath).getName() + " (" + (new File(imagePath).length() / 1024) + " KB)", SwingConstants.CENTER);
            fileInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fileInfo.setForeground(Theme.getTextMuted());

            JPanel bottomImagePanel = new JPanel(new BorderLayout());
            bottomImagePanel.setBackground(Theme.getCard());
            bottomImagePanel.add(imgToolbar, BorderLayout.CENTER);
            bottomImagePanel.add(fileInfo, BorderLayout.SOUTH);

            rightCol.add(bottomImagePanel, BorderLayout.SOUTH);
        } else {
            JPanel noImagePanel = new JPanel(new GridBagLayout());
            noImagePanel.setBackground(Theme.getBackground());
            noImagePanel.setBorder(new LineBorder(Theme.getBorder(), 1));

            JPanel noImgContent = new JPanel();
            noImgContent.setLayout(new BoxLayout(noImgContent, BoxLayout.Y_AXIS));
            noImgContent.setBackground(Theme.getBackground());

            JLabel icon = new JLabel("📷", SwingConstants.CENTER);
            icon.setFont(new Font("Segoe UI", Font.PLAIN, 42));
            icon.setForeground(Theme.getTextMuted());
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel text1 = new JLabel("No Image Verification Attached", SwingConstants.CENTER);
            text1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            text1.setForeground(Theme.getTextPrimary());
            text1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel text2 = new JLabel("(Claimant submitted text proof only)", SwingConstants.CENTER);
            text2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            text2.setForeground(Theme.getTextMuted());
            text2.setAlignmentX(Component.CENTER_ALIGNMENT);

            noImgContent.add(icon);
            noImgContent.add(Box.createVerticalStrut(8));
            noImgContent.add(text1);
            noImgContent.add(Box.createVerticalStrut(4));
            noImgContent.add(text2);

            noImagePanel.add(noImgContent);
            rightCol.add(noImagePanel, BorderLayout.CENTER);
        }

        centerGrid.add(rightCol);
        dialog.add(centerGrid, BorderLayout.CENTER);

        // Footer Actions: Approve / Reject / Close
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Theme.getCard());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.getBorder()));

        JButton approveBtn = new JButton("✓ Authenticate & Approve");
        JButton rejectBtn = new JButton("✕ Reject Claim");
        JButton closeBtn = new JButton("Close Dossier");

        Theme.stylePrimaryButton(approveBtn);
        Theme.styleDangerButton(rejectBtn);
        Theme.styleSecondaryButton(closeBtn);

        boolean isPending = "PENDING".equalsIgnoreCase(claim.getClaimStatus());
        approveBtn.setEnabled(isPending);
        rejectBtn.setEnabled(isPending);

        approveBtn.addActionListener(e -> {
            dialog.dispose();
            updateSelectedClaim("APPROVED");
        });

        rejectBtn.addActionListener(e -> {
            dialog.dispose();
            updateSelectedClaim("REJECTED");
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        footer.add(rejectBtn);
        footer.add(approveBtn);
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addSectionTitle(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(Theme.getPrimaryAccent());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(Theme.getCard());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.getTextMuted());

        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(Theme.getTextPrimary());

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(3));
    }

    private void updateSelectedClaim(String newStatus) {
        Claim claim = getSelectedClaim();
        if (claim == null) return;

        if (!"PENDING".equalsIgnoreCase(claim.getClaimStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending claims can be approved or rejected.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String actionName = "APPROVED".equalsIgnoreCase(newStatus) ? "APPROVE and resolve the matched items" : "REJECT";
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + actionName + " for Claim #" + claim.getId() + "?",
                "Confirm Claim Verification Decision",
                JOptionPane.YES_NO_OPTION,
                "APPROVED".equalsIgnoreCase(newStatus) ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        boolean updated = claimService.updateClaimStatus(claim.getId(), newStatus);
        if (updated) {
            JOptionPane.showMessageDialog(
                    this,
                    "Claim #" + claim.getId() + " was successfully " + newStatus.toLowerCase() + "!"
                            + ("APPROVED".equalsIgnoreCase(newStatus) ? "\nThe associated FOUND item has been marked as RESOLVED." : ""),
                    "Status Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadClaims();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not update the claim status. Please verify the linked item statuses.",
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        Theme.stylePanel(this);
        Theme.styleBrandHeading(headingLabel);
        Theme.styleLabel(subtitleLabel);
        Theme.styleTable(claimsTable);
        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(approveButton);
        Theme.styleDangerButton(rejectButton);
        Theme.styleSecondaryButton(refreshButton);
        buttonPanel.setBackground(Theme.getBackground());
        repaint();
    }
}
