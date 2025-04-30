import javax.swing.*;

public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FileSelectorUI().setVisible(true);
        });

    }
}