package com.webseleniumdriver.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class PreferenceUtils {
    private static final String USER_DATA_PATH = "data/user.txt";
    private static final String APP_CONFIG_PATH = "data/config.txt";

//    public static void saveUserLogin(User user) {
//        String userJson = JsonParser.toString(user);
//        writeFile(EncryptUtils.encrypt(userJson), USER_DATA_PATH);
//    }

//    public static User getUserLogin() {
//        String data = EncryptUtils.decrypt(readFile(USER_DATA_PATH));
//        if (data == null || data.isEmpty()) {
//            return null;
//        } else {
//            return JsonParser.fromString(data, User.class);
//        }
//    }
//
//    public static void saveAccountData(AccountData accountData) {
//        String accountDataJson = JsonParser.toString(accountData);
//        writeFile(accountDataJson, "data/" + accountData.getAccountId() + "_data.txt");
//    }
//
//    public static AccountData getAccountData(String accountId) {
//        String data = readFile("data/" + accountId + "_data.txt");
//        if (data == null || data.isEmpty()) {
//            return new AccountData(accountId);
//        } else {
//            try {
//                AccountData accountData = JsonParser.fromString(data, AccountData.class);
//                if (accountData == null) {
//                    return new AccountData(accountId);
//                } else {
//                    return accountData;
//                }
//            } catch (Exception ex) {
//                return new AccountData(accountId);
//            }
//        }
//    }




    private static String readFile(String inputPath) {
        String line;
        String output = "";
        try {
            FileReader fileReader = new FileReader(inputPath);
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                output += line;
            }
            bufferedReader.close();
        } catch (Exception ex) {
        }
        return output;
    }

    private static void writeFile(String input, String path) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(input);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
