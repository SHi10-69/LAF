import javax.swing.*;
import java.awt.*;

public class TestDialogTree {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Color dark = new Color(22, 22, 22);
        UIManager.put("OptionPane.background", dark);
        UIManager.put("Panel.background", dark);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Label.foreground", Color.WHITE);

        JDialog d = new JOptionPane("Test Message", JOptionPane.INFORMATION_MESSAGE).createDialog("Test Title");
        printTree(d, "  ");
    }

    static void printTree(Component c, String indent) {
        System.out.println(indent + c.getClass().getName() + " bg=" + c.getBackground() + " fg=" + c.getForeground());
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                printTree(child, indent + "  ");
            }
        }
    }
}
