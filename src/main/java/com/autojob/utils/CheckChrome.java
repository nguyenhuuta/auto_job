package com.autojob.utils;

import com.autojob.App;
import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CheckChrome {
    private final String urlFileWebDriverNew = "http://133.130.73.227:6789/downloadFiles/chromedriver/";
    private String chromeCurrentVersion = "95."; //95.0.4638.54

    public interface ICheckChromeCallBack {
        void onFinished();
    }

    ICheckChromeCallBack callBack;

    public static void startCheckChrome(ICheckChromeCallBack callBack) {
        CheckChrome checkChrome = new CheckChrome();
        checkChrome.callBack = callBack;
        new Thread(() -> {
            checkChrome.checkAll();
            callBack.onFinished();
        }).start();
    }

    private void checkAll() {
        String parentPath;
        if (isMac()) {
            parentPath = Paths.get("").toAbsolutePath().toString();
        } else {
            parentPath = Paths.get("").toAbsolutePath().toString();
        }
        if (isMac()) {
            downloadChrome(parentPath, "ChromeMac", 1);
        } else {
            downloadChrome(parentPath, "ChromeWin", 1);
        }
//        downloadChromeDriverNew(parentPath,"74");
    }

    private void downloadChrome(String parentPath, String name, int time) {
        Logger.info("Check downloadChrome start.");
        File webdriver = new File(parentPath + File.separator + name);
        // Delete folder old
        try {
            FileUtils.deleteDirectory(new File(webdriver + "-old"));
        } catch (Exception ex) {
            Logger.error("deleteDirectoryOld Exception: " + webdriver.getPath());
        }
        if (!webdriver.exists()) {
            String fileZipPath = parentPath + File.separator + name + ".zip";
            String webDriverPath = parentPath + File.separator + name;
            String urlFileWebDriver = urlFileWebDriverNew + name + ".zip";
            downloadFile(urlFileWebDriver, fileZipPath);
            if (isMac()) {
                Utils.grantFilePermission(new File(fileZipPath));
            }
            File chrome = new File(parentPath + File.separator + name);
            if (!chrome.exists()) {
                chrome.mkdir();
            }
            if (isMac()) {
                // Mac
                try {
                    String command = "unzip -q " + fileZipPath + " -d " + chrome.getPath();
                    System.out.println(command);
                    ProcessBuilder pb = new ProcessBuilder("unzip", "-q", fileZipPath, "-d", chrome.getPath());
                    Process p = pb.start();
                    p.waitFor();
                } catch (Exception e) {
                    System.out.println("HEY Buddy ! U r Doing Something Wrong ");
                    e.printStackTrace();
                }
            } else {
                // Win
                unzipFolder(fileZipPath, webDriverPath);
            }
            File file = new File(fileZipPath);
            if (file.exists()) {
                file.delete();
            }
        }
        App.setupWebDriver();
    }

    private void unzipFolder(String zipFile, String outFolder) {
        try {
            Unzip unzipper = new Unzip();
            unzipper.setSrc(new File(zipFile));
            unzipper.setDest(new File(outFolder));
            unzipper.execute();
        } catch (Exception ex) {
            Logger.info("Unzip...");
        }
    }

    private void unzip(String zipFile, String outFolder) {
        try {
            File destDir = new File(outFolder);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void downloadFile(String urlDownload, String saveFile) {
        try {
            URL url = new URL(urlDownload);
            FileUtils.copyURLToFile(url, new File(saveFile));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }
}