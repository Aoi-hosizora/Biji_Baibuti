package com.baibuti.biji.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.ui.adapter.GroupAdapter;
import com.baibuti.biji.data.model.Note;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.respObj.UploadStatus;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.module.note.ImgUtil;
import com.baibuti.biji.R;
import com.baibuti.biji.data.dao.db.GroupDao;
import com.baibuti.biji.data.dao.db.NoteDao;
import com.baibuti.biji.ui.dialog.ImagePopupDialog;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.fileDirUtil.FilePathUtil;
import com.baibuti.biji.util.imgDocUtil.ImageUtil;
import com.baibuti.biji.util.layoutUtil.PopupMenuUtil;
import com.baibuti.biji.util.fileDirUtil.SDCardUtil;
import com.baibuti.biji.util.strSrchUtil.StringUtil;
import com.baibuti.biji.util.otherUtil.ExtractUtil;
import com.sendtion.xrichtext.RichTextEditor;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.kareluo.imaging.IMGEditActivity;

import static com.baibuti.biji.util.otherUtil.ExtractUtil.assets2SD;


public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    // region 声明: UI ProgressDialog Menu PopupMenu
    
    private EditText TitleEditText;
    private TextView UpdateTimeTextView;
    private TextView GroupNameTextView;
    private com.sendtion.xrichtext.RichTextEditor ContentEditText;

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private Disposable subsLoading;
    private Disposable subsInsert;
    private ProgressDialog idenLoadingDialog;
    
    private Menu menu;
    private Dialog mInsertImgPopupMenu;
    private Dialog mLongClickImgPopupMenu;
    
    // endregion 声明: UI ProgressDialog Menu
    
    // region 声明: Note Dao List<Group> GroupAdapter selectedGropId
    
    private Note note;
    private GroupDao groupDao;
    private NoteDao noteDao;
    private List<Group> GroupList;
    private GroupAdapter groupAdapter;

    private int selectedGropId = 0;
    // endregion 声明: Note Dao List<Group> GroupAdapter selectedGropId
    
    // region 声明: flag CUT_LENGTH screen
    
    /**
     * NOTE_NEW 0
     * NOTE_UPDATE 1
     */
    private int flag; // 0: NEW, 1: UPDATE
    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    public final int CUT_LENGTH = 17;
    private int screenWidth;
    private int screenHeight;
    
    // endregion 声明: flag CUT_LENGTH screen
    
    // region 声明: region REQ img PERMISSION
    
    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
    private static final int REQUEST_CROP = 1;// 裁剪
    private static final int SCAN_OPEN_PHONE = 2;// 相册

    private Uri imgUri; // 拍照时返回的uri
    /**
     *  返回活动时是否删除图片
     */
    private boolean isTakePhoto_Delete = false;

    private static final int REQUEST_PERMISSION = 100;
    private boolean hasPermission = false;

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int PICK_REQUEST_CODE = 10;

    // endregion 声明: region REQ img PERMISSION

    // region 菜单创建 活动返回 onCreate initPopupMenu onCreateOptionsMenu onBackPressed ShowPopMenu onActivityResult ShowLogE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifynote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage(getResources().getString(R.string.MNoteActivity_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage(getResources().getString(R.string.MNoteActivity_LoadingImg));
        insertDialog.setCanceledOnTouchOutside(false);

        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);
        new Thread(new Runnable() {
            @Override
            public void run() {

                GroupList = groupDao.queryAllGroups();
                groupAdapter = new GroupAdapter(ModifyNoteActivity.this, GroupList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        groupAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

        note = (Note) getIntent().getSerializableExtra("notedata");
        flag = getIntent().getIntExtra("flag", NOTE_NEW);

        if (flag == NOTE_NEW) {
            setTitle(R.string.NMoteActivity_TitleForNewNote);
            note.setGroup(groupDao.queryDefaultGroup(), true);
        }
        else
            setTitle(R.string.NMoteActivity_TitleForUpdateNote);

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);


        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        UpdateTimeTextView = (TextView) findViewById(R.id.id_modifynote_updatetime);
        GroupNameTextView = (TextView) findViewById(R.id.id_modifynote_group);
        ContentEditText = (com.sendtion.xrichtext.RichTextEditor) findViewById(R.id.id_modifynote_content);


        TitleEditText.setText(note.getTitle());
        UpdateTimeTextView.setText(note.getUpdateTime_ShortString());
        GroupNameTextView.setText(note.getGroup().getName());
        GroupNameTextView.setTextColor(note.getGroup().getIntColor());
        selectedGropId = note.getGroup().getId();

        initPopupMenu();

        //////////////////////////////////////////////////
        // ContentEditText

        ContentEditText.post(new Runnable() {
            @Override
            public void run() {
                dealWithContent();
            }
        });
    }

    /**
     * 初始化弹出菜单
     */
    private void initPopupMenu() {
        mLongClickImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = PopupMenuUtil.initPopupMenu(this, mLongClickImgPopupMenu, R.layout.popupmenu_mnote_longclickimg);

        root.findViewById(R.id.id_MNoteAct_PopupMenu_OCR).setOnClickListener(this);
        root.findViewById(R.id.id_MNoteAct_PopupMenu_OCRCancel).setOnClickListener(this);

        mLongClickImgPopupMenu.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                LongClickImgPath = "";
            }
        });
    }

    /**
     * 获取 Menu 实例
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu, menu);
        this.menu = menu;
        return true;
    }

    /**
     * 返回键取消保存判断
     */
    @Override
    public void onBackPressed() {
        CancelSaveNoteData();
    }


    /**
     * 显示下部弹出图片选择菜单
     */
    private void ShowPopMenu() {
        mInsertImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.popupmenu_mnote_insertimg, null);

        //初始化视图
        root.findViewById(R.id.id_popmenu_choose_img).setOnClickListener(this);
        root.findViewById(R.id.id_popmenu_open_camera).setOnClickListener(this);
        root.findViewById(R.id.id_popmenu_cancel).setOnClickListener(this);

        mInsertImgPopupMenu.setContentView(root);
        Window dialogWindow = mInsertImgPopupMenu.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度

        dialogWindow.setAttributes(lp);

        mInsertImgPopupMenu.show();
    }

    /**
     * 图片处理活动返回
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                // 相册获得图片，编辑
                case SCAN_OPEN_PHONE:
                    StartEditImg(data.getData(), false);
                    break;

                // 拍照获得图片，编辑
                case REQUEST_TAKE_PHOTO:
                    StartEditImg(imgUri, true);
                    break;

                // 裁剪后设置图片
                case REQUEST_CROP: // 裁剪
                    InsertEditedImg(data.getData());
                    break;
            }
        }
    }

    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ModifyNoteActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }
    
    // endregion 菜单创建 活动返回

    // region 顶部工具栏事件 弹出菜单事件 onOptionsItemSelected onClick
    /**
     * 点击顶部菜单项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        CommonUtil.closeSoftKeyInput(this);

        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_finish:
                saveNoteData();
                break;

            case android.R.id.home:
            case R.id.id_menu_modifynote_cancel:
                CancelSaveNoteData();
                break;

            case R.id.id_menu_modifynote_img:
                ShowPopMenu();
                break;

            case R.id.id_menu_modifynote_info:
                showDetailInfo();
                break;

            case R.id.id_menu_modifynote_group:
                showGroupSetting();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 点击弹出菜单项
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // 从相册选择图片，ACTION_GET_CONTENT
            case R.id.id_popmenu_choose_img:
                checkPermissions();
                mInsertImgPopupMenu.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, SCAN_OPEN_PHONE);
                break;

            // 打开相机
            case R.id.id_popmenu_open_camera:
                checkPermissions();
                mInsertImgPopupMenu.dismiss();
                takePhone();
                break;

            // 取消
            case R.id.id_popmenu_cancel:
                mInsertImgPopupMenu.dismiss();
                break;

            case R.id.id_MNoteAct_PopupMenu_OCR:
                openOCRAct(LongClickImgPath);
                mLongClickImgPopupMenu.cancel();
                break;
            case R.id.id_MNoteAct_PopupMenu_OCRCancel:
                mLongClickImgPopupMenu.cancel();
                break;
        }
    }

    // endregion 顶部工具栏 弹出菜单 事件处理
    
    // region 修改 保存处理 图片上传 CheckIsModify CancelSaveNoteData saveNoteData handleSaveImgToServer

    /**
     * 判断是否修改
     * @return
     */
    private Boolean CheckIsModify() {
        if (!TitleEditText.getText().toString().equals(note.getTitle()) ||
                !GroupNameTextView.getText().toString().equals(note.getGroup().getName()) ||
                !getEditData().equals(note.getContent()))
            return true;
        return false;
    }

    /**
     * 取消保存文件退出
     */
    private void CancelSaveNoteData() {
        if (CheckIsModify()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.MNoteActivity_CancelSaveAlertTitle)
                    .setMessage(R.string.MNoteActivity_CancelSaveAlertMsg)
                    .setNegativeButton(R.string.MNoteActivity_CancelSaveAlertNegativeButtonForLeave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.MNoteActivity_CancelSaveAlertPositiveButtonForCancel, null)
                    .create();
            alertDialog.show();
        } else
            finish();
    }

    /**
     * 文件保存活动处理
     */
    private void saveNoteData() {
        checkPermissions();

        // 获得笔记内容
        String Content = getEditData();

        // 内容为空，提醒
        if (Content.isEmpty()) {
            CommonUtil.closeSoftKeyInput(this);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.MNoteActivity_SaveAlertTitle)
                    .setMessage(R.string.MNoteActivity_SaveAlertMsg)
                    .setPositiveButton(R.string.MNoteActivity_SaveAlertPositiveButtonForOK, null)
                    .create();
            alertDialog.show();
            return;
        }

        // 标题空
        if (TitleEditText.getText().toString().isEmpty()) {

            // 替换换行
            String Con = Content.replaceAll("[\n|\r].*", "");
            // 替换HTML标签
            Con = Con.replaceAll("<img src=.*", getResources().getString(R.string.MNoteActivity_SaveAlertImgReplaceMozi));

            // 舍去过长的内容
            if (Con.length() > CUT_LENGTH + 3)
                TitleEditText.setText(Con.substring(0, CUT_LENGTH) + "...");
            else
                TitleEditText.setText(Con);
        }

        //////////////////////////////////////////////////
        // 具体保存过程

        // 判断是否修改
        boolean isModify = CheckIsModify();

        final String motoNote = note.getContent();

        // 设置内容
        note.setTitle(TitleEditText.getText().toString());
        note.setContent(Content);

        new Thread(new Runnable() {
            @Override
            public void run() {

                // 处理分组
                Group re = groupDao.queryGroupById(selectedGropId);
                if (re != null)
                    note.setGroup(re, true);
                else
                    note.setGroup(groupDao.queryGroupById(0), true);

                // 处理界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.closeSoftKeyInput(ModifyNoteActivity.this);
                    }
                });

                // 处理图片
                if (AuthMgr.getInstance().isLogin()) {
                    note.setContent(handleSaveImgToServer(note.getContent(), motoNote));
                }

                // 处理保存
                if (flag == NOTE_NEW) {

                    // 从 Note Frag 打开的 新建
                    long noteId = noteDao.insertNote(note);
                    note.setId((int) noteId);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent_FromNoteFrag = new Intent();
                            intent_FromNoteFrag.putExtra("notedata", note);
                            intent_FromNoteFrag.putExtra("flag", NOTE_NEW); // NEW
                            setResult(RESULT_OK, intent_FromNoteFrag);
                            finish();
                        }
                    });
                }
                else {

                    // 从 VMNOTE 打开的 修改
                    if (isModify)
                        // 修改数据库
                        noteDao.updateNote(note);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent_FromVMNote = new Intent();

                            intent_FromVMNote.putExtra("notedata", note);
                            intent_FromVMNote.putExtra("flag", NOTE_UPDATE); // UPDATE
                            intent_FromVMNote.putExtra("isModify", isModify);
                            setResult(RESULT_OK, intent_FromVMNote);

                            finish();
                        }
                    });
                }

            }
        }).start();
    }

    /**
     * 将笔记的图片上传并修改
     * @param note 新笔记 用来上传图片
     * @param motoNote 旧笔记 用来删除笔记
     * @return
     */
    @WorkerThread
    private String handleSaveImgToServer(String note, String motoNote) {

        String ret = note;

        // 切割块
        List<String> motoTextList = StringUtil.cutStringByImgTag(motoNote); // 旧 删除用
        List<String> textList = StringUtil.cutStringByImgTag(note); // 新 上传用

        // 获取 服务器上原有的图片
        ArrayList<String> NewUrls = new ArrayList<>();
        for (String blocks : textList) {
            if (blocks.contains("<img") && blocks.contains("src=")) {
                String imagePath = StringUtil.getImgSrc(blocks);
                if (imagePath.startsWith(ImgUtil.GetImgUrlHead)) {
                    NewUrls.add(imagePath);
                }
            }
        }

        ArrayList<String> DelUrls = new ArrayList<>();
        for (String blocks : motoTextList) {
            if (blocks.contains("<img") && blocks.contains("src=")) {
                String imagePath = StringUtil.getImgSrc(blocks);
                if (imagePath.startsWith(ImgUtil.GetImgUrlHead)) {
                    if (NewUrls.indexOf(imagePath) == -1) // 不存在新内，删除
                        DelUrls.add(imagePath);
                }
            }
        }

        // 异步删除原有的图片
        if (DelUrls.size() > 0)
            ImgUtil.DeleteImgsAsync(DelUrls.toArray(new String[0]));


        // 遍历本地图片
        for (String blocks : textList) {
            // 图片块
            if (blocks.contains("<img") && blocks.contains("src=")) {
                // 图片路径
                String imagePath = StringUtil.getImgSrc(blocks);
                // 本地路径，网络路径忽略
                if (imagePath.startsWith(SDCardUtil.SDCardRoot)) { // /storage/emulated/0/
                    try {
                        UploadStatus uploadStatus = ImgUtil.uploadImg(imagePath);
                        if (uploadStatus != null) {
                            String newFileName = uploadStatus.getNewFileName();
                            Log.e("", "handleSaveImgToServer: " + imagePath + " -> " + newFileName);
                            ret = ret.replaceAll(imagePath, newFileName);
                        }
                    }
                    catch (ServerErrorException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return ret;
    }

    
    // endregion 保存处理
    
    // region 插入图片 拍照编辑 编辑插入 takePhone StartEditImg InsertEditedImg

    /**
     * 拍照
     */
    private void takePhone() {
        // Photo 类型
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date());
        String fileName = time + "_Photo";

        // /Biji/NoteImage/
        String path = SDCardUtil.getPictureDir(); // 保存路径
        File file = new File(path);

        // 要保存的图片文件
        File imgFile = new File(file + File.separator + fileName + ".jpg");

        // 将file转换成uri，返回 provider 路径
        imgUri = FilePathUtil.getUriForFile(this, imgFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 权限
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // 传入新图片名
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        try {
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.MNoteActivity_CameraNoFoundAlertTitle)
                    .setMessage(R.string.MNoteActivity_CameraNoFoundAlertMsg)
                    .setPositiveButton(R.string.MNoteActivity_CameraNoFoundAlertPosButton, null)
                    .create().show();
        }

        // TODO nox 模拟器没有相机可以测试
    }

    /**
     * 微信弹出图片涂鸦裁剪
     * @param uridata
     * @param isTakePhoto
     */
    private void StartEditImg(Uri uridata, boolean isTakePhoto) {

        // uridata:
        // content://com.android.providers.media.documents/document/image%3A172304
        // content://com.baibuti.biji.FileProvider/images/NoteImage/20190518133507370_Photo.jpg

        ShowLogE("StartEditImg", "uridata: " + uridata.getPath());

        // uridata.getPath():
        // /document/image:172305
        // /images/NoteImage/20190518133854935_Photo.jpg

        try {
            // 获得源路径
            String uri_path = FilePathUtil.getFilePathByUri(this, uridata);

            ShowLogE("StartEditImg", "uri_path: " + uri_path);

            // FilePathUtil.getFilePathByUri:
            // /storage/emulated/0/Tencent/TIMfile_recv/Q52T81B2LV(XA1Y7}1BS0{F.png
            // /storage/emulated/0/Biji/NoteImage/NoteImage

            if (uri_path.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("插入图片")
                        .setMessage("从相册获取的图片或拍照得到的图片不存在，请重试。")
                        .setNegativeButton("确定", null)
                        .create()
                        .show();
                return;
            }

            Uri uri = Uri.fromFile(new File(uri_path));

            // 删除图片判断
            this.isTakePhoto_Delete = isTakePhoto;

            // 打开编辑页面
            Intent intent = new Intent(ModifyNoteActivity.this, IMGEditActivity.class);

            intent.putExtra("IMAGE_URI", uri);
            intent.putExtra("IMAGE_SAVE_PATH", SDCardUtil.getPictureDir());

            startActivityForResult(intent, REQUEST_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入编辑好的图片
     * @param mCutUri 图片裁剪时返回的uri
     */
    private void InsertEditedImg(Uri mCutUri) {
        ShowLogE("onActivityResult", "Result:"+ mCutUri);

        // 判断是否需要删除原图片
        if (isTakePhoto_Delete)
            SDCardUtil.deleteFile(FilePathUtil.getFilePathByUri(this, imgUri));

        insertImagesSync(mCutUri); // URI
    }

    // endregion 插入图片 拍照编辑
    
    // region 笔记处理 列表信息处理 refreshGroupList

    /**
     * 刷新分组列表
     */
    private void refreshGroupList() {
        groupDao = new GroupDao(this);
        GroupList = groupDao.queryAllGroups();
        Collections.sort(GroupList);
        groupAdapter = new GroupAdapter(this, GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    // endregion 笔记处理 列表信息处理
    
    // region 其他功能 showDetailInfo showGroupSetting

    /**
     * 显示笔记详细信息
     */
    private void showDetailInfo() {
        if (flag == NOTE_NEW) {
            AlertDialog savedialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.MNoteActivity_InfoSaveAlertTitle)
                    .setMessage(R.string.MNoteActivity_InfoSaveAlertMsg)
                    .setNegativeButton(R.string.MNoteActivity_InfoSaveAlertNegativeButtonForCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.MNoteActivity_InfoSaveAlertNegativeButtonForSave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveNoteData();
                            dialog.dismiss();
                        }
                    }).create();
            savedialog.show();
        }
        else {
            final String Info = getResources().getString(R.string.VMNoteActivity_InfoTitle) + note.getTitle() + "\n" +
                    getResources().getString(R.string.VMNoteActivity_InfoCreateTime) + note.getCreateTime_FullString() + "\n" +
                    getResources().getString(R.string.VMNoteActivity_InfoUpdateTime) + note.getUpdateTime_FullString() + "\n\n" +
                    getResources().getString(R.string.VMNoteActivity_InfoGroupLabelTitle) + note.getGroup().getName();

            AlertDialog infodialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.VMNoteActivity_InfoAlertTitle)
                    .setMessage(Info)
                    .setNeutralButton(R.string.VMNoteActivity_InfoAlertNeutralButtonForCopy, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.VMNoteActivity_InfoAlertClipDataLabel), Info);
                            clipboardManager.setPrimaryClip(clip);
                            Toast.makeText(ModifyNoteActivity.this, R.string.VMNoteActivity_InfoAlertCopySuccess, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.VMNoteActivity_InfoAlertNegativeButtonForOK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            infodialog.show();
        }
    }

    /**
     * 显示分组设置
     */
    private void showGroupSetting() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                refreshGroupList();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog GroupSettingDialog = new AlertDialog
                                .Builder(ModifyNoteActivity.this)
                                .setTitle(R.string.MNoteActivity_GroupSetAlertTitle)
                                .setNegativeButton(R.string.MNoteActivity_GroupSetAlertNegativeButtonForCancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setSingleChoiceItems(groupAdapter, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selectedGropId = GroupList.get(which).getId();
                                        GroupNameTextView.setText(GroupList.get(which).getName());
                                        GroupNameTextView.setTextColor(GroupList.get(which).getIntColor());
                                        dialog.cancel();
                                    }
                                }).create();

                        GroupSettingDialog.show();

                    }
                });
            }
        }).start();


    }

    // endregion 其他功能
    
    // region 文字识别 openOCRAct

    /**
     * 对图片 打开OCR活动
     * @param imgPath
     */
    private void openOCRAct(String imgPath) {
        Intent intent = new Intent(ModifyNoteActivity.this, OCRActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(OCRActivity.INT_IMGPATH, imgPath);

        intent.putExtra(OCRActivity.INT_BUNDLE, bundle);
        startActivity(intent);
    }

    //
    // /**
    //  * 处理文字识别
    //  * 在 dealWithContent 处理图片点击事件
    //  * @param imagePath
    //  */
    // private void ShowdealWithContentForOCR(final String imagePath) {
    //
    //     AlertDialog.Builder idenDialog = new AlertDialog
    //             .Builder(ModifyNoteActivity.this)
    //             .setTitle(R.string.MNoteActivity_OCRCheckAlertTitle)
    //             .setMessage(getResources().getString(R.string.MNoteActivity_OCRCheckAlertMsg) + imagePath)
    //             .setCancelable(true)
    //             .setPositiveButton(R.string.MNoteActivity_OCRCheckAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
    //                 @Override
    //                 public void onClick(DialogInterface dialog, int which) {
    //
    //                     Bitmap bitmap = BitmapUtil.getBitmapFromFile(imagePath);
    //                     // 异步识别文字
    //                     idenWordsSync(bitmap);
    //                     dialog.dismiss();
    //                 }
    //             })
    //             .setNegativeButton(R.string.MNoteActivity_OCRCheckAlertNegativeButtonForCancel, null);
    //     idenDialog.show();
    // }
    //
    // /**
    //  * 异步识别文字
    //  * @param bitmap 图片路径
    //  */
    // private void idenWordsSync(final Bitmap bitmap) {
    //     idenLoadingDialog = new ProgressDialog(ModifyNoteActivity.this);
    //     idenLoadingDialog.setTitle(R.string.MNoteActivity_OCRSyncAlertTitle);
    //     idenLoadingDialog.setMessage(getResources().getString(R.string.MNoteActivity_OCRSyncAlertMsg));
    //
    //     class HasDismiss {
    //         private boolean dismiss = false;
    //         HasDismiss() {}
    //         void setDismiss() { this.dismiss = true; }
    //         boolean getDismiss() { return this.dismiss; }
    //     }
    //     final HasDismiss isHasDismiss = new HasDismiss();
    //
    //     final Observable<String> mObservable = Observable.create(new ObservableOnSubscribe<String>() {
    //         @Override
    //         public void subscribe(ObservableEmitter<String> emitter) throws Exception {
    //
    //             // 识别 ExtractUtil.recognition <<< 异步
    //             final String extaText = ExtractUtil.recognition(bitmap, ModifyNoteActivity.this);
    //
    //             emitter.onNext(extaText); // 处理识别后响应
    //             emitter.onComplete(); // 完成
    //         }
    //     })
    //             .subscribeOn(Schedulers.io()) //生产事件在io
    //             .observeOn(AndroidSchedulers.mainThread()); //消费事件在UI线程
    //
    //     idenLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
    //         @Override
    //         public void onCancel(DialogInterface dialog) { ////////// 待改
    //             // Toast.makeText(ModifyNoteActivity.this, "d", Toast.LENGTH_SHORT).show();
    //             // ExtractUtil.tessBaseAPI.clear();
    //             // mObservable.onTerminateDetach();
    //             isHasDismiss.setDismiss();
    //         }
    //     });
    //     idenLoadingDialog.show();
    //
    //     mObservable.subscribe(new Observer<String>() {
    //         @Override
    //         public void onComplete() {
    //
    //         }
    //
    //         @Override
    //         public void onError(Throwable e) {
    //             if (idenLoadingDialog != null && idenLoadingDialog.isShowing()) {
    //                 idenLoadingDialog.dismiss();
    //             }
    //             Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_OCRSyncAlertError, Toast.LENGTH_SHORT).show();
    //         }
    //
    //         @Override
    //         public void onSubscribe(Disposable d) {
    //             subsInsert = d;
    //         }
    //
    //         // 识别后的处理
    //         @Override
    //         public void onNext(final String extaText) {
    //             // 关闭进度条
    //             if (idenLoadingDialog != null && idenLoadingDialog.isShowing()) {
    //                 idenLoadingDialog.dismiss();
    //             }
    //             // 处理回显
    //             ShowLogE("idenWordsSync.onNext()", isHasDismiss.getDismiss() + "");
    //             if (!isHasDismiss.getDismiss()) // GOMI Code
    //                 idenWordsNextReturn(extaText);
    //         }
    //     });
    // }
    //
    // /**
    //  * 异步识别出图片后的回显
    //  * @param extaText 识别出的文字
    //  */
    // private void idenWordsNextReturn(final String extaText) {
    //     final EditText et = new EditText(ModifyNoteActivity.this);
    //     et.setText(extaText);
    //
    //     AlertDialog.Builder resultDialog = new AlertDialog
    //             .Builder(ModifyNoteActivity.this)
    //             .setTitle(R.string.MNoteActivity_OCRSyncResultAlertTitle)
    //             .setView(et)
    //             .setCancelable(true)
    //             .setPositiveButton(R.string.MNoteActivity_OCRSyncResultAlertPositiveButtonForCopy, new DialogInterface.OnClickListener() {
    //                 @Override
    //                 public void onClick(DialogInterface dialog, int which) {
    //                     ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    //                     ClipData clip = ClipData.newPlainText(getResources().getString(R.string.MNoteActivity_OCRSyncResultAlertCopyClipLabel), extaText);
    //                     clipboardManager.setPrimaryClip(clip);
    //                     Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_OCRSyncAlertCopy, Toast.LENGTH_SHORT).show();
    //                 }
    //             })
    //             .setNegativeButton(R.string.MNoteActivity_OCRSyncResultAlertNegativeButtonForCancel, null);
    //
    //     resultDialog.show();
    // }
    //
    // endregion  OCR部分
    
    // region 权限 软键盘 checkPermissions onRequestPermissionsResult closeSoftKeyInput

    /**
     * 动态判断存储拍照权限
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 检查是否有存储和拍照权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                hasPermission = true;
            else
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
    }

    /**
     * 授予权限，checkPermissions() 用
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            }
            else {
                Toast.makeText(this, R.string.MNoteActivity_PermissionGrantedError, Toast.LENGTH_SHORT).show();
                hasPermission = false;
            }
        }
        else if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                assets2SD(getApplicationContext(), ExtractUtil.LANGUAGE_PATH, ExtractUtil.DEFAULT_LANGUAGE_NAME);
            }
        }
    }

    // endregion 权限 软键盘 
    
    // region 文字图片显示处理 getEditData dealWithContent showDataSync showEditData insertImagesSync
    /**
     * 获取 ContentEditText 内容
     * @return 笔记内容
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = ContentEditText.buildEditData();
        StringBuilder content = new StringBuilder();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            }
            else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }

    /**
     * 处理内容，重要
     * ContentEditText.post(new Runnable() -> dealWithContent(););
     */
    private void dealWithContent() {

        ContentEditText.clearAllLayout();
        showDataSync(note.getContent());

        ContentEditText.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath))
                    if (SDCardUtil.deleteFile(imagePath))
                        Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_DWCRtImageDelete, Toast.LENGTH_SHORT).show();
            }
        });

        // 图片点击事件
        ContentEditText.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(final String imagePath) {
                if (!TextUtils.isEmpty(getEditData())) {

                    ArrayList<String> imageList = StringUtil.getTextFromHtml(getEditData(), true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        int currentPosition = imageList.indexOf(imagePath);
                        ShowClickImg(imageList, currentPosition);
                    }

//                    List<String> imageList = StringUtil.getTextFromHtml(getEditData(), true);
//                    if (!TextUtils.isEmpty(imagePath)) {
//                        // int currentPosition = imageList.indexOf(imagePath);
//                        // ShowdealWithContentForOCR(imagePath);
//
//                    }
                }
            }
        });
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
            String[] imgs = imageList.toArray(new String[0]);
            ImagePopupDialog dialog = new ImagePopupDialog(this, imgs, currentPosition);
            dialog.setOnLongClickImageListener(new ImagePopupDialog.onLongClickImageListener() {

                @Override
                public void onLongClick(View v, int index) {
                    LongClickImgPath = imgs[index];
                    mLongClickImgPopupMenu.show();
                }
            });
            dialog.show();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 异步显示数据
     * @param html
     */
    private void showDataSync(final String html) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
        //.onBackpressureBuffer()
        .subscribeOn(Schedulers.io())//生产事件在io
        .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onComplete() {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                if (ContentEditText != null) {
                    //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                    ContentEditText.addEditTextAtIndex(ContentEditText.getLastIndex(), "");
                }
            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_showDataSyncError, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSubscribe(Disposable d) {
                subsLoading = d;
            }

            @Override
            public void onNext(String text) {
                if (ContentEditText != null) {
                    if (text.contains("<img") && text.contains("src=")) {
                        //imagePath可能是本地路径，也可能是网络地址
                        String imagePath = StringUtil.getImgSrc(text);
                        //插入空的EditText，以便在图片前后插入文字
                        ContentEditText.addEditTextAtIndex(ContentEditText.getLastIndex(), "");
                        ContentEditText.addImageViewAtIndex(ContentEditText.getLastIndex(), imagePath);
                    } else {
                        ContentEditText.addEditTextAtIndex(ContentEditText.getLastIndex(), text);
                    }
                }
            }
        });
    }

    /**
     * 显示数据
     * @param emitter
     * @param html
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtil.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e) {
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    /**
     * 异步插入图片，重要
     * @param data 图片 Uri
     */
    private void insertImagesSync(final Uri data) {
        insertDialog.show();

        // TODO 整理

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try {
                    ContentEditText.measure(0, 0);

                    ShowLogE("insertImagesSync", "data: " + data); // _Edited

                    Bitmap bitmap = ImageUtil.getSmallBitmap(data + "", screenWidth, screenHeight); // 压缩图片
                    String smallImagePath = SDCardUtil.saveSmallImgToSdCard(bitmap);

                    ShowLogE("insertImagesSync", "imagePath: " + smallImagePath); // _Small

                    // 删除 Edited
                    SDCardUtil.deleteFile("" + data);

                    emitter.onNext(smallImagePath);

                    // TODO 网络图片插入

                    // <img src="https://www.baidu.com/img/bd_logo1.png"> <- `https://` 不可漏

                    // 测试插入网络图片
                    // emitter.onNext("https://raw.githubusercontent.com/Aoi-hosizora/Biji_Baibuti/a5bb15af4098296ace557e281843513b2f672e0f/assets/DB_Query.png");

                    emitter.onComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        })
                //.onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_insertImagesSyncSuccess, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_insertImagesSyncError, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsInsert = d;
                    }

                    @Override
                    public void onNext(String imagePath) {
                        ContentEditText.insertImage(imagePath, ContentEditText.getMeasuredWidth());
                    }
                });
    }

    // endregion 文字图片显示处理
}

