package lol.ocelot.dragonmanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.*;
import com.formdev.flatlaf.*;


public class DragonManager extends JFrame implements ActionListener {

    // Get config file, or create if there isn't one
    public static Preferences prefs;
    static {
        prefs = Preferences.userRoot().node(DragonManager.class.getName());
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

        JMenuItem resetBG = new JMenuItem("Reset Background");
        resetBG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        resetBG.setActionCommand("resetBG");
        resetBG.addActionListener(this);
        viewMenu.add(resetBG);

        JMenuItem themeToggle = new JMenuItem("Change Theme");
        themeToggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        themeToggle.setActionCommand("themeToggle");
        themeToggle.addActionListener(this);
        viewMenu.add(themeToggle);

        // Add Menu Items to Menubar
        menuBar.add(fileSelect);
        menuBar.add(viewMenu);
        return menuBar;
    }

    public void setBackground() {
        if (!(prefs.get("wallpaper", null) == null)) {
            try {
                this.setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File(prefs.get("wallpaper", null))))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
            filePick.addChoosableFileFilter(new ImagesPicker());
            filePick.setAcceptAllFileFilterUsed(false);
            // Open the dialogue
            int returnVal = filePick.showOpenDialog(DragonManager.this);
            // if the user selected a file and didn't cancel
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filePick.getSelectedFile();
                prefs.put("wallpaper", file.getAbsolutePath());
                setBackground();
            }
        } else if ("resetBG".equals(e.getActionCommand())) {
                prefs.remove("wallpaper");
        } else if ("themeToggle".equals(e.getActionCommand())) {
            try {
                if (UIManager.getLookAndFeel().getName().equals("FlatLaf Light")) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    prefs.put("theme", "dark");
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    prefs.put("theme", "light");
                }
                SwingUtilities.updateComponentTreeUI(DragonManager.getFrames()[0]);
            } catch (UnsupportedLookAndFeelException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // File picker set to only accept images
    class ImagesPicker extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                return extension.equals("jpeg") ||
                        extension.equals("jpg") ||
                        extension.equals("png");
            }
            return false;
        }

        @Override
        public String getDescription() {
            return ".jpg, .jpeg, .png";
        }

        String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
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
        setBackground();
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

        c.insets = new Insets(3, 0,3,0);
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
                try {
                    if (prefs.get("theme", null) == "dark") { // use == because it can be null
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                    } else {
                        UIManager.setLookAndFeel(new FlatLightLaf());
                    }
                } catch( Exception ex ) {
                    System.err.println( "Failed to initialize LaF, " + ex);
                }
                System.setProperty("apple.laf.useScreenMenuBar", "true"); // If on macOS make menu bar show up top
                new DragonManager();
            }
        });
    }
}
