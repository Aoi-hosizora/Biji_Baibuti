package com.baibuti.biji;

import java.util.ArrayList;

import com.baibuti.biji.Note;

public class Data {
    private Data() {}

    private static Data DataInstance;

    public static Data getData() {
        if (DataInstance == null) {
            initAlarm();
            initNote();
            DataInstance = new Data();
        }

        return DataInstance;
    }

    static ArrayList<Alarm> alarmlist;
    static ArrayList<Note> notelist;

    private static void initAlarm() {
        alarmlist = new ArrayList<>();
        if (false)
            alarmlist.add(new Alarm("No Note","Empty"));

        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
        alarmlist.add(new Alarm("First Alarm","Empty"));
    }

    private static void initNote() {
        notelist = new ArrayList<>();
        if (false)
            notelist.add(new Note("No Note","Empty"));

        notelist.add(new Note("Xinki","New"));
        notelist.add(new Note("Xinki","New"));
        notelist.add(new Note("Xinki","New"));
        notelist.add(new Note("Xinki","New"));
        notelist.add(new Note("Xinki","New"));
    }

    public ArrayList<Alarm> getAlarm() {
        return alarmlist;
    }

    public ArrayList<Note> getNote() {
        return notelist;
    }

    public void setAlarmItem(int index, Alarm alarm) {
        alarmlist.set(index, alarm);
    }

    public void setNoteItem(int index, Note note) {
        notelist.set(index, note);
    }
}
