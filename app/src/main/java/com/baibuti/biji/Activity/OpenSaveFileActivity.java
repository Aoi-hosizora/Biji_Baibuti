package com.baibuti.biji.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenSaveFileActivity extends AppCompatActivity implements View.OnClickListener, IShowLog {

    private static final int SELECT_FILE_OPEN = 0;
    private static final int SELECT_FILE_SAVE = 1;
    /**
     * 当前Intent的执行类型
     *      SELECT_FILE_OPEN 打开文件
     *      SELECT_FILE_SAVE 保存文件
     */
    private int Select_type = SELECT_FILE_SAVE;

    private TextView m_PathDirTextView;
    private ListView m_dirListView;
    private Button m_NewFolderButton;
    private Button m_CancelButton;
    private Button m_OKButton;
    private EditText m_FileNameEditText;
    private Spinner m_FileTypeSpinner;

    private String[] FileType = {".docx",".pdf"};
    /**
     * 文件保存名，可能包含后缀名
     */
    private String Selected_File_Name = "New_File";
    /**
     * 文件保存类型
     *      .docx
     *      .pdf
     */
    private String Selected_File_Type;

    /**
     * 通过文件类型过滤文件显示
     * eg: docx|pdf
     */
    private String FileFilterType;

    private String m_sdcardDirectory = "";
    private String m_dir = "";
    private List<String> m_subdirs;
    private ArrayAdapter<String> m_listAdapter;

    /**
     * 四个 Extra:
     *      isSaving
     *      FileName
     *      FileType
     *      CurrentDir
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensavefile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean isSaving = getIntent().getBooleanExtra("isSaving", true);
        if (isSaving) {
            Select_type = SELECT_FILE_SAVE;
            setTitle("保存文件");
        }
        else {
            Select_type = SELECT_FILE_OPEN;
            setTitle("打开文件");
        }

        Selected_File_Name = getIntent().getStringExtra("FileName");

        String fileType = getIntent().getStringExtra("FileType");
        Selected_File_Type = ".docx";
        if (".pdf".equals(fileType))
            Selected_File_Type = ".pdf";

        m_dir = getIntent().getStringExtra("CurrentDir");

        FileFilterType = getIntent().getStringExtra("FileFilterType");
        //////////

        m_PathDirTextView = findViewById(R.id.id_OpenSaveFile_PathDirTextView);
        m_PathDirTextView.setText("当前路径: " + m_dir);
        m_dirListView = findViewById(R.id.id_OpenSaveFile_DirList);
        m_NewFolderButton = findViewById(R.id.id_OpenSaveFile_NewFolderButton);
        m_CancelButton = findViewById(R.id.id_OpenSaveFile_CancelButton);
        m_OKButton = findViewById(R.id.id_OpenSaveFile_OKButton);
        if (isSaving)
            m_OKButton.setText("保存");
        else
            m_OKButton.setText("打开");

        m_FileTypeSpinner = findViewById(R.id.id_OpenSaveFile_FileTypeSpinner);
        m_FileNameEditText = findViewById(R.id.id_OpenSaveFile_FileNameEditText);
        m_FileNameEditText.setText(Selected_File_Name);

        m_NewFolderButton.setOnClickListener(this);
        m_CancelButton.setOnClickListener(this);
        m_OKButton.setOnClickListener(this);

        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        }
        catch (IOException ioe) { }

        setupSpinner();

        if (m_dir.equals(""))
            setupDirList(m_sdcardDirectory);
        else
            setupDirList(m_dir);

    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "OpenSaveFileActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 根据 dir 显示文件列表的初始位置，
     * 并且设置单击列表项的文件夹跳转以及文件选择 -> m_dirListView.setOnItemClickListener
     * @param dir
     */
    private void setupDirList(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = m_sdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        m_dir = dir;
        m_subdirs = getDirectories(dir);

        m_listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, m_subdirs);
        m_dirListView.setAdapter(m_listAdapter);

        m_dirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String m_dir_old = m_dir;
                String sel = m_subdirs.get(position);

                if (sel.charAt(sel.length() - 1) == '/')
                    sel = sel.substring(0, sel.length() - 1);

                // Navigate into the sub-directory
                if (sel.equals("..")) {
                    m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
                } else {
                    m_dir += "/" + sel;
                }

                // Click File
                if ((new File(m_dir).isFile())) {
                    m_dir = m_dir_old;
                    Selected_File_Name = sel;
                }

                updateDirectory();
            }
        });
    }

    /**
     * 设置文件类型选择的 Spinner 弹出菜单
     */
    private void setupSpinner() {
        m_FileTypeSpinner.setDropDownWidth(100);
        m_FileTypeSpinner.setDropDownHorizontalOffset(100);
        m_FileTypeSpinner.setDropDownVerticalOffset(-120);

        // 显示
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.modulelayout_opensavefile_spinneritemselect, FileType);
        // 下拉
        spinnerAdapter.setDropDownViewResource(R.layout.modulelayout_opensavefile_spinneritemdrop);
        //spinnerAdapter.setDropDownViewTheme(Theme.LIGHT);
        m_FileTypeSpinner.setAdapter(spinnerAdapter);

        m_FileTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selected_File_Type = FileType[position];
                ShowLogE("setupSpinner", "Selected_File_Type=" + Selected_File_Type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_OpenSaveFile_OKButton:
                OKButton_Click();
            break;
            case R.id.id_OpenSaveFile_CancelButton:
                CancelButton_Click();
            break;
            case R.id.id_OpenSaveFile_NewFolderButton:
                NewFolderButton_Click();
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AndroidHome_Click();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 系统返回按钮，实现返回上一级目录以及退出选择文件
     */
    private void AndroidHome_Click() {
        if (m_subdirs.get(0).equals("..")) {
            m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
            updateDirectory();
        }
        else {
            CancelButton_Click();
        }
    }

    /**
     * 确定打开或者保存
     */
    private void OKButton_Click() {
        BackToActivity(true);
    }

    /**
     * 取消操作返回上一活动
     */
    private void CancelButton_Click() {
        BackToActivity(false);
    }

    private String getFileNameWithoutEx() {
        String filename = m_dir + "/" + Selected_File_Name;
        if (filename.contains(".")) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length())))
                filename = filename.substring(0, dot);
        }
        return filename;
    }

    /**
     * 返回上一活动，并且传送信息
     * @param IsSend 是否传递信息
     */
    private void BackToActivity(boolean IsSend) {
        Intent BackIntent = new Intent();
        if (Select_type == SELECT_FILE_SAVE) {
            if (IsSend) {
                BackIntent.putExtra("path", getFileNameWithoutEx() + Selected_File_Type);
                BackIntent.putExtra("type", Selected_File_Type);
                setResult(RESULT_OK, BackIntent);
            }
            else {
                setResult(RESULT_CANCELED, BackIntent);
            }
            finish();
        }
        else {
            // 未涉及
        }
    }

    /**
     * 新建文件夹
     */
    private void NewFolderButton_Click() {
        final EditText input = new EditText(this);
        AlertDialog new_folder_dialog = new AlertDialog.Builder(this)
                .setTitle("新建文件夹")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        if (newDirName.isEmpty())
                            Toast.makeText(OpenSaveFileActivity.this, "请输入文件夹名。", Toast.LENGTH_SHORT).show();
                        else {
                            if (createSubDir(m_dir + "/" + newDirName)) {
                                // m_dir += "/" + newDirName;
                                updateDirectory();
                                String msg = String.format("文件夹 %s 创建成功。", newDirName);
                                Toast.makeText(OpenSaveFileActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String msg = String.format("无法创建文件夹 %s。", newDirName);
                                Toast.makeText(OpenSaveFileActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                })
                .setNegativeButton("取消", null)
                .create();

        new_folder_dialog.show();
    }

    // region 文件夹操作 createSubDir updateDirectory getDirectories

    /**
     * 新建文件夹，NewFolderButton_Click() 用
     * @param newDir
     * @return
     */
    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists())
            return newDirFile.mkdir();
        else
            return false;
    }

    /**
     * 通过 m_dir 更新当前文件夹信息
     * 通过 getDirectories()
     */
    private void updateDirectory() {
        m_subdirs.clear();
        m_subdirs.addAll(getDirectories(m_dir));
        m_PathDirTextView.setText("当前路径: " + m_dir);
        m_listAdapter.notifyDataSetChanged();
        // #scorch
        if (Select_type == SELECT_FILE_SAVE || Select_type == SELECT_FILE_OPEN) {
            m_FileNameEditText.setText(Selected_File_Name);
        }
    }

    /**
     * 显示文件夹内容，并实现排序
     * @param dir
     * @return
     */
    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();
        try {
            File dirFile = new File(dir);

            // 非根目录添加 ".."
            if (!m_dir.equals(m_sdcardDirectory))
                dirs.add("..");

            // 目录不存在，返回 ".."
            if (!dirFile.exists() || !dirFile.isDirectory())
                return dirs;

            // 正常情况，列出所有内容
            for (File file : dirFile.listFiles()) {

                if (file.isDirectory()) // 列表项为文件夹，添加 "/"
                    dirs.add(file.getName() + "/");

                else // 列表项为文件
                    if (!IsFilteredFileName(file.getName())) // 非过滤文件
                        dirs.add(file.getName());
            }
        }
        catch (Exception e) { }

        // 文件夹与文件的排序
        Collections.sort(dirs, new Comparator<String>() {

            public int compare(String o1, String o2) {
                // .. > others
                if ("..".equals(o1))
                    return -1;
                // others < ..
                if ("..".equals(o2))
                    return +1;
                // folder > file
                if (o1.contains("/") && !o2.contains("/"))
                    return -1;
                // file < folder
                if (!o1.contains("/") && o2.contains("/"))
                    return +1;

                // others
                return o1.toUpperCase().compareTo(o2.toUpperCase());
            }
        });
        return dirs;
    }

    /**
     * 根据文件名和 FileFilterType 过滤文件显示
     * @param filename
     * @return
     *      true: 过滤不显示
     *      false: 不过滤显示
     */
    private boolean IsFilteredFileName(String filename) {
        // pdf|docx
        String[] FilterList = FileFilterType.split("\\|");
        for (String filter : FilterList) {
            if (filename.contains("." + filter)) {
                return false;
            }
        }
        return true;
    }

    // endregion 文件夹操作

}
