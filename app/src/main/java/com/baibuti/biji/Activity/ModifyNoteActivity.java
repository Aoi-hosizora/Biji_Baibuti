package com.baibuti.biji.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    // private Button button;
    private EditText TitleEditText;
    private EditText ContentEditText;

    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modifyplainnote);
       //  getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = (Note) getIntent().getSerializableExtra("notedata");

        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        ContentEditText = (EditText) findViewById(R.id.id_modifynote_content);

        TitleEditText.setText(note.getTitle());
        ContentEditText.setText(note.getContent());

        // button = (Button) findViewById(R.id.button);
        // button.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_finish:
                note.setTitle(TitleEditText.getText().toString());
                note.setContent(ContentEditText.getText().toString());

                Intent intent = new Intent();
                intent.putExtra("modify_result",true);
                intent.putExtra("modify_note",note);

                setResult(RESULT_OK,intent);
                finish();
                break;
            case android.R.id.home:

            case R.id.id_menu_modifynote_cancel:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.id_menu_modifynote_finish:
//
//                note.setTitle(TitleEditText.getText().toString());
//                note.setContent(ContentEditText.getText().toString());
//
//                Intent intent = new Intent();
//                intent.putExtra("modify_result",true);
//                intent.putExtra("modify_note",note);
//
//                setResult(RESULT_OK,intent);
//                finish();
//                break;
        }
    }
}
