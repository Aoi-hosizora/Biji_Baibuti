package com.baibuti.biji.UI.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.UI.Dialog.ImagePopupDialog;
import com.baibuti.biji.R;
import com.baibuti.biji.Utils.OtherUtils.CommonUtil;
import com.baibuti.biji.Utils.LayoutUtils.PopupMenuUtil;
import com.baibuti.biji.Utils.StrSrchUtils.StringUtils;
import com.baibuti.biji.Utils.ImgDocUtils.ToDocUtil;
import com.sendtion.xrichtext.RichTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ViewModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    // region 声明: UI ProgressDialog m_LongClickImgPopupMenu

    private TextView TitleEditText_View;
    private TextView UpdateTimeTextView_View;
    private TextView GroupNameTextView_View;
    private com.sendtion.xrichtext.RichTextView ContentEditText_View;

    private ProgressDialog loadingDialog;
    private ProgressDialog savingDialog;
    private Disposable mDisposable;
    private Dialog m_LongClickImgPopupMenu;

    // endregion 声明: UI ProgressDialog

    // region 声明: Note isModify

    private Note note;
    private boolean isModify = false;

    // endregion 声明: 笔记

    // region 声明: flag REQ

    private int flag = NOTE_NEW;
    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    private static final int REQ_CHOOSE_FOLDER_PATH = 2;

    // endregion 声明: flag REQ

    // region 菜单创建 活动返回 onCreate initPopupMenu onCreateOptionsMenu onBackPressed onActivityResult ShowLogE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmodifynote);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.VMNoteActivity_Title);

        savingDialog = new ProgressDialog(this);
        savingDialog.setCancelable(true);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage(getResources().getString(R.string.VMNoteActivity_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        initPopupMenu();

        note = (Note) getIntent().getSerializableExtra("notedata");
        flag = getIntent().getIntExtra("flag", NOTE_NEW);
        isModify = getIntent().getBooleanExtra("isModify", true);

        TitleEditText_View = (TextView) findViewById(R.id.id_modifynote_viewtitle);
        UpdateTimeTextView_View = (TextView) findViewById(R.id.id_modifynote_viewupdatetime);
        GroupNameTextView_View = (TextView) findViewById(R.id.id_modifynote_viewgroup);
        ContentEditText_View = (com.sendtion.xrichtext.RichTextView) findViewById(R.id.id_modifynote_viewcontent);

        TitleEditText_View.setText(note.getTitle());
        UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
        GroupNameTextView_View.setText(note.getGroupLabel().getName());
        GroupNameTextView_View.setTextColor(note.getGroupLabel().getIntColor());

        //////////////////////////////////////////////////

        ContentEditText_View.post(new Runnable() {
            @Override
            public void run() {
                dealWithContent();
            }
        });
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP)
//            ShowModifyNoteActivity();
//
//        return super.onTouchEvent(event);
//    }

    /**
     * 初始化弹出菜单
     */
    private void initPopupMenu() {
        m_LongClickImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = PopupMenuUtil.initPopupMenu(this, m_LongClickImgPopupMenu, R.layout.popupmenu_vmnote_longclickimg);

        root.findViewById(R.id.id_VMNoteAct_PopupMenu_OCR).setOnClickListener(this);
        root.findViewById(R.id.id_VMNoteAct_PopupMenu_Cancel).setOnClickListener(this);

        m_LongClickImgPopupMenu.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                LongClickImgPath = "";
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewmodifynoteactivity_menu,menu);
        return true;
    }

    /**
     * 由 Modify Note 返回到 View Modify 界面处理
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // MODIFY
                if (resultCode == RESULT_OK) {
                    Note newnote = (Note) data.getSerializableExtra("notedata");

                    // 判断是否修改
                    if (!isModify)
                        isModify = data.getBooleanExtra("isModify", true);

                    if (isModify) {

                        // 使用新的笔记数据
                        note = new Note(newnote);

                        TitleEditText_View.setText(note.getTitle());
                        UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
                        GroupNameTextView_View.setText(note.getGroupLabel().getName());
                        GroupNameTextView_View.setTextColor(note.getGroupLabel().getIntColor());
                        ContentEditText_View.post(new Runnable() {
                            @Override
                            public void run() {
                                // 处理显示数据
                                dealWithContent();
                            }
                        });

                    }
                }
                break;

            case REQ_CHOOSE_FOLDER_PATH:
                if (resultCode == RESULT_OK) {
                    // 返回选择的文件夹路径
                    requestForOpenChooseFolder(data);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 返回键返回主界面
     */
    @Override
    public void onBackPressed() {
        BackToActivity();
    }

    /**
     * 全局设置 Log 格式
     * @param FunctionName
     * @param Msg
     */
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ViewModifyNoteActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    // endregion 菜单创建 活动返回

    // region 工具栏事件处理 onClick onOptionsItemSelected

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_VMNoteAct_PopupMenu_OCR:
                openOCRAct(LongClickImgPath);
                m_LongClickImgPopupMenu.cancel();
            break;
            case R.id.id_VMNoteAct_PopupMenu_Cancel:
                m_LongClickImgPopupMenu.cancel();
            break;
        }
    }

    /**
     * 菜单栏点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_viewmodify:
                ShowModifyNoteActivity();
                break;

            case android.R.id.home:
            case R.id.id_menu_modifynote_viewcancel:
                BackToActivity();
                break;

            case R.id.id_menu_modifynote_viewinfo:
                showDetailInfo();
                break;

            case R.id.id_menu_modifynote_sharenote:
                ShareNoteContent();
                break;

            case R.id.id_menu_modifynote_turntofile:
                OpenChooseFolderForCreateFileAsNote();
                break;
        }
        return true;
    }

    // endregion 工具栏事件处理

    // region 打开编辑 返回主活动 ShowModifyNoteActivity BackToActivity

    /**
     * 打开 ModifyNote 活动
     */
    private void ShowModifyNoteActivity() {
        Intent intent=new Intent(ViewModifyNoteActivity.this, ModifyNoteActivity.class);

        intent.putExtra("notedata",note);
        intent.putExtra("flag",flag);

        startActivityForResult(intent,1); // 1 from CardView
    }

    /**
     * 返回主界面
     */
    private void BackToActivity() {
        Intent motointent = new Intent();

        if (flag == NOTE_NEW) { // Frag -> MN ----> VMN
            motointent.putExtra("notedata", note);
            motointent.putExtra("flag", NOTE_NEW);
        }
        else {
            if (isModify) { // Frag -> VMN -> MN ----> VMN
                motointent.putExtra("notedata", note);
                motointent.putExtra("flag", NOTE_UPDATE);
                setResult(RESULT_OK,motointent);
            }
            else {
                setResult(RESULT_CANCELED,motointent);
            }
        }
        finish();
    }

    // endregion 打开编辑 返回主活动

    // region 保存为文件 OpenChoostFolderForCreateFileAsNote requestForOpenChooseFolder CreateFileAsNote SavingFileAsNote SavingDocxPdfAsNote

    /**
     * 打开保存为文件的路径选择活动
     */
    private void OpenChooseFolderForCreateFileAsNote() {
        Intent savefile_intent=new Intent(this, OpenSaveFileActivity.class);
        savefile_intent.putExtra("isSaving", true);
        savefile_intent.putExtra("FileType", ".docx");
        savefile_intent.putExtra("CurrentDir", ToDocUtil.getDefaultPath());
        savefile_intent.putExtra("FileName", note.getTitle());
        savefile_intent.putExtra("FileFilterType", "docx|pdf");

        // 返回含后缀名的文件名，并且单独返回后缀名
        savefile_intent.putExtra("isReturnType", "true");

        startActivityForResult(savefile_intent, REQ_CHOOSE_FOLDER_PATH);
    }

    /**
     * 返回选择的文件路径
     * -> OpenChooseFolderForCreateFileAsNote()访问OpenSaveFileActivity
     * @param data Intent
     */
    private void requestForOpenChooseFolder(Intent data) {
        // isReturnType == true
        String path = data.getStringExtra("path"); // 包含后缀名
        String type = data.getStringExtra("type");

        if (!path.isEmpty()) {
            ShowLogE("requestForOpenChooseFolder", "path: " + path);

            if (!".pdf".equals(type) && !".docx".equals(type))
                type = ".docx";
            CreateFileAsNote(path, type);
        }
    }

    /**
     * 新建笔记文件，还未分开指定类型
     * @param path 已经包含的后缀名的路径
     * @param type .pdf|.docx
     */
    private void CreateFileAsNote(final String path, String type) {

        if (".pdf".equals(type))
            type = "PDF";
        else
            type = "DOCX";

        final boolean isSaveAsDocx = "DOCX".equals(type);

        String msg = String.format(getString(R.string.VMNoteActivity_CreateFileAsNoteMessage), type, path);
        // 保存为 PDF 时的提醒
        if (!isSaveAsDocx)
            msg += getString(R.string.VMNoteActivity_CreateFileAsNoteMessageHint);

        // 确定所选择的类型以保存
        AlertDialog TypealertDialog = new AlertDialog
                .Builder(this)
                .setTitle(R.string.VMNoteActivity_CreateFileAsNoteTitle)
                .setMessage(msg)
                .setPositiveButton(R.string.VMNoteActivity_CreateFileAsNotePositiveForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavingFileAsNote(isSaveAsDocx, note, path);
                    }
                })
                .setNegativeButton(R.string.VMNoteActivity_CreateFileAsNoteNegativeForCancel, null).create();
        TypealertDialog.show();

    }

    /**
     * 根据类型保存为 Pdf 还是 Docx，CreateFileAsNote()用
     * @param isSaveAsDocx
     * @param mnote
     * @param filename 包含后缀名
     */
    public void SavingFileAsNote(final boolean isSaveAsDocx, final Note mnote, String filename) {

        File file = new File(filename);
        final String FilePath = file.getPath();

        // 判断文件是否存在
        if (file.exists()) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(this)
                    .setTitle(R.string.VMNoteActivity_SavingFileAsNoteTitle)
                    .setMessage(String.format(getResources().getString(R.string.VMNoteActivity_SavingFileAsNoteMessage), FilePath))
                    .setPositiveButton(R.string.VMNoteActivity_SavingFileAsNotePositiveForCover, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SavingDocxPdfAsNote(isSaveAsDocx, FilePath, mnote);
                        }
                    })
                    .setNegativeButton(R.string.VMNoteActivity_SavingFileAsNoteNegativeForCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }) .create();
            alertDialog.show();
        }
        else
            SavingDocxPdfAsNote(isSaveAsDocx, FilePath, mnote);
    }

    /**
     * 具体保存笔记过程，SavingFileAsNote()用
     * @param Path
     * @param mnote
     */
    private void SavingDocxPdfAsNote(final boolean isSaveAsDocx, final String Path, final Note mnote) {
        ShowLogE("SavingDocxAsNote", "Path: " + Path);

        class HasDismiss {
            private boolean dismiss = false;
            HasDismiss() {}
            void setDismiss() { this.dismiss = true; }
            boolean getDismiss() { return this.dismiss; }
        }
        final HasDismiss isHasDismiss = new HasDismiss();

        // %s 文件正在保存中...\n\n%s\n
        String Msg = String.format(getResources().getString(R.string.VMNoteActivity_SavingDataMessage), isSaveAsDocx ? "Docx" : "Pdf", Path);
        savingDialog.setTitle(getResources().getString(R.string.VMNoteActivity_SavingData));
        savingDialog.setMessage(Msg);
        savingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // 取消保存
                isHasDismiss.setDismiss();
            }
        });
        savingDialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (isSaveAsDocx)
                                ToDocUtil.CreateDocxByNote(Path, mnote.getTitle(), mnote.getContent(), true);
                            else
                                ToDocUtil.CreatePdfByNote(Path, mnote.getTitle(), mnote.getContent(), true);

                            if (!isHasDismiss.getDismiss()) {
                                if (savingDialog != null)
                                    savingDialog.dismiss();

                                Toast.makeText(ViewModifyNoteActivity.this, "文件 " + Path + " 保存成功。", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                ShowLogE("SavingDocxPdfAsNote", "Saning is HasDismiss");
                                File f = new File(Path);
                                if (f.exists())
                                    f.delete();
                            }

                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    // endregion 其他功能

    // region 其他功能 showDetailInfo ShowClickImg ShareNoteContent LongClickImgPath openOCRAct

    /**
     * 显示笔记详细信息
     */
    private void showDetailInfo() {
        final String Info = getResources().getString(R.string.VMNoteActivity_InfoTitle) + note.getTitle() + "\n" +
                getResources().getString(R.string.VMNoteActivity_InfoCreateTime) + note.getCreateTime_FullString() + "\n" +
                getResources().getString(R.string.VMNoteActivity_InfoUpdateTime) + note.getUpdateTime_FullString() + "\n\n" +
                getResources().getString(R.string.VMNoteActivity_InfoGroupLabelTitle) + note.getGroupLabel().getName();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.VMNoteActivity_InfoAlertTitle)
                .setMessage(Info)
                .setNeutralButton(R.string.VMNoteActivity_InfoAlertNeutralButtonForCopy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(getResources().getString(R.string.VMNoteActivity_InfoAlertClipDataLabel), Info);
                        clipboardManager.setPrimaryClip(clip);
                        Toast.makeText(ViewModifyNoteActivity.this, R.string.VMNoteActivity_InfoAlertCopySuccess, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.VMNoteActivity_InfoAlertNegativeButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

    }

    /**
     * 记录长按图片序号
     *
     * -1: 没有长按
     */
    private String LongClickImgPath = "";

    /**
     * 点击图片后弹出预览窗口，待改
     * @param imageList
     * @param currentPosition
     */
    private void ShowClickImg(ArrayList<String> imageList, int currentPosition) {
        try {
            String[] imgs = imageList.toArray(new String[imageList.size()]);
            ImagePopupDialog dialog = new ImagePopupDialog(this, imgs, currentPosition);
            dialog.setOnLongClickImageListener(new ImagePopupDialog.onLongClickImageListener() {

                @Override
                public void onLongClick(View v, int index) {
                    LongClickImgPath = imgs[index];
                    m_LongClickImgPopupMenu.show();
                }
            });
            dialog.show();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 对图片 打开OCR活动
     * @param imgPath
     */
    private void openOCRAct(String imgPath) {
        Intent intent = new Intent(ViewModifyNoteActivity.this, OCRActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(OCRActivity.INT_IMGPATH, imgPath);

        intent.putExtra(OCRActivity.INT_BUNDLE, bundle);
        startActivity(intent);
    }

    /**
     * 分享笔记，待修改
     */
    private void ShareNoteContent() {
        CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null); //分享图文
    }

    // endregion 其他功能

    // region 文字图片显示处理 dealWithContent showDataSync showEditData

    /**
     * 处理内容，重要
     * ContentEditText.post(new Runnable() -> dealWithContent(););
     */
    private void dealWithContent() {
        //showEditData(myContent);
        ContentEditText_View.clearAllLayout();
        showDataSync(note.getContent());

        final AppCompatActivity app = this;

        // 图片点击事件
        ContentEditText_View.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                ArrayList<String> imageList = StringUtils.getTextFromHtml(note.getContent(), true);
                int currentPosition = imageList.indexOf(imagePath);

                ShowLogE("dealWithContent", "点击图片："+currentPosition+"："+imagePath);

                ShowClickImg(imageList, currentPosition);
            }
        });
    }

    /**
     * 异步方式显示数据
     * @param html
     */
    private void showDataSync(final String html){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
        //.onBackpressureBuffer()
        .subscribeOn(Schedulers.io()) //生产事件在io
        .observeOn(AndroidSchedulers.mainThread()) //消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onComplete() {
                if (loadingDialog != null)
                    loadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                // Toast.makeText(ViewModifyNoteActivity.this, R.string.VMNoteActivity_showDataSyncError, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(String text) {
                try {
                    if (ContentEditText_View != null) {
                        if (text.contains("<img") && text.contains("src=")) {
                            // imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtils.getImgSrc(text);
                            ContentEditText_View.addImageViewAtIndex(ContentEditText_View.getLastIndex(), imagePath);
                        } else {
                            ContentEditText_View.addTextViewAtIndex(ContentEditText_View.getLastIndex(), text);
                        }
                    }
                }
                catch (Exception ex) {
                    Toast.makeText(ViewModifyNoteActivity.this, "笔记中图片显示错误，可能由于源文件被删除。", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * 显示数据
     * @param html
     */
    private void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    // endregion 文字图片显示处理
}
