package com.baibuti.biji;

import java.util.ArrayList;

public class Data {
    private Data() {}

    private static Data DataInstance;

    public static Data getData() {
        if (DataInstance == null)
            DataInstance = new Data();
        return DataInstance;
    }

    public ArrayList<String> getAlarm() {
        ArrayList<String> list = new ArrayList<>();
        if (false)
            list.add("No Alarm...");
        list.add("First Alarm");
        list.add("Second Alarm");
        return list;
    }

    public ArrayList<String> getNote() {
        ArrayList<String> list = new ArrayList<>();
        if (false)
            list.add("No Note...");

        list.add("Null");
        return list;
    }
}
