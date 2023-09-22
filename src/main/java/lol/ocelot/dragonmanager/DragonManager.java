package lol.ocelot.dragonmanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.prefs.*;
import java.util.Properties;
import javax.imageio.ImageIO;

public class DragonManager extends JFrame implements ActionListener {

    public static FileInputStream config;

    static {
        try {
            Path configpath = Paths.get(DragonManager.class.getResource("/").getPath());
            File configfile = new File(configpath + "/" + "config.properties");
            configfile.createNewFile();
            config = new FileInputStream(String.valueOf(DragonManager.class.getResourceAsStream("config.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JMenuBar createMenu() {
        // Create Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // Add Menu
        JMenu fileSelect = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        // Add Items to File Menu
        JMenuItem newChar = new JMenuItem("New Character...");
        newChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        newChar.setActionCommand("newChar");
        newChar.addActionListener(this);
        fileSelect.add(newChar);

        JMenuItem loadChar = new JMenuItem("Load Character...");
        loadChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        loadChar.setActionCommand("loadChar");
        loadChar.addActionListener(this);
        fileSelect.add(loadChar);

        JMenuItem saveChar = new JMenuItem("Save Character");
        saveChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        saveChar.setActionCommand("saveChar");
        saveChar.addActionListener(this);
        fileSelect.add(saveChar);

        // Add Items to View Menu
        JMenuItem changeBG = new JMenuItem("Change Background");
        changeBG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        changeBG.setActionCommand("changeBG");
        changeBG.addActionListener(this);
        viewMenu.add(changeBG);

        menuBar.add(fileSelect);
        menuBar.add(viewMenu);
        return menuBar;
    }

    // All Actions from buttons and menus
    public void actionPerformed(ActionEvent e) {
        if ("newChar".equals(e.getActionCommand())) {
            System.out.println("newCharacter");
        } else if ("loadChar".equals(e.getActionCommand())) {
            System.out.println("loadCharacter");
        } else if ("saveChar".equals(e.getActionCommand())) {
            System.out.println("saveCharacter");
        } else if ("changeBG".equals(e.getActionCommand())) {
            //Create File Chooser Object
            JFileChooser filePick = new JFileChooser();
            // Open the dialogue
            int returnVal = filePick.showOpenDialog(DragonManager.this);
            // if the user selected a file and didn't cancel
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filePick.getSelectedFile();
                file.getAbsolutePath();
            }
        }
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

        createText("Welcome to DragonManager!", c);
        c.gridy = 1;
        JButton newCharBtn = createButton("New Character", c);
        newCharBtn.setActionCommand("newChar");
        newCharBtn.addActionListener(this);
        c.gridy = 2;
        JButton loadCharBtn = createButton("Load Character", c);
        loadCharBtn.setActionCommand("loadChar");
        loadCharBtn.addActionListener(this);

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