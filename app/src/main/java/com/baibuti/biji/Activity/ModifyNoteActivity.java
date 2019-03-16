package com.baibuti.biji.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;
import com.zzhoujay.richtext.RichText;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText TitleEditText;
    private com.sendtion.xrichtext.RichTextEditor ContentEditText;

    private TextView TypeTextView;

    private Note note;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modifyplainnote);
         getSupportActionBar().show();
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = (Note) getIntent().getSerializableExtra("notedata");

        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        ContentEditText = (com.sendtion.xrichtext.RichTextEditor) findViewById(R.id.id_modifynote_content);
        TypeTextView = (TextView) findViewById(R.id.id_modifynote_type);

        TitleEditText.setText(note.getTitle());
        TypeTextView.setText(note.getUpdateTimeString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu,menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_finish:
                if (TitleEditText.getText().toString().isEmpty())
                    break;
                note.setTitle(TitleEditText.getText().toString());
                // note.setContent(ContentEditText.getText().toString());

                Intent intent = new Intent();

                intent.putExtra("intent_result",true);


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

        }
    }
}
