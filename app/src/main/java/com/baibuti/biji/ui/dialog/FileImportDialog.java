package com.baibuti.biji.ui.dialog;

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

import com.baibuti.biji.ui.adapter.FileItemAdapter;
import com.baibuti.biji.model.vo.FileItem;
import com.baibuti.biji.R;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FileImportDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public ImageButton close;
    public TextView title;
    public RecyclerView fileList;
    public List<FileItem> fileListItems;
    public FileItemAdapter fileItemAdapter;

    private MyThread scanThread;
    private Timer scanTimer;
    private TimerTask scanTask;

    private OnFinishScanListener onFinishScanListener;

    private boolean stopScan = false;

    final String[] fileFilter = {".doc", ".docx", ".ppt", ".pptx",
            ".xls", ".xlsx", ".pdf", ".txt", ".zip", ".rar"};

    public FileImportDialog(Activity a, List<FileItem> l) {
        super(a, android.R.style.Widget_Material);
        fileListItems = l;
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_file_import);
        close = (ImageButton) findViewById(R.id.fileimportdialog_btnClose);
        title = (TextView) findViewById(R.id.fileimportdialog_title_text);
        title.setText("导入文件...");
        title.setTextSize(18);
        title.setTextColor(c.getResources().getColor(R.color.white));
        fileList = (RecyclerView) findViewById(R.id.fileimportdialog_filelist);
        close.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        fileList.setLayoutManager(layoutManager);
        fileItemAdapter = new FileItemAdapter(fileListItems);
        fileList.setAdapter(fileItemAdapter);
        startScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fileimportdialog_btnClose:
                cancelTask();
                stopScan = true;
                if(null != onFinishScanListener){
                    onFinishScanListener.OnFinish();
                }
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void startScan(){
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(rootPath);

        scanThread = new MyThread(dir);

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

        if(stopScan)
            return;

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
                                fileItemAdapter.notifyItemInserted(fileItemAdapter.getItemCount());
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

    public void setOnFinishScanListener(OnFinishScanListener onFinishScanListener){
        this.onFinishScanListener = onFinishScanListener;
    }

    public interface OnFinishScanListener{
        public void OnFinish();
    }

    class MyThread extends Thread {

        private File dir;

        private volatile boolean flag = true;

        public MyThread(File dir){
            this.dir = dir;
        }

        public void stopRunning() {
            flag = false;
        }

        @Override
        public void run() {
            scanFile(dir, fileFilter);
        }
    }

}
