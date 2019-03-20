package com.baibuti.biji.Data;

import java.util.ArrayList;

public class Data {
    private Data() {}

    private static Data DataInstance;

    public static Data getData() {
        if (DataInstance == null) {
            initNote();
            DataInstance = new Data();
        }

        return DataInstance;
    }

    static ArrayList<Note> notelist;

    private static void initNote() {
        notelist = new ArrayList<>();
        if (false)
            notelist.add(new Note("No Note","Empty"));

        for( int i = 0 ; i < 10 ; i++ ) {
            notelist.add(new Note("Xinki", "New"));
        }
    }

    public ArrayList<Note> getNote() {
        return notelist;
    }

    public void setNoteItem(int index, Note note) {
        notelist.set(index, note);
    }
}
