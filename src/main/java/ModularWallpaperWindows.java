import javax.imageio.ImageIO;
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
    private static HashMap<String, Color> colors = new HashMap<>();

    public static void main(String[] args) {
        populateColors();
        setSystemTrayIcon();
        startImageLoop();
    }

    private static void populateColors() {
        colors.put("Pink", new Color(255, 189, 189));
        colors.put("Black", new Color(0, 0, 0));
        colors.put("Blue", new Color(97, 152, 208));
        colors.put("Green", new Color(74, 120, 74));
        colors.put("Dark Gray", new Color(75, 75, 75));
        colors.put("Orange", new Color(227, 180, 77));
        colors.put("Gray", new Color(150, 150, 150));
        colors.put("Red", new Color(159, 23, 23));
        colors.put("Yellow", new Color(255, 222, 142));
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

        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(getBackgroundColor());
        g.setStroke(new BasicStroke(1));
        g.fillRect(0, 0, 1920, 1080);

        g.setColor(Color.WHITE);
        Path fontPath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/timeburnerbold.ttf");
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile());
            font = font.deriveFont(Font.BOLD, 305);
            g.setFont(font);

            DateFormat dateFormat = new SimpleDateFormat("hh:mm");
            if (Boolean.parseBoolean(properties.getProperty("SECONDS", "FALSE")))
                dateFormat = new SimpleDateFormat("hh:mm:ss");
            String timeString = dateFormat.format(new Date());

            FontMetrics fontMetrics = g.getFontMetrics(font);
            double x = 1920.0 / 2.0 - fontMetrics.stringWidth(timeString) / 2.0;
            double y = 1080.0 / 2.0 -  fontMetrics.getHeight() / 2.0;
            g.drawString(timeString, (float) x, (float) y);

            if (Boolean.parseBoolean(properties.getProperty("DATE", "FALSE"))) {
                // Date
                font = font.deriveFont(Font.PLAIN, 50);
                g.setFont(font);
                fontMetrics = g.getFontMetrics(font);

                dateFormat = new SimpleDateFormat("EEEEE, MMMMM d, y");
                String dateString = dateFormat.format(new Date());
                g.drawString(dateString,
                        (int) (1920.0 / 2.0 - fontMetrics.stringWidth(dateString) / 2.0),
                        425);
            }
        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }


        Path imagePath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/wallpaper.png");
        try {
            ImageIO.write(image, "png", imagePath.toFile());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        updateWallpaper();
    }

    private static Color getBackgroundColor() {
        Properties properties = getProperties();
        String colorName = properties.getProperty("COLOR", "Black");
        return colors.getOrDefault(colorName, Color.BLACK);
    }

    private static void updateWallpaper() {
        Path imagePath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/wallpaper.png");
        Path path = Paths.get(System.getenv("APPDATA"), "ModularWallpaper");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "--enable-native-access=ALL-UNNAMED",
                    "--add-modules",
                    "jdk.incubator.foreign",
                    "ModularWallpaper",
                    imagePath.toAbsolutePath().toString());
            processBuilder.directory(path.toFile());
            processBuilder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
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

        // Color
        PopupMenu colorMenu = new PopupMenu("Color");
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            MenuItem color = new MenuItem(entry.getKey());
            color.addActionListener(e -> {
                properties.setProperty("COLOR", entry.getKey());
                storeProperties(properties);
            });
            colorMenu.add(color);
        }
        popupMenu.add(colorMenu);

        // Date button
        CheckboxMenuItem date = new CheckboxMenuItem("Date");
        date.setState(Boolean.parseBoolean(properties.getProperty("DATE", "FALSE")));
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
