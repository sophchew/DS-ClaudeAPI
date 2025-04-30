import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class FileSelectorUI extends JFrame {
    private JButton selectButton;
    private JLabel fileLabel;
    private static File file;

    public FileSelectorUI() {
        setTitle("Simple File Selector");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        selectButton = new JButton("Choose File");
        selectButton.setBounds(30, 30, 120, 30);
        add(selectButton);

        fileLabel = new JLabel("No file selected");
        fileLabel.setBounds(160, 30, 200, 30);
        add(fileLabel);

        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(FileSelectorUI.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileLabel.setText(file.getAbsolutePath());
                    try {
                        ClaudeAPI.toQTIQuiz(file);
                    } catch (IOException | ParserConfigurationException | SAXException | TransformerException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    fileLabel.setText("No file selected");
                }
            }
        });
    }

    public static File getFile() {
        return file;
    }
}
