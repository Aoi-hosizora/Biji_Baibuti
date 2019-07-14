package com.baibuti.biji.UI.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baibuti.biji.Data.Adapters.FileAdapter;
import com.baibuti.biji.Data.Models.FileItem;
import com.baibuti.biji.R;

import java.util.ArrayList;
import java.util.List;

public class FileImportDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public ImageButton back, close;
    public RecyclerView fileList;
    public List<FileItem> fileListItems = new ArrayList<>();
    public FileAdapter fileAdapter;

    public FileImportDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fileimportdialog);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.fileimportdialog_layout);

        Resources resources = c.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if(constraintLayout == null)
            Toast.makeText(c, "NULL", Toast.LENGTH_LONG).show();
        else {
            ViewGroup.LayoutParams lp;
            lp= constraintLayout.getLayoutParams();
            lp.width=(int)(0.75 * width);
            lp.height=(int)(0.75 * height);
            constraintLayout.setLayoutParams(lp);
        }
        back = (ImageButton) findViewById(R.id.btnBack);
        close = (ImageButton) findViewById(R.id.btnClose);
        fileList = (RecyclerView) findViewById(R.id.filelist);
        back.setOnClickListener(this);
        close.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        fileList.setLayoutManager(layoutManager);
        getFileList(fileListItems);
        fileAdapter = new FileAdapter(fileListItems);
        fileList.setAdapter(fileAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                c.finish();
                break;
            case R.id.btnClose:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    public List<FileItem> getFileList(List<FileItem> l){
        return l;
    }

}
