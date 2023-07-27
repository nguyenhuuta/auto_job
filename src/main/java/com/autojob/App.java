package com.autojob;

import com.autojob.database.DatabaseHelper;
import com.autojob.gui.splash.SplashScreen;
import com.autojob.shopee.ShopeeFile;
import com.autojob.utils.IRegisterStopApp;
import com.autojob.utils.Utils;
import com.autojob.utils.WebDriverUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.chrome.ChromeDriverService;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class App extends Application {
    public static boolean debug = true;
    private static App instance;
    private List<IRegisterStopApp> listCallbackRegisterStopApp;

    public static boolean APP_STOPPING = false;
    public static long timeStartApp = System.currentTimeMillis();

    public static App getInstance() {
        return instance;
    }

    private void setInstance(App app) {
        App.instance = app;
    }

    static {
        DatabaseHelper.getInstance();
        ShopeeFile.createFile();
    }

    public static void setupWebDriver() {
        String userDir = System.getProperty("user.dir");
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, userDir + "/ChromeWin/chromedriver.exe");
        } else if (SystemUtils.IS_OS_MAC) {
            File webDriver = new File(userDir, "/ChromeMac/chromedriver");
            Utils.grantFilePermission(webDriver);
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, userDir + "/ChromeMac/chromedriver");
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.contains("debug")) {
                debug = true;
            }
        }
        launch(args);
    }


    boolean playingSound = false;

    public void playSound() {
        System.out.println("PlaySound " + playingSound);
        if (playingSound) {
            return;
        }
        playingSound = true;
        try {
            URL url = this.getClass().getResource("/bip2.mp3");
            Media hit = new Media(url.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Observable.just(1).zipWith(Observable.interval(10, TimeUnit.SECONDS), (item, interval) -> item)
                .subscribe(integer -> playingSound = false);

    }

    private Stage mStage;
    private boolean startApp;

    public Stage getStage() {
        return mStage;
    }

    public boolean isStartApp() {
        return startApp;
    }


    private void displaySplashScreen(Stage stage) {
        SplashScreen splashScreen = new SplashScreen();
        Scene scene = new Scene(splashScreen);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
//        displaySplashScreen(primaryStage);
        listCallbackRegisterStopApp = new ArrayList<>();
        startApp = true;
        setInstance(this);
        mStage = primaryStage;
        startApp = false;
    }

    @Override
    public void stop() throws Exception {
        for (IRegisterStopApp callback : listCallbackRegisterStopApp) {
            callback.onStopApp();
        }
        addEventWhenShutdownApp();
        super.stop();
        System.exit(0);
        Platform.exit();
    }

    public void registerStopApp(IRegisterStopApp callback) {
        this.listCallbackRegisterStopApp.add(callback);
    }

    public static void stopAllAccount() {
        APP_STOPPING = true;
    }

    private void addEventWhenShutdownApp() {
        //Add event when turn off application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown application!");
            WebDriverUtils.quitAll();
        }));
    }
}
