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
import android.widget.TextView;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    // private Button button;
    private EditText TitleEditText;
    private EditText ContentEditText;
    private TextView TypeTextView;

    private Note note;
    // private boolean IsNewData ;
    private boolean IsMarkDown;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modifyplainnote);
        //  getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = (Note) getIntent().getSerializableExtra("notedata");
        IsMarkDown = note.getIsMarkDown();

//        IsNewData = false;
//        if (note.getTitle().isEmpty() && note.getContent().isEmpty())
//            IsNewData = true;

        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        ContentEditText = (EditText) findViewById(R.id.id_modifynote_content);
        TypeTextView = (TextView) findViewById(R.id.id_modifynote_type);


        TitleEditText.setText(note.getTitle());
        ContentEditText.setText(note.getContent());
        TypeTextView.setText((note.getIsMarkDown() ? "MarkDown" : "PlainNote") + " - " + note.getMakeTimeString());

        // button = (Button) findViewById(R.id.button);
        // button.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu, menu);
        mMenu = menu;
        if (IsMarkDown == false) {
            mMenu.findItem(R.id.id_menu_modifynote_changeplain).setVisible(false);
            mMenu.findItem(R.id.id_menu_modifynote_showmarkdown).setVisible(false);
        } else {
            mMenu.findItem(R.id.id_menu_modifynote_changemd).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_finish:
                note.setTitle(TitleEditText.getText().toString());
                note.setContent(ContentEditText.getText().toString());

                Intent intent = new Intent();
                intent.putExtra("intent_result", true);

//                if (IsNewData)
//                    intent.putExtra("new_note",note);
//                else
                intent.putExtra("modify_note", note);

                setResult(RESULT_OK, intent);
                finish();
                break;
            case android.R.id.home:

            case R.id.id_menu_modifynote_cancel:
                finish();
                break;

            case R.id.id_menu_modifynote_changeplain:
                note.setIsMarkDown(false);
                TypeTextView.setText((note.getIsMarkDown() ? "MarkDown" : "PlainNote") + " - " + note.getMakeTimeString());
                mMenu.findItem(R.id.id_menu_modifynote_changeplain).setVisible(false);
                mMenu.findItem(R.id.id_menu_modifynote_changemd).setVisible(true);

                mMenu.findItem(R.id.id_menu_modifynote_showmarkdown).setVisible(false);
                break;

            case R.id.id_menu_modifynote_changemd:
                note.setIsMarkDown(true);
                TypeTextView.setText((note.getIsMarkDown() ? "MarkDown" : "PlainNote") + " - " + note.getMakeTimeString());
                mMenu.findItem(R.id.id_menu_modifynote_changemd).setVisible(false);
                mMenu.findItem(R.id.id_menu_modifynote_changeplain).setVisible(true);

                mMenu.findItem(R.id.id_menu_modifynote_showmarkdown).setVisible(true);

                break;

            case R.id.id_menu_modifynote_showmarkdown:


                break;
        }
        return super.onOptionsItemSelected(item);
    }
}