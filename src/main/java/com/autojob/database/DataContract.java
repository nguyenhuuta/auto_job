package com.autojob.database;

public class DataContract {

    static class Preference {

        public static final String TABLE_NAME = "Preference";

        public static final String COLUMN_USER_ID = "id";

        public static final String COLUMN_IS_LOGIN = "IsLogin";

        public static final String COLUMN_HOST = "host";

        public static final String COLUMN_TOKEN = "token";

        public static final String COLUMN_SETTING = "setting";

        public static final String COLUMN_POSITION_CATEGORY = "position_category";

        public static final String COLUMN_TEST = "test";

        private Preference() {
            //no instance
        }
    }

    private DataContract() {
        //no instance
    }
}
