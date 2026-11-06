import javax.swing.*;
import java.awt.*;

public class TestJOptionPaneCustom {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Color bg = new Color(22, 22, 22);
        Color fg = Color.WHITE;
        UIManager.put("OptionPane.background", bg);
        UIManager.put("Panel.background", bg);
        UIManager.put("OptionPane.messageForeground", fg);
        UIManager.put("Label.foreground", fg);
        UIManager.put("Button.background", new Color(35, 35, 35));
        UIManager.put("Button.foreground", fg);

        // Also UIManager keys for JOptionPane
        UIManager.put("OptionPane.messageArea.background", bg);
        UIManager.put("OptionPane.realBody.background", bg);
        UIManager.put("OptionPane.separator.background", bg);

        JOptionPane pane = new JOptionPane("Test Message", JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog("Test");
        dialog.getContentPane().setBackground(bg);
        styleDeep(dialog, bg, fg);

        System.out.println("Dialog styled successfully!");
    }

    static void styleDeep(Component c, Color bg, Color fg) {
        if (c instanceof JPanel || c instanceof JOptionPane) {
            c.setBackground(bg);
        }
        if (c instanceof JLabel) {
            c.setForeground(fg);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                styleDeep(child, bg, fg);
            }
        }
    }
}
