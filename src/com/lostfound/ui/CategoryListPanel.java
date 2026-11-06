package com.lostfound.ui;

import javax.swing.*;
import java.awt.*;

public class CategoryListPanel extends JPanel {

    private JList<String> categoryList;

    public CategoryListPanel() {

        setLayout(
                new BorderLayout(10, 10)
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );


        JLabel title =
                new JLabel(
                        "ITEM CATEGORIES",
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


        String[] categories = {
                "Electronics",
                "Documents",
                "Wallet",
                "Keys",
                "Clothing",
                "Jewellery",
                "Books",
                "Bags",
                "Other"
        };


        categoryList =
                new JList<>(
                        categories
                );


        categoryList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        categoryList.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );


        JScrollPane scrollPane =
                new JScrollPane(
                        categoryList
                );


        add(
                scrollPane,
                BorderLayout.CENTER
        );


        JButton showButton =
                new JButton(
                        "SHOW SELECTED CATEGORY"
                );


        showButton.addActionListener(
                e -> {

                    String selected =
                            categoryList
                                    .getSelectedValue();


                    if (selected == null) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Please select a category."
                        );

                        return;
                    }


                    JOptionPane.showMessageDialog(
                            this,
                            "Selected category: "
                                    + selected
                    );
                }
        );


        add(
                showButton,
                BorderLayout.SOUTH
        );
    }
}