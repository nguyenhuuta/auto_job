package com.autojob.shopee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by OpenYourEyes on 19/07/2023
 */
public class ShopeeFile {

    static String path() {
        String parentPath = Paths.get("").toAbsolutePath().toString();
        return parentPath + File.separator + "data/shopee.txt";
    }

    public static void createFile() {
        File shopee = new File(path());
        if (!shopee.exists()) {
            try {
                boolean success = shopee.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(String orderId) {
        try {
            FileWriter myWriter = new FileWriter(path());
            myWriter.write(orderId);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile() {
        String data = "";
        try {
            File myObj = new File(path());
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                System.out.println(data);
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


}
