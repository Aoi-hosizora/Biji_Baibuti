package com.baibuti.biji.Activity;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Dialog.GroupDialog;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;
import com.baibuti.biji.util.FilePathUtil;
import com.baibuti.biji.util.ImageUtils;
import com.baibuti.biji.util.SDCardUtil;
import com.baibuti.biji.util.StringUtils;
import com.baibuti.biji.util.ExtractUtil;
import com.baibuti.biji.util.BitmapUtils;
import com.sendtion.xrichtext.RichTextEditor;


import java.io.File;
import java.text.SimpleDateFormat;
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

import static com.baibuti.biji.util.ExtractUtil.assets2SD;


/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener, IShowLog {

    private EditText TitleEditText;
    private TextView UpdateTimeTextView;
    private TextView GroupNameTextView;
    private com.sendtion.xrichtext.RichTextEditor ContentEditText;


    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private Disposable subsLoading;
    private Disposable subsInsert;

    private AlertDialog.Builder idenDialog;
    private ProgressDialog idenLoadingDialog;

    private Menu menu;

    private Note note;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private List<Group> GroupList;
    private GroupAdapter groupAdapter;

    private int flag; // 0: NEW, 1: UPDATE
    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    public final int CUT_LENGTH = 17;
    private int screenWidth;
    private int screenHeight;

    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
    private static final int REQUEST_CROP = 1;// 裁剪
    private static final int SCAN_OPEN_PHONE = 2;// 相册

    private Uri imgUri; // 拍照时返回的uri
    private Uri mCutUri;// 图片裁剪时返回的uri
    private boolean isTakePhoto_Delete = false;
    private Bitmap avatarBitMap = null;

    private static final int REQUEST_PERMISSION = 100;
    private boolean hasPermission = false;

    private Dialog mCameraDialog;

    /**
     * 权限请求值
     */
    private static final int PERMISSION_REQUEST_CODE = 0;

    private static final int PICK_REQUEST_CODE = 10;

    private int selectedGropId = 0;

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
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(this, GroupList);
        groupAdapter.notifyDataSetChanged();

        note = (Note) getIntent().getSerializableExtra("notedata");
        flag = getIntent().getIntExtra("flag", NOTE_NEW);

        if (flag == NOTE_NEW) {
            setTitle(R.string.NMoteActivity_TitleForNewNote);
            note.setGroupLabel(groupDao.queryDefaultGroup());
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
        GroupNameTextView.setText(note.getGroupLabel().getName());
        GroupNameTextView.setTextColor(note.getGroupLabel().getIntColor());
        selectedGropId = note.getGroupLabel().getId();

        //////////////////////////////////////////////////
        // ContentEditText

        ContentEditText.post(new Runnable() {
            @Override
            public void run() {
                dealWithContent();
            }
        });
        checkPermissions();
    }

    /**
     * IShowLog 接口，全局设置 Log 格式
     * @param FunctionName
     * @param Msg
     */
    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ModifyNoteActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 判断是否修改
     * @return
     */
    private Boolean CheckIsModify() {
        if (!TitleEditText.getText().toString().equals(note.getTitle()) ||
            !GroupNameTextView.getText().toString().equals(note.getGroupLabel().getName()) ||
            !getEditData().equals(note.getContent()))
            return true;
        return false;
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
     * 点击顶部菜单项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        closeSoftKeyInput();

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
                                getResources().getString(R.string.VMNoteActivity_InfoGroupLabelTitle) + note.getGroupLabel().getName();

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
     * 刷新分组列表
     */
    private void refreshGroupList() {
        groupDao = new GroupDao(this);
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(this, GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    /**
     * 显示分组设置
     */
    private void showGroupSetting() {
        refreshGroupList();

        AlertDialog GroupSettingDialog = new AlertDialog
                .Builder(this)
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

    /**
     * 显示下部弹出图片选择菜单
     */
    private void ShowPopMenu() {
        mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.dialog_mnote_bottompopupmenu, null);

        //初始化视图
        root.findViewById(R.id.id_popmenu_choose_img).setOnClickListener(this);
        root.findViewById(R.id.id_popmenu_open_camera).setOnClickListener(this);
        root.findViewById(R.id.id_popmenu_cancel).setOnClickListener(this);

        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度

        dialogWindow.setAttributes(lp);

        mCameraDialog.show();
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
                mCameraDialog.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, SCAN_OPEN_PHONE);
                break;

            // 打开相机
            case R.id.id_popmenu_open_camera:
                checkPermissions();
                mCameraDialog.dismiss();
                takePhone();
                break;

            // 取消
            case R.id.id_popmenu_cancel:
                mCameraDialog.dismiss();
                break;
        }
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
                    WeiXinEditImg(data.getData(), false);
                    break;

                // 拍照获得图片，编辑
                case REQUEST_TAKE_PHOTO:
                    WeiXinEditImg(imgUri, true);
                    break;

                //////////////////////////////////////////////////////////////////////

                // 裁剪后设置图片
                case REQUEST_CROP: // 裁剪
                    ShowLogE("onActivityResult", "Result:"+ data.getData());

                    // 判断是否需要删除原图片
                    if (isTakePhoto_Delete) {
                        if (SDCardUtil.deleteFile(FilePathUtil.getFilePathByUri(this, imgUri)))
                            ShowLogE("onActivityResult", "Delete finish: "+ FilePathUtil.getFilePathByUri(this, imgUri));
                    }

                    mCutUri = data.getData();
                    insertImagesSync(mCutUri); // URI
                    break;
            }
        }
    }

    /**
     * 微信弹出图片涂鸦裁剪
     * @param uridata
     * @param isTakePhoto
     */
    private void WeiXinEditImg(Uri uridata, boolean isTakePhoto) {

        try {
            // 获得Bitmap图片
            avatarBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uridata);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, IMGEditActivity.class);

        try {
            // 获得源路径
            String uri_path = FilePathUtil.getFilePathByUri(this, uridata);

            Uri uri = Uri.fromFile(new File(uri_path));
            ShowLogE("WeiXinEditImg", "Path: " + uri.toString());

            // 删除图片判断
            this.isTakePhoto_Delete = isTakePhoto;

            // 打开编辑页面
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri);
            startActivityForResult(intent, REQUEST_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePhone() {
        // Photo 类型
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date());
        String fileName = time + "_Photo";

        String path = SDCardUtil.getPictureDir(); // 保存路径
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        // 要保存的图片文件
        File imgFile = new File(file + File.separator + fileName + ".jpg");

        // 将file转换成uri，返回 provider 路径
        imgUri = FilePathUtil.getUriForFile(this, imgFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 权限
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // 传入新图片名
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
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

        // 获得笔记内容
        String Content = getEditData();

        // 内容为空，提醒
        if (Content.isEmpty()) {
            closeSoftKeyInput();
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
            String Con = Content.replaceAll("[\n|\r|\n\r|\r\n].*", "");
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

        // 设置内容
        note.setTitle(TitleEditText.getText().toString());
        note.setContent(Content);

        // 处理分组信息
        Group re = groupDao.queryGroupById(selectedGropId);
        if (re != null)
            note.setGroupLabel(re);
        else
            note.setGroupLabel(groupDao.queryGroupById(0));

        //////////////////////////////////////////////////


        if (flag == NOTE_NEW) { // 从 Note Frag 打开的

            // 插入到数据库一条新信息
            long noteId = noteDao.insertNote(note);
            note.setId((int) noteId);
            closeSoftKeyInput();

            Intent intent_fromnotefrag = new Intent();
            intent_fromnotefrag.putExtra("notedata", note);
            intent_fromnotefrag.putExtra("flag", NOTE_NEW); // NEW
            setResult(RESULT_OK, intent_fromnotefrag);
            finish();
        }
        else { // 从 VMNOTE 打开的

            if (isModify)
                // 修改数据库
                noteDao.updateNote(note);
            closeSoftKeyInput();

            Intent intent_fromvmnote = new Intent();

            intent_fromvmnote.putExtra("notedata", note);
            intent_fromvmnote.putExtra("flag", NOTE_UPDATE); // UPDATE
            intent_fromvmnote.putExtra("isModify", isModify);
            setResult(RESULT_OK, intent_fromvmnote);

            finish();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // OCR 部分

    /**
     * 处理文字识别
     * 在 dealWithContent 处理图片点击事件
     * @param imagePath
     */
    private void ShowdealWithContentForOCR(final String imagePath) {

        idenDialog = new AlertDialog
                .Builder(ModifyNoteActivity.this)
                .setTitle(R.string.MNoteActivity_OCRCheckAlertTitle)
                .setMessage(getResources().getString(R.string.MNoteActivity_OCRCheckAlertMsg) + imagePath)
                .setCancelable(false)
                .setPositiveButton(R.string.MNoteActivity_OCRCheckAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Bitmap bitmap = BitmapUtils.getBitmapFromFile(imagePath);
                        // 异步识别文字
                        idenWordsSync(bitmap);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.MNoteActivity_OCRCheckAlertNegativeButtonForCancel, null);
        idenDialog.show();
    }

    /**
     * 异步识别文字
     * @param bitmap 图片路径
     */
    private void idenWordsSync(final Bitmap bitmap) {
        idenLoadingDialog = new ProgressDialog(ModifyNoteActivity.this);
        idenLoadingDialog.setTitle(R.string.MNoteActivity_OCRSyncAlertTitle);
        idenLoadingDialog.setMessage(getResources().getString(R.string.MNoteActivity_OCRSyncAlertMsg));


        final Observable<String> mObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {

                // 识别 ExtractUtil.recognition
                final String extaText = ExtractUtil.recognition(bitmap, ModifyNoteActivity.this);

                emitter.onNext(extaText); // 处理识别后响应
                emitter.onComplete(); // 完成
            }
        })
        .subscribeOn(Schedulers.io()) //生产事件在io
        .observeOn(AndroidSchedulers.mainThread()); //消费事件在UI线程


        idenLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mObservable.blockingSubscribe();
            }
        });
        idenLoadingDialog.show();

        mObservable.subscribe(new Observer<String>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {
                if (idenLoadingDialog != null && idenLoadingDialog.isShowing()) {
                    idenLoadingDialog.dismiss();
                }
                Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_OCRSyncAlertError, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSubscribe(Disposable d) {
                subsInsert = d;
            }

            // 识别后的处理
            @Override
            public void onNext(final String extaText) {
                // 关闭进度条
                if (idenLoadingDialog != null && idenLoadingDialog.isShowing()) {
                    idenLoadingDialog.dismiss();
                }
                // 处理回显
                idenWordsNextReturn(extaText);
            }
        });
    }

    /**
     * 异步识别出图片后的回显
     * @param extaText 识别出的文字
     */
    private void idenWordsNextReturn(final String extaText) {
        final EditText et = new EditText(ModifyNoteActivity.this);
        et.setText(extaText);

        AlertDialog.Builder resultDialog = new AlertDialog
                .Builder(ModifyNoteActivity.this)
                .setTitle(R.string.MNoteActivity_OCRSyncResultAlertTitle)
                .setView(et)
                .setCancelable(true)
                .setPositiveButton(R.string.MNoteActivity_OCRSyncResultAlertPositiveButtonForCopy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(getResources().getString(R.string.MNoteActivity_OCRSyncResultAlertCopyClipLabel), extaText);
                        clipboardManager.setPrimaryClip(clip);
                        Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_OCRSyncAlertCopy, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.MNoteActivity_OCRSyncResultAlertNegativeButtonForCancel, null);

        resultDialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是业务内部

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

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && imm.isActive() && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

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
            } else if (itemData.imagePath != null) {
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
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        Toast.makeText(ModifyNoteActivity.this, R.string.MNoteActivity_DWCRtImageDelete + imagePath, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // 图片点击事件
        ContentEditText.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(final String imagePath) {
                if (!TextUtils.isEmpty(getEditData())) {
                    List<String> imageList = StringUtils.getTextFromHtml(getEditData(), true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        // int currentPosition = imageList.indexOf(imagePath);
                        ShowdealWithContentForOCR(imagePath);
                    }
                }
            }
        });
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
                        String imagePath = StringUtils.getImgSrc(text);
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
            List<String> textList = StringUtils.cutStringByImgTag(html);
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


        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try {
                    ContentEditText.measure(0, 0);

                    ShowLogE("insertImagesSync", "data: " + data);
                    String imagePath = SDCardUtil.getFilePathFromUri(getApplicationContext(), data);

                    ShowLogE("insertImagesSync", "path: " + imagePath);
                    Bitmap bitmap = ImageUtils.getSmallBitmap(data + "", screenWidth, screenHeight);//压缩图片

                    //bitmap = BitmapFactory.decodeFile(imagePath);
                    imagePath = SDCardUtil.saveToSdCard(bitmap);
                    ShowLogE("insertImagesSync", "imagePath: " + imagePath);

                    emitter.onNext(imagePath);


                    // 测试插入网络图片 http://p695w3yko.bkt.clouddn.com/18-5-5/44849367.jpg
                    //subscriber.onNext("http://p695w3yko.bkt.clouddn.com/18-5-5/30271511.jpg");

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
}

