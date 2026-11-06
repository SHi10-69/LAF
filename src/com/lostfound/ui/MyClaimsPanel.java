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

public class MyClaimsPanel extends JPanel implements Theme.ThemeChangeListener {

    private final User loggedInUser;
    private final ClaimService claimService;
    private final ItemService itemService;

    private JTable claimsTable;
    private DefaultTableModel tableModel;

    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JButton viewButton;
    private JButton refreshButton;
    private JPanel buttonPanel;

    public MyClaimsPanel(User loggedInUser) {
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

        headingLabel = new JLabel("MY SUBMITTED CLAIMS");
        Theme.styleBrandHeading(headingLabel);

        subtitleLabel = new JLabel(
                "Track all claims you have submitted, verification proof, and current administrative status."
        );
        Theme.styleLabel(subtitleLabel);

        headerPanel.add(headingLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {
                "Claim ID",
                "Item ID",
                "Found Item Name",
                "Date Submitted",
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

        claimsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && claimsTable.getSelectedRow() != -1) {
                    viewClaimDetails();
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

        viewButton = new JButton("🔍 View Claim Details");
        refreshButton = new JButton("↻ Refresh");

        viewButton.addActionListener(e -> viewClaimDetails());
        refreshButton.addActionListener(e -> loadClaims());

        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(refreshButton);

        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadClaims() {
        tableModel.setRowCount(0);
        List<Claim> userClaims = claimService.getClaimsByUsername(loggedInUser.getUsername());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        for (Claim claim : userClaims) {
            Item item = null;
            for (Item currentItem : itemService.getAllItems()) {
                if (currentItem.getId() == claim.getItemId()) {
                    item = currentItem;
                    break;
                }
            }

            String itemName = item != null ? item.getItemName() : "Item not found";
            String claimDate = claim.getClaimDate() != null ? dateFormat.format(claim.getClaimDate()) : "Not available";
            boolean hasImage = claim.getImagePath() != null
                    && !claim.getImagePath().trim().isEmpty()
                    && new File(claim.getImagePath()).exists();

            tableModel.addRow(new Object[]{
                    claim.getId(),
                    claim.getItemId(),
                    itemName,
                    claimDate,
                    hasImage ? "📷 Attached" : "— None",
                    claim.getClaimStatus()
            });
        }
    }

    private void viewClaimDetails() {
        int selectedRow = claimsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = claimsTable.convertRowIndexToModel(selectedRow);
        int claimId = (int) tableModel.getValueAt(modelRow, 0);

        Claim selectedClaim = null;
        for (Claim claim : claimService.getClaimsByUsername(loggedInUser.getUsername())) {
            if (claim.getId() == claimId) {
                selectedClaim = claim;
                break;
            }
        }
        if (selectedClaim == null) return;
        final Claim finalClaim = selectedClaim;

        Item item = null;
        for (Item i : itemService.getAllItems()) {
            if (i.getId() == selectedClaim.getItemId()) {
                item = i;
                break;
            }
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Claim Details - #" + selectedClaim.getId(),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.getBackground());
        dialog.setLayout(new BorderLayout(12, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.getCard());
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("CLAIM #" + selectedClaim.getId() + " STATUS REVIEW");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Theme.getPrimaryAccent());

        JLabel statusBadge = new JLabel("STATUS: " + selectedClaim.getClaimStatus());
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if ("APPROVED".equalsIgnoreCase(selectedClaim.getClaimStatus())) {
            statusBadge.setForeground(Theme.STATUS_APPROVED);
        } else if ("REJECTED".equalsIgnoreCase(selectedClaim.getClaimStatus())) {
            statusBadge.setForeground(Theme.STATUS_REJECTED);
        } else {
            statusBadge.setForeground(Theme.STATUS_PENDING);
        }

        header.add(title, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // Center: Details and Image
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.getCard());
        content.setBorder(new CompoundBorder(
                new LineBorder(Theme.getBorder(), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        addDetailRow(content, "Target Item:", item != null ? item.getItemName() + " (ID #" + item.getId() + ")" : "ID #" + selectedClaim.getItemId());
        addDetailRow(content, "Claimant:", selectedClaim.getClaimantName() + " (@" + selectedClaim.getClaimantUsername() + ")");
        addDetailRow(content, "Contact:", selectedClaim.getContact());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        addDetailRow(content, "Date Submitted:", selectedClaim.getClaimDate() != null ? df.format(selectedClaim.getClaimDate()) : "—");

        content.add(Box.createVerticalStrut(10));
        JLabel proofLbl = new JLabel("Ownership Proof Description:");
        proofLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        proofLbl.setForeground(Theme.getTextMuted());
        content.add(proofLbl);
        content.add(Box.createVerticalStrut(4));

        JTextArea proofText = new JTextArea(selectedClaim.getProof(), 3, 20);
        proofText.setEditable(false);
        proofText.setLineWrap(true);
        proofText.setWrapStyleWord(true);
        proofText.setBackground(Theme.getBackground());
        proofText.setForeground(Theme.getTextPrimary());
        proofText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        proofText.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane proofScroll = new JScrollPane(proofText);
        Theme.styleScrollPane(proofScroll);
        proofScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        proofScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(proofScroll);

        // Verification photo section
        content.add(Box.createVerticalStrut(10));
        String imgPath = selectedClaim.getImagePath();
        boolean hasImg = imgPath != null && !imgPath.trim().isEmpty() && new File(imgPath).exists();

        if (hasImg) {
            JLabel photoLbl = new JLabel("Uploaded Verification Proof Image:");
            photoLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            photoLbl.setForeground(Theme.getPrimaryAccent());
            content.add(photoLbl);
            content.add(Box.createVerticalStrut(4));

            JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            imgPanel.setBackground(Theme.getCard());

            ImageIcon thumb = ImageUtil.loadScaledIcon(imgPath, 120, 80);
            JLabel thumbLbl = new JLabel(thumb != null ? thumb : null);
            thumbLbl.setBorder(new LineBorder(Theme.getBorder(), 1, true));
            imgPanel.add(thumbLbl);

            JButton viewImgBtn = new JButton("🔍 View Full Image");
            Theme.styleSecondaryButton(viewImgBtn);
            final String finalImgPath = imgPath;
            viewImgBtn.addActionListener(e -> ImageUtil.showFullResolutionViewer(dialog, finalImgPath, "Claim #" + finalClaim.getId()));
            imgPanel.add(viewImgBtn);

            imgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(imgPanel);
        } else {
            JLabel noImg = new JLabel("ℹ No verification photo proof attached for this claim.");
            noImg.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noImg.setForeground(Theme.getTextMuted());
            noImg.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(noImg);
        }

        dialog.add(content, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Theme.getCard());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.getBorder()));

        JButton closeBtn = new JButton("Close");
        Theme.stylePrimaryButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
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

    @Override
    public void onThemeChanged(Theme.ThemeMode newMode) {
        Theme.stylePanel(this);
        Theme.styleBrandHeading(headingLabel);
        Theme.styleLabel(subtitleLabel);
        Theme.styleTable(claimsTable);
        Theme.stylePrimaryButton(viewButton);
        Theme.styleSecondaryButton(refreshButton);
        buttonPanel.setBackground(Theme.getBackground());
        repaint();
    }
}
