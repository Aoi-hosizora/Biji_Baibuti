package com.baibuti.biji;

import java.util.ArrayList;

import com.baibuti.biji.Note;

public class Data {
    private Data() {}

    private static Data DataInstance;

    public static Data getData() {
        if (DataInstance == null)
            DataInstance = new Data();
        return DataInstance;
    }

    public ArrayList<Alarm> getAlarm() {
        ArrayList<Alarm> list = new ArrayList<>();
        if (false)
            list.add(new Alarm("No Note","Empty"));

        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));
        list.add(new Alarm("First Alarm","Empty"));

        return list;
    }

    public ArrayList<Note> getNote() {
        ArrayList<Note> list = new ArrayList<>();
        if (false)
            list.add(new Note("No Note","Empty"));

        list.add(new Note("Xinki","New"));
        list.add(new Note("Xinki","New"));
        list.add(new Note("Xinki","New"));
        list.add(new Note("Xinki","New"));
        list.add(new Note("Xinki","New"));

        return list;
    }
}
