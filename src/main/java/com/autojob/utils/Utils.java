package com.autojob.utils;

import com.autojob.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.awt.Image.SCALE_SMOOTH;

public class Utils {

    private static final Random random = new Random();

    public static int getRandomInt(int min, int max) {
        if (min >= max) return min;
        return min + random.nextInt(max - min + 1);
    }

    public static void showAlert(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE, content, ButtonType.OK);
            alert.setTitle("");
            alert.showAndWait();
        });
    }

    public static void showAlert(String quest, IConfirmCallback callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText(quest);
            ButtonType btnOk = new ButtonType("Ok");
            ButtonType btnCancel = new ButtonType("Cancel");
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(btnOk, btnCancel);
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() == null) {
                callback.onFalse();
            } else if (option.get() == btnCancel) {
                callback.onFalse();
            } else callback.onTrue();
        });
    }

    public interface IConfirmCallback {
        void onTrue();

        void onFalse();
    }

    public static void postDelay(IDoWordCallback doWork, long milliSeconds) {
        Task<Void> sleeper = new Task<Void>() {

            @Override
            protected Void call() {
                try {
                    Thread.sleep(milliSeconds);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> doWork.run());
        new Thread(sleeper).start();
    }

    public static void postDelayy() {

        KeyFrame kf2 = new KeyFrame(Duration.millis(3000), e -> {
        });
        Timeline timeline = new Timeline(kf2);
        Platform.runLater(timeline::play);
    }

    public static void runOnUIThread(IDoWordCallback doWork) {
        Platform.runLater(doWork::run);
    }

    public static void sleepTime(long time) {
        if (time == 0) return;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    public static int getRandomNumber(int min, int max) {
        if (max > min) {
            Random random = new Random();
            return random.nextInt(max - min + 1) + min;
        } else {
            throw new IllegalArgumentException("Max must be greater than min");
        }
    }

    public static int randomInteger(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to + 1);
//        int length = to - from;
//        if (length <= 0) return 0;
//        return from + new Random().nextInt(length);
    }

    public static void grantFilePermission(File file) {
        if (file != null && file.exists()) {
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                Files.setPosixFilePermissions(file.toPath(), perms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void grantChormeAppPermission(File file) {
        if (file != null && file.exists()) {
            try {
                if (file.isDirectory()) {
                    if (file.listFiles() != null && file.listFiles().length > 0) {
                        for (File f : file.listFiles()) {
                            grantChormeAppPermission(f);
                        }
                    }
                }
                Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxr-xr-x");
                Files.setPosixFilePermissions(file.toPath(), ownerWritable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String regexString(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        String res = "";
        while (matcher.find()) {
            res = matcher.group("res");
        }
        return res;
    }

    public static List<String> regexListString(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        List<String> res = new ArrayList<>();
        while (matcher.find()) {
            res.add(matcher.group("res"));
        }
        return res;
    }

    public static void setStyleForButton(Button... buttons) {
        for (Button button : buttons) {
            button.setOnMouseEntered(event -> {
                button.getScene().setCursor(Cursor.HAND);
            });
            button.setOnMouseExited(event -> {
                button.getScene().setCursor(Cursor.DEFAULT);
            });
        }
    }

//    public static void deleteCookie(String accountId) {
//        try {
//            File file = new File(DatabaseHelper.DIRECTORY + "/" + accountId + ".txt");
//            file.delete();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public static void deleteFile(String path) {
        try {
            File file = new File(path);
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteDirectory(String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean saveImageByUrl(String url, String filename) {
        try (InputStream in = new URL(url).openStream()) {
            File file = new File(filename);
            file.getParentFile().mkdirs();
            Files.copy(in, Paths.get(filename));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    private static final String JPG_IMAGE = "jpg";

    public static void createImageWithBkg(String originImagePath, String backgroundImagePath) {
        try {

            // load source images
            BufferedImage image = null;
            File originFileImage = new File(originImagePath);
            image = ImageIO.read(originFileImage);

            BufferedImage overlay = ImageIO.read(new File(backgroundImagePath));

            // create the new image, canvas size is the max. of both image sizes
            int w = overlay.getWidth();
            int h = overlay.getHeight();
            BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = combined.createGraphics();

            graphics.setPaint(new Color(255, 255, 255));
            graphics.fillRect(0, 0, w, h);
            float scaleRatioW = (float) image.getWidth() / (float) w;
            float scaleRatioH = (float) image.getHeight() / (float) h;
            float scaleRatio = Math.max(scaleRatioH, scaleRatioW);
            int scaleW = (int) (image.getWidth() / scaleRatio);
            int scaleH = (int) (image.getHeight() / scaleRatio);
            Image scaleImage = image.getScaledInstance(scaleW, scaleH, SCALE_SMOOTH);
            int x, y;
            if (scaleRatioW < scaleRatioH) {
                x = (w - scaleW) / 2;
                y = 0;
            } else {
                x = 0;
                y = (h - scaleH) / 2;
            }

            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            g.drawImage(scaleImage, x, y, null);
            g.drawImage(overlay, 0, 0, null);


            // Save as new image
            File path = new File(originFileImage.getParent()); // base path of the images

            String imageType = JPG_IMAGE;
            if (originFileImage.getName().split(".").length == 2) {
                imageType = originFileImage.getName().split(".")[1];
            }

            if (imageType.equals(JPG_IMAGE)) {
                JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
                jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpegParams.setCompressionQuality(1f);
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                writer.setOutput(new FileImageOutputStream(
                        new File(path, originFileImage.getName())));
                writer.write(null, new IIOImage(combined, null, null), jpegParams);
            } else {
                ImageIO.write(combined, "PNG", new File(path, originFileImage.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("createImageWithBkg error " + originImagePath + " " + backgroundImagePath + " " + e.getMessage());
        }
    }

    public static void openBrowser(String url) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void checkRestartApp() {
        // Restart 3 AM
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int min = rightNow.get(Calendar.MINUTE);
        if (hour == 3
                && !App.APP_STOPPING
                && System.currentTimeMillis() - App.timeStartApp > TimeUtils.M_1_HOURS) {
            setUpRestartApplication(5);
        }
    }

    public static void setUpRestartApplication(int minDelay) {
        Logger.info("setUpRestartApplication => START.");
        App.stopAllAccount();
        new Thread(() -> {
            try {
                Thread.sleep(minDelay * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            restartApplication();
        }).start();
    }

    public static void restartApplication() {
        Logger.info(Utils.class.getSimpleName(), "RebootApp START => restartApplication");
        try {
            // java binary
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            // vm arguments
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            StringBuffer vmArgsOneLine = new StringBuffer();
            for (String arg : vmArguments) {
                // if it's the agent argument : we ignore it otherwise the
                // address of the old application and the new one will be in conflict
                if (!arg.contains("-agentlib")) {
                    vmArgsOneLine.append(arg);
                    vmArgsOneLine.append(" ");
                }
            }
            // init the command to execute, add the vm args
            final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);
            // program main and program arguments (be careful a sun property. might not be supported by all JVM)
            String[] mainCommand = System.getProperty("sun.java.command").split(" ");
            // program main is a jar
//            if (mainCommand[0].endsWith(".jar")) {
            // if it's a jar, add -jar mainJar
            cmd.append("-jar \"" + new File(mainCommand[0]).getPath());
//            } else {
//                 else it's a .class, add the classpath and mainClass
//                cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
//            }
            // finally add program arguments
            for (int i = 1; i < mainCommand.length; i++) {
                cmd.append(" ");
                cmd.append(mainCommand[i]);
            }
            // execute the command in a shutdown hook, to be sure that all the
            // resources have been disposed before restarting the application
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Logger.info("#restartApplication => cmd: " + cmd.toString());
                        Runtime.getRuntime().exec(cmd.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logger.error("Error while run cmd", e);
                    }
                }
            });
            // exit
            System.exit(0);
        } catch (Exception e) {
            // something went wrong
            Logger.error("Error while trying to restart the application", e);
        }
    }
}
