package com.autojob.database;

import java.util.Date;

public class DataContract {

    static class Account {

        public static final String TABLE_NAME = "Account";

        public static final String id = "id";

        public static final String shopId = "shopId";

        public static final String shopName = "shopName";

        public static final String type = "type";

        public static final String lastOrderId = "lastOrderId";

        public static final String rowId = "rowId";

        private Account() {
            //no instance
        }
    }

    private DataContract() {
        //no instance
    }
}
