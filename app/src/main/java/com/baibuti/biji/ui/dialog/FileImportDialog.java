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

    private Activity activity;
    public TextView title;
    private List<FileItem> fileListItems;
    private FileItemAdapter fileItemAdapter;

    private Timer scanTimer;
    private TimerTask scanTask;

    private OnFinishScanListener onFinishScanListener;

    private boolean stopScan = false;

    private final String[] fileFilter = {".doc", ".docx", ".ppt", ".pptx",
        ".xls", ".xlsx", ".pdf", ".txt", ".zip", ".rar"};

    public FileImportDialog(Activity a, List<FileItem> l) {
        super(a, android.R.style.Widget_Material);
        fileListItems = l;
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_file_import);

        ImageButton ok_btn = findViewById(R.id.fileimportdialog_btnOk);
        ok_btn.setOnClickListener(this);

        ImageButton btn_back = findViewById(R.id.fileimportdialog_back);
        btn_back.setOnClickListener(this);

        title = findViewById(R.id.fileimportdialog_title_text);
        title.setText("导入文件...");

        RecyclerView fileList = findViewById(R.id.fileimportdialog_filelist);
        fileList.setLayoutManager(new LinearLayoutManager(getContext()));
        fileItemAdapter = new FileItemAdapter(fileListItems);
        fileList.setAdapter(fileItemAdapter);
        startScan();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fileimportdialog_btnOk) {
            cancelTask();
            stopScan = true;
            if (null != onFinishScanListener)
                onFinishScanListener.OnFinish();
            dismiss();
        } else if (v.getId() == R.id.fileimportdialog_back) {
            cancelTask();
            stopScan = true;
            dismiss();
        }
    }

    private void startScan() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File dir = new File(rootPath);

        Thread thread = new Thread(() -> scanFile(dir, fileFilter));

        scanTimer = new Timer();
        scanTask = new TimerTask() {
            @Override
            public void run() {
                if (thread.getState() == Thread.State.TERMINATED) {
                    activity.runOnUiThread(() -> {
                        title.setText("扫描完成");
                        cancelTask();
                    });
                }
            }
        };

        scanTimer.schedule(scanTask, 0, 1000);

        thread.start();
    }

    private void scanFile(File dir, String[] fileFilter) {

        if (stopScan) return;

        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {

            for (final File file : files) {
                final String fileName = file.getName();
                final String path = file.getAbsolutePath();
                for (String filterName : fileFilter) {
                    if (fileName.endsWith(filterName)) {
                        activity.runOnUiThread(() -> {
                            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                                fileListItems.add(new FileItem(fileName, path, "doc"));
                            } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                                fileListItems.add(new FileItem(fileName, path, "ppt"));
                            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                                fileListItems.add(new FileItem(fileName, path, "xls"));
                            } else if (fileName.endsWith(".pdf")) {
                                fileListItems.add(new FileItem(fileName, path, "pdf"));
                            } else if (fileName.endsWith(".txt")) {
                                fileListItems.add(new FileItem(fileName, path, "txt"));
                            } else if (fileName.endsWith(".zip")) {
                                fileListItems.add(new FileItem(fileName, path, "zip"));
                            } else {
                                fileListItems.add(new FileItem(fileName, path, "unknown"));
                            }
                            fileItemAdapter.notifyItemInserted(fileItemAdapter.getItemCount());
                            String titleText = "已扫描出" + fileListItems.size() + "个文件";
                            title.setText(titleText);
                        });
                        break;
                    }
                }
                if (file.isDirectory())
                    scanFile(file, fileFilter);
            }
        }
    }

    private void cancelTask() {
        if (scanTask != null)
            scanTask.cancel();

        if (scanTimer != null) {
            scanTimer.purge();
            scanTimer.cancel();
        }
    }

    public void setOnFinishScanListener(OnFinishScanListener onFinishScanListener) {
        this.onFinishScanListener = onFinishScanListener;
    }

    public interface OnFinishScanListener {
        void OnFinish();
    }
}
