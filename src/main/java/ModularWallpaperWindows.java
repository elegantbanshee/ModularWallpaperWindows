import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
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

    public static void main(String[] args) {
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

        Path fontPath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/timeburnerbold.ttf");

        Font timeFont = null;
        Font dateFont = null;
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile());
            timeFont = font.deriveFont(Font.BOLD, 305);
            dateFont = font.deriveFont(Font.PLAIN, 50);
        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        timeField.setBackground(new Color(0, 0, 0, 0));
        timeField.setBorder(BorderFactory.createEmptyBorder());
        timeField.setFont(timeFont);
        timeField.setForeground(Color.WHITE);
        Dimension timeSize = new Dimension((int) screen.getWidth(), 400);
        timeField.setSize(timeSize);
        timeField.setMinimumSize(timeSize);
        timeField.setPreferredSize(timeSize);
        timeField.setMaximumSize(timeSize);
        timeField.setHorizontalAlignment(SwingConstants.CENTER);
        timeField.setHorizontalTextPosition(SwingConstants.CENTER);
        panel.add(timeField);

        dateField.setBackground(new Color(0, 0, 0, 0));
        dateField.setBorder(BorderFactory.createEmptyBorder());
        dateField.setFont(dateFont);
        dateField.setForeground(Color.WHITE);
        Dimension dateSize = new Dimension((int) screen.getWidth(), 50);
        dateField.setSize(dateSize);
        dateField.setMinimumSize(dateSize);
        dateField.setPreferredSize(dateSize);
        dateField.setMaximumSize(dateSize);
        dateField.setHorizontalAlignment(SwingConstants.CENTER);
        dateField.setHorizontalTextPosition(SwingConstants.CENTER);
        panel.add(dateField);

        setSystemTrayIcon();
        startImageLoop();
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
