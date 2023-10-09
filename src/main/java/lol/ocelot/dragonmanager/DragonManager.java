package lol.ocelot.dragonmanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.prefs.*;

import com.formdev.flatlaf.*;

import java.net.*;

import org.update4j.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.*;

public class DragonManager extends JFrame implements ActionListener {

    // Get config file, or create if there isn't one
    public static Preferences prefs = Preferences.userRoot().node(DragonManager.class.getName());
    // The JRadioButtonMenuItems
    public static JRadioButtonMenuItem bgScaled, bgTiled, bgActual;
    // BackgroundPanels that are tabs
    public static ArrayList<BackgroundPanel> bgTabs = new ArrayList<>();
    // Components that are only enabled when no character is loaded
    public static ArrayList<JComponent> noCharComponents = new ArrayList<>();
    public static DefaultListModel searchResultsArray = new DefaultListModel();

    public DragonManager() {
        super("DragonManager");
        // Default settings, like padding and exit behavior
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create tabbedpane for everything to be added to
        JTabbedPane tabbedPane = new JTabbedPane();

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

        // Create Main Menu and add it as a tab
        BackgroundPanel mainScreen = new BackgroundPanel(null, BackgroundPanel.SCALED, 0.0f, 0.0f);
        bgTabs.add(mainScreen);
        mainScreen.setLayout(new GridBagLayout());
        tabbedPane.addTab("Character", mainScreen);

        // Same as above but with the Search menu
        BackgroundPanel searchMenu = new BackgroundPanel(null, BackgroundPanel.SCALED, 0.0f, 0.0f);
        bgTabs.add(searchMenu);
        searchMenu.setLayout(new GridBagLayout());
        tabbedPane.addTab("Search", searchMenu);

        // Create search menu components

        // Search Filter
        String[] searchFilterList = {"All", "Spells", "Monsters", "Backgrounds", "Planes", "Feats", "Conditions", "Races", "Classes", "Magic Items", "Weapons", "Armor"};

        GridBagConstraints searchFilterConstraints = new GridBagConstraints();
        searchFilterConstraints.insets = new Insets(10, 3, 5, 10);
        searchFilterConstraints.fill = GridBagConstraints.HORIZONTAL;
        searchFilterConstraints.gridx = 1;
        searchFilterConstraints.weightx = 0.4;
        JComboBox searchFilter = new JComboBox(searchFilterList);

        // Search Box
        GridBagConstraints searchBoxConstraints = new GridBagConstraints();
        searchBoxConstraints.insets = new Insets(10, 10, 5, 3);
        searchBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
        searchBoxConstraints.weightx = 0.6;
        JTextField searchBox = new JTextField();
        searchBox.addActionListener(new SearchEvent(searchFilter));

        // Search Results
        GridBagConstraints searchResultConstraints = new GridBagConstraints();
        searchResultConstraints.insets = new Insets(5, 10, 3, 10);
        searchResultConstraints.gridy = 1;
        searchResultConstraints.fill = GridBagConstraints.BOTH;
        searchResultConstraints.weighty = 1;
        searchResultConstraints.weightx = 0.4;
        JList searchResultsList = new JList(searchResultsArray);
        searchResultsList.addListSelectionListener(new IndexChangeEvent(searchResultsList));
        JScrollPane resultsPane = new JScrollPane();
        resultsPane.setBorder(null);
        resultsPane.setViewportView(searchResultsList);

        // Result Data
        GridBagConstraints resultInfoConstraints = new GridBagConstraints();
        resultInfoConstraints.insets = new Insets(5, 10, 3, 10);
        resultInfoConstraints.gridy = 1;
        resultInfoConstraints.gridx = 1;
        resultInfoConstraints.weightx = 0.4;
        resultInfoConstraints.fill = GridBagConstraints.BOTH;
        JScrollPane resultInfoPane = new JScrollPane();
        resultInfoPane.setBorder(null);


        // Add in order
        searchMenu.add(searchBox, searchBoxConstraints);
        searchMenu.add(searchFilter, searchFilterConstraints);
        searchMenu.add(resultsPane, searchResultConstraints);
        searchMenu.add(resultInfoPane, resultInfoConstraints);

        // Add pane to frame and set background on tabs
        add(tabbedPane, BorderLayout.CENTER);
        setBackground(bgTabs);

        // Create and set Menu Bar
        JMenuBar menu = createMenu();
        setJMenuBar(menu);

        // Set size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = (int) (screenSize.height * 0.75); // get height and width of the screen and make it 3/4
        int screenWidth = (int) (screenSize.width * 0.75);
        mainScreen.setSize(new Dimension(screenWidth, screenHeight));
        searchMenu.setSize(new Dimension(screenWidth, screenHeight));
        setSize(new Dimension(screenWidth, screenHeight));

        // Display Window
        setVisible(true);
    }

    public static JSONObject getJson(URI url) {
        try {
            String json = IOUtils.toString(url, StandardCharsets.UTF_8);
            return new JSONObject(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Set theme for application if a preference is set
                try {
                    if (Objects.equals(prefs.get("theme", null), "dark")) { // use == because it can be null
                        UIManager.setLookAndFeel(new FlatDarkLaf());
                    } else {
                        UIManager.setLookAndFeel(new FlatLightLaf());
                    }
                    // Make things rounder
                    UIManager.put("Button.arc", 10);
                    UIManager.put("Component.arc", 10);
                    UIManager.put("ProgressBar.arc", 10);
                    UIManager.put("TextComponent.arc", 10);
                    //
                } catch (Exception ex) {
                    System.err.println("Failed to initialize LaF, " + ex);
                }
                System.setProperty("apple.laf.useScreenMenuBar", "true"); // If on macOS make menu bar show up top
                new DragonManager();
            }
        });
    }

    private boolean APIisReachable() {
        // Get whether open5e is available
        try {
            return (InetAddress.getByName("api.open5e.com").isReachable(1000));
        } catch (IOException e) {
            return (false);
        }
    }

    private JMenuBar createMenu() {
        // Create Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // Add Menu
        JMenu fileSelect = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        // Add Items to File Menu

        // Character Submenu
        JMenu charMenu = new JMenu("Character");
        fileSelect.add(charMenu);

        JMenuItem newChar = new JMenuItem("New Character...");
        newChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        newChar.setActionCommand("newChar");
        newChar.addActionListener(this);
        charMenu.add(newChar);

        JMenuItem loadChar = new JMenuItem("Load Character...");
        loadChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        loadChar.setActionCommand("loadChar");
        loadChar.addActionListener(this);
        charMenu.add(loadChar);


        // Add Items to View Menu
        // Background Submenu
        JMenu bgMenu = new JMenu("Background");
        viewMenu.add(bgMenu);

        JMenuItem changeBG = new JMenuItem("Change Background");
        changeBG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        changeBG.setActionCommand("changeBG");
        changeBG.addActionListener(this);
        bgMenu.add(changeBG);

        JMenuItem resetBG = new JMenuItem("Reset Background");
        resetBG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        resetBG.setActionCommand("resetBG");
        resetBG.addActionListener(this);
        bgMenu.add(resetBG);

        // Background Scaling Menu
        JMenu bgScaling = new JMenu("Scaling");
        bgMenu.add(bgScaling);

        bgScaled = new JRadioButtonMenuItem("Scaled");
        bgScaled.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        bgScaled.addActionListener(new ScalingEvent(0));
        bgScaled.setSelected(prefs.getInt("scalingType", 0) == 0);
        bgScaling.add(bgScaled);

        bgTiled = new JRadioButtonMenuItem("Tiled");
        bgTiled.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        bgTiled.addActionListener(new ScalingEvent(1));
        bgTiled.setSelected(prefs.getInt("scalingType", 0) == 1);
        bgScaling.add(bgTiled);

        bgActual = new JRadioButtonMenuItem("Actual Size");
        bgActual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        bgActual.addActionListener(new ScalingEvent(2));
        bgActual.setSelected(prefs.getInt("scalingType", 0) == 2);
        bgScaling.add(bgActual);

        JMenuItem themeToggle = new JMenuItem("Change Theme");
        themeToggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        themeToggle.setActionCommand("themeToggle");
        themeToggle.addActionListener(this);
        viewMenu.add(themeToggle);

        // Add Menu Items to Menubar
        menuBar.add(fileSelect);
        menuBar.add(viewMenu);
        return menuBar;
    }

    public void loadCharacter(JSONObject charInfo) {
        System.out.println(charInfo.get("genericRaceInfo"));
    }

    public void setBackground(ArrayList<BackgroundPanel> bgComponents) {
        if (!(prefs.get("wallpaper", null) == null)) {
            try {
                for (BackgroundPanel i : bgComponents) {
                    BufferedImage bgImage = ImageIO.read(new File(prefs.get("wallpaper", null)));
                    BufferedImage tintedImage = darkenImage(bgImage);
                    i.setImage(tintedImage);
                    int Scaling = prefs.getInt("scalingType", 0);
                    /*
                    0 = Scaled
                    1 = Tiled
                    2 = Actual Size
                    */
                    i.setStyle(Scaling);
                }

            } catch (IOException e) {
                prefs.remove("wallpaper");
                JOptionPane.showMessageDialog(null, "Can't access background image! Removing custom background.", "Error!", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e);
            }
        } else {
            for (BackgroundPanel i : bgComponents) {
                i.setImage(null);
            }
        }
    }

    // Int instead of bool because it's easier, 0 means don't add a None option and 1 means add one
    private Object[] namesAndData(JSONObject infoJSON, String key, int addNone) {
        JSONObject[] infoArray = new JSONObject[infoJSON.getJSONArray(key).length()]; // Create array that has space for however many values there are
        Object[] infoNames = new Object[infoJSON.getJSONArray(key).length() + addNone];
        if (addNone == 1) {
            infoNames[0] = "None";
        }

        for (int i = 0; i <= infoJSON.getJSONArray(key).length() - 1; i++) { // Subtract 1 because including zero
            infoArray[i] = infoJSON.getJSONArray(key).getJSONObject(i);
            infoNames[i + addNone] = infoArray[i].get("name");
        }
        return new Object[]{infoArray, infoNames};
    }

    // All Other Actions from buttons and menus
    public void actionPerformed(ActionEvent e) {
        if ("newChar".equals(e.getActionCommand())) {
            if (APIisReachable()) {
                JSONObject charInfo = new JSONObject();

                // Name
                String characterName;
                do {
                    characterName = JOptionPane.showInputDialog(null, "Enter Character Name:");
                    if (characterName == null) {
                        characterName = "";
                    }
                }
                while (characterName.isEmpty()); // Until the character's name isn't empty
                charInfo.put("name", characterName);

                JSONObject raceObject = null;
                try {
                    Object[] racesInfo = namesAndData(getJson(new URI("https://api.open5e.com/races/?format=json")), "results", 0);
                    JSONObject[] racesArray = (JSONObject[]) racesInfo[0];
                    Object[] racesNames = (Object[]) racesInfo[1];

                    // Select race and make sure it's not empty
                    Object race;
                    do {
                        race = JOptionPane.showInputDialog(null, "Choose Race:", "Input", JOptionPane.INFORMATION_MESSAGE, null, racesNames, racesNames[0]);
                        if (race != null) {
                            int raceIndex = ArrayUtils.indexOf(racesNames, race);
                            raceObject = racesArray[raceIndex]; // Convert from name to full character JSONObject
                        }
                    }
                    while (race == null);
                    charInfo.put("genericRaceInfo", raceObject); // All info about race

                    // If there's subraces, then select one
                    JSONObject subraceObject;
                    if (!raceObject.get("subraces").toString().equals("[]")) {
                        Object[] subraceInfo = namesAndData(raceObject, "subraces", 1);
                        JSONObject[] subraceArray = (JSONObject[]) subraceInfo[0];
                        Object[] subraceNames = (Object[]) subraceInfo[1];

                        Object subrace;
                        do {
                            subrace = JOptionPane.showInputDialog(null, "Choose Subrace:", "Input", JOptionPane.INFORMATION_MESSAGE, null, subraceNames, subraceNames[0]);
                            if (subrace != null) {
                                if (!subrace.equals("None")) {
                                    int subraceIndex = ArrayUtils.indexOf(subraceNames, subrace);
                                    subraceObject = subraceArray[subraceIndex - 1]; // Convert from name to full subrace JSONObject
                                    charInfo.put("genericSubraceInfo", subraceObject); // All info about race
                                } else {
                                    charInfo.put("genericSubraceInfo", "");
                                }
                            }
                        }
                        while (subrace == null);
                    }
                } catch (Exception ex) { // Generic just in case
                    JOptionPane.showMessageDialog(null, ex, "Error!", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }

                JSONObject classObject = null;
                try {
                    Object[] info = namesAndData(getJson(new URI("https://api.open5e.com/classes/?format=json")), "results", 0);
                    JSONObject[] classesArray = (JSONObject[]) info[0];
                    Object[] classesNames = (Object[]) info[1];
                    // Select class and make sure it's not empty
                    Object classes;
                    do {
                        classes = JOptionPane.showInputDialog(null, "Choose Class:", "Input", JOptionPane.INFORMATION_MESSAGE, null, classesNames, classesNames[0]);
                        if (classes != null) {
                            int raceIndex = ArrayUtils.indexOf(classesNames, classes);
                            classObject = classesArray[raceIndex]; // Convert from name to full character JSONObject
                        }
                    }
                    while (classes == null);
                    charInfo.put("genericClassInfo", classObject); // All info about race
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex, "Error!", JOptionPane.ERROR_MESSAGE);
                }

                Integer characterLevel;
                // If is valid integer and >0 then it passes
                do {
                    String characterLevelStr = JOptionPane.showInputDialog(null, "Enter Character Level:", 1);
                    try {
                        characterLevel = Integer.parseInt(characterLevelStr);
                    } catch (NumberFormatException nfe) {
                        // If not valid number set to zero so it doesn't pass
                        characterLevel = 0;
                    }
                }
                while (characterLevel <= 0);
                charInfo.put("level", characterLevel);


                Object alignment;
                Object[] alignmentList = {"Chaotic Evil", "Chaotic Good", "Chaotic Neutral", "Lawful Evil", "Lawful Good", "Lawful Neutral", "Neutral Evil", "Neutral", "Neutral Good"};
                do {
                    alignment = JOptionPane.showInputDialog(null, raceObject.get("alignment"), "Select Alignment", JOptionPane.INFORMATION_MESSAGE, null, alignmentList, alignmentList[0]);
                } while (alignment == null);
                charInfo.put("alignment", alignment);

                String campaignName = JOptionPane.showInputDialog(null, "Enter Campaign Name (Optional):");
                if (campaignName == null) {
                    campaignName = "";
                }
                charInfo.put("campaignName", campaignName);

                // Save to file
                JFileChooser saveLocation = new JFileChooser();
                saveLocation.setCurrentDirectory(new java.io.File("./characters"));
                saveLocation.setAcceptAllFileFilterUsed(false);
                saveLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                saveLocation.setDialogTitle("Folder to save character in");
                int returnVal = saveLocation.showOpenDialog(DragonManager.this);
                // if the user selected a file and didn't cancel
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        PrintWriter jsonFile = new PrintWriter(saveLocation.getSelectedFile() + "/" + characterName.toLowerCase().replace(" ", "_") + ".json", StandardCharsets.UTF_8);
                        jsonFile.write(charInfo.toString(4));
                        jsonFile.close();
                        JSONObject charJSON = new JSONObject(charInfo.toString(4));
                        loadCharacter(charJSON);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Cannot access API, unable to create characters!\nAre you connected to a stable wifi connection?", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("loadChar".equals(e.getActionCommand())) {
            JFileChooser saveLocation = new JFileChooser();
            saveLocation.setCurrentDirectory(new File("./characters"));
            saveLocation.setAcceptAllFileFilterUsed(false);
            saveLocation.addChoosableFileFilter(new jsonPicker());
            int returnVal = saveLocation.showOpenDialog(DragonManager.this);

            // if the user selected a file and didn't cancel
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    String charInfo = new String((Files.readAllBytes(saveLocation.getSelectedFile().toPath())));
                    JSONObject charJSON = new JSONObject(charInfo);
                    loadCharacter(charJSON);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if ("changeBG".equals(e.getActionCommand())) {
            //Create File Chooser Object
            JFileChooser filePick = new JFileChooser();
            filePick.addChoosableFileFilter(new ImagesPicker());
            filePick.setAcceptAllFileFilterUsed(false);
            filePick.setCurrentDirectory(new java.io.File("."));
            // Open the dialogue
            int returnVal = filePick.showOpenDialog(DragonManager.this);
            // if the user selected a file and didn't cancel
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filePick.getSelectedFile();
                prefs.put("wallpaper", file.getAbsolutePath());
                setBackground(bgTabs);
            }
        } else if ("resetBG".equals(e.getActionCommand())) {
            prefs.remove("wallpaper");
            setBackground(bgTabs);
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

    // Tint background image, taken from https://stackoverflow.com/questions/4248104/applying-a-tint-to-an-image-in-java
    public BufferedImage darkenImage(BufferedImage originalImage) {
        // Make 40% darker
        RescaleOp filter = new RescaleOp(.6f, 0, null);
        return filter.filter(originalImage, null);
    }

    private JLabel createText(BackgroundPanel panel, String caption, GridBagConstraints constraints) {
        JLabel t = new JLabel(caption);
        panel.add(t, constraints);
        return t;
    }

    private JButton createButton(BackgroundPanel panel, String caption, String actionCommand, GridBagConstraints constraints) {
        JButton b = new JButton(caption);
        b.setActionCommand(actionCommand);
        b.addActionListener(this);
        panel.add(b, constraints);
        return b;
    }

    private JTextField createTextField(BackgroundPanel panel, String actionCommand, GridBagConstraints constraints) {
        JTextField tf = new JTextField();
        tf.setActionCommand(actionCommand);
        tf.addActionListener(this);
        panel.add(tf, constraints);
        return tf;
    }

    // Actions from scaling menus
    private class ScalingEvent implements ActionListener {
        private final int ScaleValue;

        public ScalingEvent(int ScaleValue) {
            this.ScaleValue = ScaleValue;
        }

        public void actionPerformed(ActionEvent e) {
            prefs.putInt("scalingType", ScaleValue);

            // Spaghetti code to uncheck the other selections when another is selected
            if (ScaleValue == 0) {
                bgTiled.setSelected(false);
                bgActual.setSelected(false);
            } else if (ScaleValue == 1) {
                bgScaled.setSelected(false);
                bgActual.setSelected(false);
            } else if (ScaleValue == 2) {
                bgScaled.setSelected(false);
                bgTiled.setSelected(false);
            }
            setBackground(bgTabs);
        }
    }

    private class IndexChangeEvent implements ListSelectionListener {
        private final JList listObject;

        public IndexChangeEvent(JList listObject) {
            this.listObject = listObject;
        }

        public void valueChanged(ListSelectionEvent e) {
            System.out.println(listObject.getSelectedIndex());
        }
    }

    // Search action
    private class SearchEvent implements ActionListener {
        private final JComboBox searchFilter;
        private JProgressBar progressBar;

        public SearchEvent(JComboBox searchFilter) {
            this.searchFilter = searchFilter;
        }

        public void actionPerformed(ActionEvent e) {
            if (APIisReachable()) {
                Object[] searchFilterSlug = {"search", "spells", "monsters", "backgrounds", "planes", "feats", "conditions", "races", "classes", "magicitems", "weapons", "armor"};
                JTextField searchBox = (JTextField) e.getSource();

                JSONObject[] searchArray;
                Object[] searchNames;

                searchResultsArray.clear();
                searchBox.setEditable(false);
                // If using generic search instead of specific search
                try {
                    if (searchFilter.getSelectedIndex() == 0) {
                        Object[] searchResults = namesAndData(getJson(new URI("https://api.open5e.com/" + searchFilterSlug[searchFilter.getSelectedIndex()] + "?format=json&text=" + searchBox.getText())), "results", 0);
                        searchArray = (JSONObject[]) searchResults[0];
                        searchNames = (Object[]) searchResults[1];
                    } else {
                        Object[] searchResults = namesAndData(getJson(new URI("https://api.open5e.com/" + searchFilterSlug[searchFilter.getSelectedIndex()] + "?format=json&search=" + searchBox.getText())), "results", 0);
                        searchArray = (JSONObject[]) searchResults[0];
                        searchNames = (Object[]) searchResults[1];
                    }
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
                if (searchNames.length > 0) {
                    for (Object i : searchNames) {
                        searchResultsArray.addElement(i);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No Results Found!", "Error!", JOptionPane.ERROR_MESSAGE);
                }
                searchBox.setEditable(true);
            } else {
                JOptionPane.showMessageDialog(null, "Cannot access API, unable to use search!\nAre you connected to a stable wifi connection?", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Only accept json
    class jsonPicker extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                return extension.equals("json");
            }
            return false;
        }

        @Override
        public String getDescription() {
            return ".json";
        }

        String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    // File picker set to only accept images, copied and modified from https://www.tutorialspoint.com/swingexamples/show_file_chooser_images_only.htm
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
                        extension.equals("png") ||
                        extension.equals("webp");
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

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    // Set Background Image of menu properly, copied from https://tips4java.wordpress.com/2008/10/12/background-panel/
    public class BackgroundPanel extends JPanel {
        public static final int SCALED = 0;
        public static final int TILED = 1;
        public static final int ACTUAL = 2;

        private Paint painter;
        private Image image;
        private int style = SCALED;
        private float alignmentX = 0.5f;
        private float alignmentY = 0.5f;
        private boolean isTransparentAdd = true;

        /*
         *  Set image as the background with the SCALED style
         */
        public BackgroundPanel(Image image) {
            this(image, SCALED);
        }

        /*
         *  Set image as the background with the specified style
         */
        public BackgroundPanel(Image image, int style) {
            setImage(image);
            setStyle(style);
            setLayout(new BorderLayout());
        }

        /*
         *  Set image as the backround with the specified style and alignment
         */
        public BackgroundPanel(Image image, int style, float alignmentX, float alignmentY) {
            setImage(image);
            setStyle(style);
            setImageAlignmentX(alignmentX);
            setImageAlignmentY(alignmentY);
            setLayout(new BorderLayout());
        }

        /*
         *  Use the Paint interface to paint a background
         */
        public BackgroundPanel(Paint painter) {
            setPaint(painter);
            setLayout(new BorderLayout());
        }

        /*
         *	Set the image used as the background
         */
        public void setImage(Image image) {
            this.image = image;
            repaint();
        }

        /*
         *	Set the style used to paint the background image
         */
        public void setStyle(int style) {
            this.style = style;
            repaint();
        }

        /*
         *	Set the Paint object used to paint the background
         */
        public void setPaint(Paint painter) {
            this.painter = painter;
            repaint();
        }

        /*
         *  Specify the horizontal alignment of the image when using ACTUAL style
         */
        public void setImageAlignmentX(float alignmentX) {
            this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
            repaint();
        }

        /*
         *  Specify the horizontal alignment of the image when using ACTUAL style
         */
        public void setImageAlignmentY(float alignmentY) {
            this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
            repaint();
        }

        /*
         *  Override method so we can make the component transparent
         */
        public void add(JComponent component) {
            add(component, null);
        }

        /*
         *  Override to provide a preferred size equal to the image size
         */
        @Override
        public Dimension getPreferredSize() {
            if (image == null)
                return super.getPreferredSize();
            else
                return new Dimension(image.getWidth(null), image.getHeight(null));
        }

        /*
         *  Override method so we can make the component transparent
         */
        public void add(JComponent component, Object constraints) {
            if (isTransparentAdd) {
                makeComponentTransparent(component);
            }

            super.add(component, constraints);
        }

        /*
         *  Controls whether components added to this panel should automatically
         *  be made transparent. That is, setOpaque(false) will be invoked.
         *  The default is set to true.
         */
        public void setTransparentAdd(boolean isTransparentAdd) {
            this.isTransparentAdd = isTransparentAdd;
        }

        /*
         *	Try to make the component transparent.
         *  For components that use renderers, like JTable, you will also need to
         *  change the renderer to be transparent. An easy way to do this it to
         *  set the background of the table to a Color using an alpha value of 0.
         */
        private void makeComponentTransparent(JComponent component) {
            component.setOpaque(false);

            if (component instanceof JScrollPane scrollPane) {
                JViewport viewport = scrollPane.getViewport();
                viewport.setOpaque(false);
                Component c = viewport.getView();

                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
            }
        }

        /*
         *  Add custom painting
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            //  Invoke the painter for the background

            if (painter != null) {
                Dimension d = getSize();
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(painter);
                g2.fill(new Rectangle(0, 0, d.width, d.height));
            }

            //  Draw the image

            if (image == null) return;

            switch (style) {
                case SCALED:
                    drawScaled(g);
                    break;

                case TILED:
                    drawTiled(g);
                    break;

                case ACTUAL:
                    drawActual(g);
                    break;

                default:
                    drawScaled(g);
            }
        }

        /*
         *  Custom painting code for drawing a SCALED image as the background
         */
        private void drawScaled(Graphics g) {
            Dimension d = getSize();
            g.drawImage(image, 0, 0, d.width, d.height, null);
        }

        /*
         *  Custom painting code for drawing TILED images as the background
         */
        private void drawTiled(Graphics g) {
            Dimension d = getSize();
            int width = image.getWidth(null);
            int height = image.getHeight(null);

            for (int x = 0; x < d.width; x += width) {
                for (int y = 0; y < d.height; y += height) {
                    g.drawImage(image, x, y, null, null);
                }
            }
        }

        /*
         *  Custom painting code for drawing the ACTUAL image as the background.
         *  The image is positioned in the panel based on the horizontal and
         *  vertical alignments specified.
         */
        private void drawActual(Graphics g) {
            Dimension d = getSize();
            Insets insets = getInsets();
            int width = d.width - insets.left - insets.right;
            int height = d.height - insets.top - insets.left;
            float x = (width - image.getWidth(null)) * alignmentX;
            float y = (height - image.getHeight(null)) * alignmentY;
            g.drawImage(image, (int) x + insets.left, (int) y + insets.top, this);
        }
    }
}
