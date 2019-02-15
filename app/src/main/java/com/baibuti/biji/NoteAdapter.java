package com.baibuti.biji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class NoteAdapter extends ArrayAdapter<Note> {

    private int resourceId;

    public NoteAdapter(Context context, int textViewResourceId, List<Note> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView Title = (TextView) view.findViewById(R.id.id_notelistview_title);
        TextView MakeTime = (TextView) view.findViewById(R.id.id_notelistview_maketime);

        Title.setText(note.getTitle());
        MakeTime.setText(note.getMakeTimeShortString());
        return view;
    }
}
