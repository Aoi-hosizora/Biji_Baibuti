package com.baibuti.biji.UI.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baibuti.biji.Data.Adapters.FileAdapter;
import com.baibuti.biji.Data.Models.FileItem;
import com.baibuti.biji.R;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FileImportDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public ImageButton close;
    public TextView title;
    public RecyclerView fileList;
    public List<FileItem> fileListItems = new ArrayList<>();
    public FileAdapter fileAdapter;

    private Thread scanThread;
    private Timer scanTimer;
    private TimerTask scanTask;

    final String[] fileFilter = {".doc", ".docx", ".ppt", ".pptx",
            ".xls", ".xlsx", ".pdf", ".txt", ".zip", ".rar"};

    public FileImportDialog(Activity a) {
        super(a, android.R.style.Widget_Material);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fileimportdialog);
        close = (ImageButton) findViewById(R.id.fileimportdialog_btnClose);
        title = (TextView) findViewById(R.id.fileimportdialog_title_text);
        title.setText("导入文件...");
        title.setTextSize(18);
        title.setTextColor(c.getResources().getColor(R.color.white));
        fileList = (RecyclerView) findViewById(R.id.fileimportdialog_filelist);
        close.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        fileList.setLayoutManager(layoutManager);
        fileAdapter = new FileAdapter(fileListItems);
        fileList.setAdapter(fileAdapter);
        startScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fileimportdialog_btnClose:
                cancelTask();
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void startScan(){
        final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File dir = new File(rootPath);

        scanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                scanFile(dir, fileFilter);
            }
        });

        scanTimer = new Timer();
        scanTask = new TimerTask() {
            @Override
            public void run() {
                if(scanThread.getState() == Thread.State.TERMINATED){
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText("扫描完成");
                            cancelTask();
                        }
                    });
                }
            }
        };

        scanTimer.schedule(scanTask, 0, 1000);

        scanThread.start();
    }

    private void scanFile(File dir, String[] fileFilter) {

        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {

            for (final File file : files) {
                final String fileName = file.getName();
                final String path = file.getAbsolutePath();
                for(String filterName: fileFilter){
                    if(fileName.endsWith(filterName)){
                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                                    fileListItems.add(new FileItem(fileName, path, "doc"));
                                }else if(fileName.endsWith(".ppt") || fileName.endsWith(".pptx")){
                                    fileListItems.add(new FileItem(fileName, path, "ppt"));
                                }else if(fileName.endsWith(".xls") || fileName.endsWith(".xlsx")){
                                    fileListItems.add(new FileItem(fileName, path, "xls"));
                                }else if(fileName.endsWith(".pdf")){
                                    fileListItems.add(new FileItem(fileName, path, "pdf"));
                                }else if(fileName.endsWith(".txt")){
                                    fileListItems.add(new FileItem(fileName, path, "txt"));
                                }else if(fileName.endsWith(".zip")){
                                    fileListItems.add(new FileItem(fileName, path, "zip"));
                                }else{
                                    fileListItems.add(new FileItem(fileName, path, "unknown"));
                                }
                                fileAdapter.notifyItemInserted(fileAdapter.getItemCount());
                                String titleText = "已扫描出" + fileListItems.size() + "个文件";
                                title.setText(titleText);
                            }
                        });
                        break;
                    }
                }

                if (file.isDirectory()) {
                    /*递归扫描*/
                    scanFile(file, fileFilter);
                }
            }
        }
    }

    private void cancelTask() {

        if (scanTask!=null){
            scanTask.cancel();
        }

        if (scanTimer!=null){
            scanTimer.purge();
            scanTimer.cancel();
        }
    }

    public void getFileList(){
        String rootPath = PathUtils.getRootPath();
        String externalStoragePath = PathUtils.getExternalStoragePath();
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.getName().endsWith(".doc")
                        ||file.getName().endsWith(".docx")
                        ||file.getName().endsWith(".ppt")
                        ||file.getName().endsWith(".pptx")
                        ||file.getName().endsWith(".xls")
                        ||file.getName().endsWith(".xlsx")
                        ||file.getName().endsWith(".pdf")
                        ||file.getName().endsWith(".txt")
                        ||file.getName().endsWith(".zip")
                        ||file.getName().endsWith(".rar"))
                    return true;
                return false;
            }
        };
        List<File> temp = FileUtils.listFilesInDirWithFilter(rootPath, fileFilter,true);
        temp.addAll(FileUtils.listFilesInDirWithFilter(externalStoragePath, fileFilter, true));
        for(File f: temp){
            try {
                String name = f.getName();
                String path = f.getCanonicalPath();
                if (f.getName().endsWith(".doc") || f.getName().endsWith(".docx")) {
                    fileListItems.add(new FileItem(name, path, "doc"));
                }else if(f.getName().endsWith(".ppt") || f.getName().endsWith(".pptx")){
                    fileListItems.add(new FileItem(name, path, "ppt"));
                }else if(f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")){
                    fileListItems.add(new FileItem(name, path, "xls"));
                }else if(f.getName().endsWith(".pdf")){
                    fileListItems.add(new FileItem(name, path, "pdf"));
                }else if(f.getName().endsWith(".txt")){
                    fileListItems.add(new FileItem(name, path, "txt"));
                }else if(f.getName().endsWith(".zip")){
                    fileListItems.add(new FileItem(name, path, "zip"));
                }else{
                    fileListItems.add(new FileItem(name, path, "unknown"));
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
