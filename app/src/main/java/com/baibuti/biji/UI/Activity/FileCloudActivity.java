package com.baibuti.biji.UI.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baibuti.biji.R;

public class FileCloudActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_cloud);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.FileCloudActivity_Toolbar_Title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filecloudactivity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.id_menu_filecloudactivity_newfoler:
                //TODO
                break;
            case R.id.id_menu_filecloudactivity_tranmanage:
                //TODO
                break;
            case R.id.id_menu_filecloudactivity_refresh:
                //TODO
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
