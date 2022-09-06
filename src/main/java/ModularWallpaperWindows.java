import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ModularWallpaperWindows {
    private static JFrame frame = new JFrame();
    private static JPanel panel = new JPanel();
    private static JLabel timeField = new JLabel();
    private static JLabel dateField = new JLabel();
    private static HashMap<String, Color> colors = new HashMap<>();

    public static void main(String[] args) {
        populateColors();

        frame.setFocusable(false);
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
        frame.setUndecorated(true);
        frame.setAutoRequestFocus(false);
        frame.setResizable(false);
        frame.setFocusableWindowState(false);
        frame.setFocusCycleRoot(false);
        frame.setVisible(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setLocation(0, 50);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screen);

        panel.setBackground(new Color(0, 0, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(screen);
        frame.add(panel);
        frame.pack();

        frame.toBack();

        panel.add(timeField);
        panel.add(dateField);

        initializeTimeLabel();
        initializeDateLabel();

        setSystemTrayIcon();
        startImageLoop();
    }

    private static void populateColors() {
        colors.put("Pink", new Color(255, 189, 189));
        colors.put("Black", new Color(0, 0, 0));
        colors.put("White", new Color(255, 255, 255, 255));
        colors.put("Blue", new Color(97, 152, 208));
        colors.put("Green", new Color(74, 120, 74));
        colors.put("Dark Gray", new Color(75, 75, 75));
        colors.put("Orange", new Color(227, 180, 77));
        colors.put("Gray", new Color(150, 150, 150));
        colors.put("Red", new Color(159, 23, 23));
        colors.put("Yellow", new Color(255, 222, 142));
    }

    private static void initializeDateLabel() {
        Font dateFont = getDateFont(getFontPath());
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        dateField.setBackground(new Color(0, 0, 0, 0));
        dateField.setBorder(BorderFactory.createEmptyBorder());
        dateField.setFont(dateFont);
        dateField.setForeground(getColor());
        Dimension dateSize = new Dimension((int) screen.getWidth(), 100);
        dateField.setSize(dateSize);
        dateField.setMinimumSize(dateSize);
        dateField.setPreferredSize(dateSize);
        dateField.setMaximumSize(dateSize);
        dateField.setHorizontalAlignment(SwingConstants.CENTER);
        dateField.setHorizontalTextPosition(SwingConstants.CENTER);

    }

    private static Color getColor() {
        Properties properties = getProperties();
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            if (properties.getProperty("COLOR", "White").equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Color.WHITE;
    }

    private static void initializeTimeLabel() {
        Font timeFont = getTimeFont(getFontPath());
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        timeField.setBackground(new Color(0, 0, 0, 0));
        timeField.setBorder(BorderFactory.createEmptyBorder());
        timeField.setFont(timeFont);
        timeField.setForeground(getColor());
        int timeSizeInt = areSecondsEnabled() ? 400 : 350;
        Dimension timeSize = new Dimension((int) screen.getWidth(), timeSizeInt);
        timeField.setSize(timeSize);
        timeField.setMinimumSize(timeSize);
        timeField.setPreferredSize(timeSize);
        timeField.setMaximumSize(timeSize);
        timeField.setHorizontalAlignment(SwingConstants.CENTER);
        timeField.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    private static boolean areSecondsEnabled() {
        Properties properties = getProperties();
        return Boolean.parseBoolean(properties.getProperty("SECONDS", "FALSE"));
    }

    private static Path getFontPath() {
        Path fontPath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/timeburnerbold.ttf");
        if (areSecondsEnabled())
            fontPath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/dealerplate_california.ttf");
        return fontPath;
    }

    private static Font getDateFont(Path fontPath) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile());
            return font.deriveFont(Font.PLAIN, 50);
        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Font getTimeFont(Path fontPath) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile());
            return font.deriveFont(Font.BOLD, 305);
        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void startImageLoop() {
        while (true) {
            createAndUpdateWallpaper();
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createAndUpdateWallpaper() {
        Properties properties = getProperties();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        if (Boolean.parseBoolean(properties.getProperty("SECONDS", "FALSE")))
            dateFormat = new SimpleDateFormat("hh:mm:ss");
        String timeString = dateFormat.format(new Date());

        EventQueue.invokeLater(() -> {
            timeField.setText(timeString);
            frame.revalidate();
            frame.repaint();
            frame.pack();
        });

        if (Boolean.parseBoolean(properties.getProperty("DATE", "TRUE"))) {
            // Date
            dateFormat = new SimpleDateFormat("EEEEE, MMMMM d, y");
            String dateString = dateFormat.format(new Date());
            EventQueue.invokeLater(() -> {
                dateField.setText(dateString);
                frame.revalidate();
                frame.repaint();
                frame.pack();
            });
        }
        else {
            EventQueue.invokeLater(() -> {
                dateField.setText("");
                frame.revalidate();
                frame.repaint();
                frame.pack();
            });
        }
    }

    private static void setSystemTrayIcon() {
        SystemTray systemTray = SystemTray.getSystemTray();
        Path imagePath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(imagePath.toString());

        PopupMenu popupMenu = new PopupMenu();

        Properties properties = getProperties();

        // Exit button
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Seconds button
        CheckboxMenuItem seconds = new CheckboxMenuItem("Seconds", false);
        seconds.setState(Boolean.parseBoolean(
                properties.getProperty("SECONDS", "FALSE")
        ));
        seconds.addItemListener(e -> {
            boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
            properties.setProperty("SECONDS", enabled ? "TRUE" : "FALSE");
            storeProperties(properties);
            initializeTimeLabel();
            initializeDateLabel();
        });
        popupMenu.add(seconds);

        // Date button
        CheckboxMenuItem date = new CheckboxMenuItem("Date");
        date.setState(Boolean.parseBoolean(properties.getProperty("DATE", "TRUE")));
        date.addItemListener(e -> {
            boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
            properties.setProperty("DATE", enabled ? "TRUE": "FALSE");
            storeProperties(properties);
        });
        popupMenu.add(date);

        // Color
        PopupMenu color = new PopupMenu("Color");
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            MenuItem colorItem = new MenuItem(entry.getKey());
            colorItem.addActionListener((e) -> {
                properties.setProperty("COLOR", entry.getKey());
                storeProperties(properties);
                initializeTimeLabel();
                initializeDateLabel();
            });
            color.add(colorItem);
        }
        popupMenu.add(color);

        // Tray Icon
        popupMenu.add(exit);
        TrayIcon trayIcon = new TrayIcon(image, "Modular Wallpaper", popupMenu);

        try {
            systemTray.add(trayIcon);
        }
        catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private static void storeProperties(Properties properties) {
        try {
            properties.store(new FileOutputStream(
                    Paths.get(System.getenv("APPDATA"), "ModularWallpaper/all.properties")
                            .toFile()
            ), null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try {
            File propertyPath = Paths.get(System.getenv("APPDATA"),
                    "ModularWallpaper/all.properties").toFile();
            if (!propertyPath.exists())
                propertyPath.createNewFile();
            properties.load(new FileInputStream(
                    propertyPath
            ));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
