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
import java.util.Properties;

public class ModularWallpaperWindows {
    public static void main(String[] args) {
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
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.fillRect(0, 0, 1920, 1080);

        Date date = new Date(System.currentTimeMillis());

        g.setColor(Color.WHITE);
        Path fontPath = Paths.get(System.getenv("APPDATA"), "ModularWallpaper/timeburnerbold.ttf");
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile());
            font = font.deriveFont(Font.BOLD, 305);
            g.setFont(font);

            //String timeString = String.format("%02d:%02d", date.getHours(), date.getMinutes());
            DateFormat dateFormat = new SimpleDateFormat("hh:mm");
            Properties properties = getProperties();
            if (Boolean.parseBoolean(properties.getProperty("SECONDS", "FALSE")))
                dateFormat = new SimpleDateFormat("hh:mm:ss");
            String timeString = dateFormat.format(new Date());

            FontMetrics fontMetrics = g.getFontMetrics(font);
            double x = 1920.0 / 2.0 - fontMetrics.stringWidth(timeString) / 2.0;
            double y = 1080.0 / 2.0 -  fontMetrics.getHeight() / 2.0;
            g.drawString(timeString, (float) x, (float) y);
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
        popupMenu.add(exit);

        // Seconds button
        CheckboxMenuItem seconds = new CheckboxMenuItem("Seconds", false);
        seconds.setState(Boolean.parseBoolean(
                properties.getProperty("SECONDS", "FALSE")
        ));
        seconds.addItemListener(e -> {
            boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
            properties.setProperty("SECONDS", enabled ? "TRUE" : "FALSE");
            storeProperties(properties);
            //seconds.setState(!enabled);
        });
        popupMenu.add(seconds);

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
            properties.load(new FileInputStream(
                    Paths.get(System.getenv("APPDATA"), "ModularWallpaper/all.properties")
                            .toFile()
            ));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
