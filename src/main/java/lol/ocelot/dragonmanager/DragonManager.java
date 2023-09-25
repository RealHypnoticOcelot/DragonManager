package lol.ocelot.dragonmanager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import com.formdev.flatlaf.*;
import java.net.*;

public class DragonManager extends JFrame implements ActionListener {

    // Get config file, or create if there isn't one
    public static Preferences prefs;
    public static BackgroundPanel bgObject;
    public static JRadioButtonMenuItem bgScaled;
    public static JRadioButtonMenuItem bgTiled;
    public static JRadioButtonMenuItem bgActual;
    static { // Static
        prefs = Preferences.userRoot().node(DragonManager.class.getName());

    }
    { // Not static
        bgObject = new BackgroundPanel(null, BackgroundPanel.SCALED, 0.0f, 0.0f);
    }

    private boolean APIisReachable() {
        // Get whether open5e is available
        try {
            return(InetAddress.getByName("api.open5e.com").isReachable(1000));
        } catch (IOException e) {
            return(false);
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

        JMenuItem saveChar = new JMenuItem("Save Character");
        saveChar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveChar.setActionCommand("saveChar");
        saveChar.addActionListener(this);
        charMenu.add(saveChar);

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

    public void setBackground(BackgroundPanel bgPanel) {
        if (!(prefs.get("wallpaper", null) == null)) {
            try {
                BufferedImage bgImage = ImageIO.read(new File(prefs.get("wallpaper", null)));
                bgPanel.setImage(bgImage);
                int Scaling = prefs.getInt("scalingType", 0);
                /*
                    0 = Scaled
                    1 = Tiled
                    2 = Actual Size
                */
                bgPanel.setStyle(Scaling);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            bgPanel.setImage(null);
        }
    }

    // Actions from scaling menus
    private class ScalingEvent implements ActionListener {
        private int ScaleValue;
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
            setBackground(bgObject);
        }
    }

    // All Other Actions from buttons and menus
    public void actionPerformed(ActionEvent e) {
        if ("newChar".equals(e.getActionCommand())) {
            if (APIisReachable()) {
                System.out.println("API");
            } else {
                JOptionPane.showMessageDialog(null, "Cannot access API, unable to create characters!", "Error!", JOptionPane.INFORMATION_MESSAGE);
            }
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
                setBackground(bgObject);
            }
        } else if ("resetBG".equals(e.getActionCommand())) {
            prefs.remove("wallpaper");
            setBackground(bgObject);
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

    // Set Background Image of menu properly, copied from https://tips4java.wordpress.com/2008/10/12/background-panel/
    public class BackgroundPanel extends JPanel
    {
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
        public BackgroundPanel(Image image)
        {
            this(image, SCALED);
        }

        /*
         *  Set image as the background with the specified style
         */
        public BackgroundPanel(Image image, int style)
        {
            setImage( image );
            setStyle( style );
            setLayout( new BorderLayout() );
        }

        /*
         *  Set image as the backround with the specified style and alignment
         */
        public BackgroundPanel(Image image, int style, float alignmentX, float alignmentY)
        {
            setImage( image );
            setStyle( style );
            setImageAlignmentX( alignmentX );
            setImageAlignmentY( alignmentY );
            setLayout( new BorderLayout() );
        }

        /*
         *  Use the Paint interface to paint a background
         */
        public BackgroundPanel(Paint painter)
        {
            setPaint( painter );
            setLayout( new BorderLayout() );
        }

        /*
         *	Set the image used as the background
         */
        public void setImage(Image image)
        {
            this.image = image;
            repaint();
        }

        /*
         *	Set the style used to paint the background image
         */
        public void setStyle(int style)
        {
            this.style = style;
            repaint();
        }

        /*
         *	Set the Paint object used to paint the background
         */
        public void setPaint(Paint painter)
        {
            this.painter = painter;
            repaint();
        }

        /*
         *  Specify the horizontal alignment of the image when using ACTUAL style
         */
        public void setImageAlignmentX(float alignmentX)
        {
            this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
            repaint();
        }

        /*
         *  Specify the horizontal alignment of the image when using ACTUAL style
         */
        public void setImageAlignmentY(float alignmentY)
        {
            this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
            repaint();
        }

        /*
         *  Override method so we can make the component transparent
         */
        public void add(JComponent component)
        {
            add(component, null);
        }

        /*
         *  Override to provide a preferred size equal to the image size
         */
        @Override
        public Dimension getPreferredSize()
        {
//		Dimension panelSize = super.getPreferredSize();
//		Dimension imageSize = getLayout().preferredLayoutSize(this);
//		panelSize.width = Math.max(panelSize.width, imageSize.width);
//		panelSize.height = Math.max(panelSize.height, imageSize.height);

            if (image == null)
                return super.getPreferredSize();
            else
                return new Dimension(image.getWidth(null), image.getHeight(null));
        }

        /*
         *  Override method so we can make the component transparent
         */
        public void add(JComponent component, Object constraints)
        {
            if (isTransparentAdd)
            {
                makeComponentTransparent(component);
            }

            super.add(component, constraints);
        }

        /*
         *  Controls whether components added to this panel should automatically
         *  be made transparent. That is, setOpaque(false) will be invoked.
         *  The default is set to true.
         */
        public void setTransparentAdd(boolean isTransparentAdd)
        {
            this.isTransparentAdd = isTransparentAdd;
        }

        /*
         *	Try to make the component transparent.
         *  For components that use renderers, like JTable, you will also need to
         *  change the renderer to be transparent. An easy way to do this it to
         *  set the background of the table to a Color using an alpha value of 0.
         */
        private void makeComponentTransparent(JComponent component)
        {
            component.setOpaque( false );

            if (component instanceof JScrollPane scrollPane)
            {
                JViewport viewport = scrollPane.getViewport();
                viewport.setOpaque( false );
                Component c = viewport.getView();

                if (c instanceof JComponent)
                {
                    ((JComponent)c).setOpaque( false );
                }
            }
        }

        /*
         *  Add custom painting
         */
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            //  Invoke the painter for the background

            if (painter != null)
            {
                Dimension d = getSize();
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(painter);
                g2.fill( new Rectangle(0, 0, d.width, d.height) );
            }

            //  Draw the image

            if (image == null ) return;

            switch (style)
            {
                case SCALED :
                    drawScaled(g);
                    break;

                case TILED  :
                    drawTiled(g);
                    break;

                case ACTUAL :
                    drawActual(g);
                    break;

                default:
                    drawScaled(g);
            }
        }

        /*
         *  Custom painting code for drawing a SCALED image as the background
         */
        private void drawScaled(Graphics g)
        {
            Dimension d = getSize();
            g.drawImage(image, 0, 0, d.width, d.height, null);
        }

        /*
         *  Custom painting code for drawing TILED images as the background
         */
        private void drawTiled(Graphics g)
        {
            Dimension d = getSize();
            int width = image.getWidth( null );
            int height = image.getHeight( null );

            for (int x = 0; x < d.width; x += width)
            {
                for (int y = 0; y < d.height; y += height)
                {
                    g.drawImage( image, x, y, null, null );
                }
            }
        }

        /*
         *  Custom painting code for drawing the ACTUAL image as the background.
         *  The image is positioned in the panel based on the horizontal and
         *  vertical alignments specified.
         */
        private void drawActual(Graphics g)
        {
            Dimension d = getSize();
            Insets insets = getInsets();
            int width = d.width - insets.left - insets.right;
            int height = d.height - insets.top - insets.left;
            float x = (width - image.getWidth(null)) * alignmentX;
            float y = (height - image.getHeight(null)) * alignmentY;
            g.drawImage(image, (int)x + insets.left, (int)y + insets.top, this);
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

        // Create background object and add it as a component
        add(bgObject);
        setBackground(bgObject); // Set background if there is ones
        setContentPane(bgObject);

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
                // Set theme for application if a preference is set
                try {
                    if (Objects.equals(prefs.get("theme", null), "dark")) { // use == because it can be null
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
