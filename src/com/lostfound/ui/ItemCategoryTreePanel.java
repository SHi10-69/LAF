package com.lostfound.ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ItemCategoryTreePanel extends JPanel {

    private JTree categoryTree;

    public ItemCategoryTreePanel() {

        setLayout(
                new BorderLayout(10, 10)
        );


        JLabel title =
                new JLabel(
                        "ITEM CATEGORY TREE",
                        SwingConstants.CENTER
                );


        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );


        add(
                title,
                BorderLayout.NORTH
        );


        // =====================================
        // ROOT
        // =====================================

        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(
                        "Lost & Found"
                );


        // Electronics

        DefaultMutableTreeNode electronics =
                new DefaultMutableTreeNode(
                        "Electronics"
                );


        electronics.add(
                new DefaultMutableTreeNode(
                        "Mobile Phone"
                )
        );


        electronics.add(
                new DefaultMutableTreeNode(
                        "Laptop"
                )
        );


        electronics.add(
                new DefaultMutableTreeNode(
                        "Headphones"
                )
        );


        // Documents

        DefaultMutableTreeNode documents =
                new DefaultMutableTreeNode(
                        "Documents"
                );


        documents.add(
                new DefaultMutableTreeNode(
                        "ID Card"
                )
        );


        documents.add(
                new DefaultMutableTreeNode(
                        "Passport"
                )
        );


        documents.add(
                new DefaultMutableTreeNode(
                        "Certificate"
                )
        );


        // Personal

        DefaultMutableTreeNode personal =
                new DefaultMutableTreeNode(
                        "Personal Items"
                );


        personal.add(
                new DefaultMutableTreeNode(
                        "Wallet"
                )
        );


        personal.add(
                new DefaultMutableTreeNode(
                        "Keys"
                )
        );


        personal.add(
                new DefaultMutableTreeNode(
                        "Bag"
                )
        );


        root.add(electronics);

        root.add(documents);

        root.add(personal);


        categoryTree =
                new JTree(root);


        JScrollPane scrollPane =
                new JScrollPane(
                        categoryTree
                );


        add(
                scrollPane,
                BorderLayout.CENTER
        );


        JButton button =
                new JButton(
                        "SHOW SELECTED"
                );


        button.addActionListener(
                e -> showSelectedNode()
        );


        add(
                button,
                BorderLayout.SOUTH
        );
    }


    private void showSelectedNode() {

        Object selected =
                categoryTree
                        .getLastSelectedPathComponent();


        if (selected == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a category."
            );

            return;
        }


        JOptionPane.showMessageDialog(
                this,
                "Selected: "
                        + selected
        );
    }
}