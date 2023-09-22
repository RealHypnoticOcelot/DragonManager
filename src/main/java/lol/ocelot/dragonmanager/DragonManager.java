package lol.ocelot.dragonmanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.BorderFactory.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;

public class DragonManager extends JFrame {
    private JMenuBar createMenu() {
        // Create Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // Add Menu
        JMenu fileSelect = new JMenu("File");

        // Add Items to Menu
        JMenuItem newChar = new JMenuItem("New Character...");
        newChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        fileSelect.add(newChar);

        JMenuItem loadChar = new JMenuItem("Load Character...");
        loadChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        fileSelect.add(loadChar);

        menuBar.add(fileSelect);
        return menuBar;
    }

    private JLabel createText(String caption, GridBagConstraints constraints) {
        JLabel t = new JLabel(caption);
        getContentPane().add(t, constraints);
        return t;
    }
    private JButton createButton(String caption, GridBagConstraints constraints) {
        JButton b = new JButton(caption);
        b.setActionCommand(caption);
        getContentPane().add(b, constraints);
        return b;
    }

    public DragonManager() {
        super("DragonManager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Set Icon
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Taskbar.getTaskbar().setIconImage(ImageIO.read(getClass().getResourceAsStream("/DragonManager.png"))); // set icon image if on macos
            } else {
                setIconImage(ImageIO.read(getClass().getResourceAsStream("/DragonManager.png"))); // set icon image if not on macos
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create and set Menu Bar
        JMenuBar menu = createMenu();
        setJMenuBar(menu);

        JLabel welcomeText = createText("Welcome to DragonManager!", c);
        // Grid offset of the following object.
        c.gridy = 1;
        JButton newCharBtn = createButton("New Character", c);
        c.gridy = 2;
        JButton loadCharBtn = createButton("Load Character", c);

        // Set size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = (int) (screenSize.height * 0.75); // get height and width of the screen and make it 3/4
        int screenWidth = (int) (screenSize.width * 0.75);
        setSize(new Dimension(screenWidth, screenHeight));

        // Display Window
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.setProperty("apple.laf.useScreenMenuBar", "true"); // If on macOS make menu bar show up top
                new DragonManager();
            }
        });
    }
}