package com.baibuti.biji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends Activity implements View.OnClickListener {

    private Button button;
    private EditText TitleEditText;
    private EditText ContentEditText;

    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifynote);

        note = (Note) getIntent().getSerializableExtra("notedata");


        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        ContentEditText = (EditText) findViewById(R.id.id_modifynote_content);

        TitleEditText.setText(note.getTitle());
        ContentEditText.setText(note.getContent());

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:

                note.setTitle(TitleEditText.getText().toString());
                note.setContent(ContentEditText.getText().toString());

                Intent intent = new Intent();
                intent.putExtra("modify_result",true);
                intent.putExtra("modify_note",note);

                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }
}
