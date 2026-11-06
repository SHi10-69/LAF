package com.lostfound;

import com.lostfound.ui.LoginFrame;
import com.lostfound.util.Theme;
import com.lostfound.web.WebServer;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // High DPI & text anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Start integrated web application server in background
        WebServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(WebServer::stop));

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                // Fallback to cross-platform if system L&F fails
            }

            // Apply NVIDIA / Netflix theme styling
            Theme.applyGlobalDefaults();

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
