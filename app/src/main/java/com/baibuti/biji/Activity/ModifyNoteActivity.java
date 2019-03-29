package com.baibuti.biji.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;
import com.baibuti.biji.util.ImageUtils;
import com.baibuti.biji.util.SDCardUtil;
import com.baibuti.biji.util.StringUtils;
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


/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class ModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText TitleEditText;
    private TextView UpdateTimeTextView;
    private TextView GroupNameTextView;
    private com.sendtion.xrichtext.RichTextEditor ContentEditText;


    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private Disposable subsLoading;
    private Disposable subsInsert;

    private Menu menu;

    private Note note;
    private int notePos;
    private GroupDao groupDao;
    private NoteDao noteDao;

    private int flag; // 0: NEW, 1: UPDATE

    public final int CUT_LENGTH = 17;
    private int screenWidth;
    private int screenHeight;

    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
    private static final int REQUEST_CROP = 1;// 裁剪
    private static final int SCAN_OPEN_PHONE = 2;// 相册

    private Uri imgUri; // 拍照时返回的uri
    private Uri mCutUri;// 图片裁剪时返回的uri
    private Bitmap avatarBitMap = null;

    private static final int REQUEST_PERMISSION = 100;
    private boolean hasPermission = false;

    private Dialog mCameraDialog;

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifyplainnote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        note = (Note) getIntent().getSerializableExtra("notedata");
        flag = getIntent().getIntExtra("flag",0);

        if (flag == NOTE_NEW)
            setTitle("新建笔记");
        else
            setTitle("编辑笔记");

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);

        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);

        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        UpdateTimeTextView = (TextView) findViewById(R.id.id_modifynote_updatetime);
        GroupNameTextView = (TextView) findViewById(R.id.id_modifynote_group);
        ContentEditText = (com.sendtion.xrichtext.RichTextEditor) findViewById(R.id.id_modifynote_content);


        TitleEditText.setText(note.getTitle());
        UpdateTimeTextView.setText(note.getUpdateTime_ShortString());
        GroupNameTextView.setText(note.getGroupLabel().getName());

        //////////////////////////////////////////////////
        // ContentEditText

        ContentEditText.post(new Runnable() {
            @Override
            public void run() {
                dealWithContent();
            }
        });
    }

    // 确定是否修改了内容
    private Boolean CheckIsModify() {
        if (!TitleEditText.getText().toString().equals(note.getTitle()) || !getEditData().equals(note.getContent()))
            return true;
        return false;
    }

    // 获取 Menu 实例
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu,menu);
        this.menu=menu;
        return true;
    }

    // 点击顶部菜单项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        closeSoftKeyInput();

        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_finish:
                saveNoteData(true);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDetailInfo() {
        if (flag == NOTE_NEW) {
            AlertDialog savedialog = new AlertDialog.Builder(this)
                    .setTitle("详细信息")//设置对话框的标题
                    .setMessage("当前笔记还没保存，是否要保存？")//设置对话框的内容
                    //设置对话框的按钮
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveNoteData(false);
                            dialog.dismiss();
                        }
                    }).create();
            savedialog.show();
        }

        final String Info = "标题：" + note.getTitle() + "\n" +
                "创建时间：" + note.getCreateTime_FullString() + "\n" +
                "最近修改时间：" + note.getUpdateTime_FullString() + "\n\n" +
                "分类：" + note.getGroupLabel().getName();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("详细信息")
                .setMessage(Info)
                .setNeutralButton("复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", Info);
                        clipboardManager.setPrimaryClip(clip);
                        Toast.makeText(ModifyNoteActivity.this, "信息复制成功。", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

    }

    // 显示下部弹出图片选择菜单
    private void ShowPopMenu() {
        mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.bottom_dialog, null);
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

    // 点击弹出菜单项
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_popmenu_choose_img:
                // SCAN_OPEN_PHONE
                checkPermissions();
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent,SCAN_OPEN_PHONE);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, SCAN_OPEN_PHONE);
                break;
            case R.id.id_popmenu_open_camera:
                checkPermissions();
                // REQUEST_TAKE_PHOTO
                takePhone();
                break;
            case R.id.id_popmenu_cancel:
                mCameraDialog.dismiss();
                break;
        }
    }

    // 图片处理活动返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {


                // 打开图库获取图片并进行裁剪 FINISHED
                case SCAN_OPEN_PHONE:

//                        Log.e("000", "onActivityResult: " + data.getData());

//                         cropPhoto(data.getData(), false);
                    WeiXinEditImg(data.getData());


                break;


                // 拍照并进行裁剪
                case REQUEST_TAKE_PHOTO:
//                    Log.e("000", "onActivityResult: " + getFilePathByUri(getApplicationContext(),imgUri));
                    // cropPhoto(imgUri, true);
                    WeiXinEditImg(imgUri);
                break;



                //////////////////////////////////////////////////////////////////////

                // 裁剪后设置图片 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                case REQUEST_CROP: // 裁剪
                    Log.i("/////////////1212///", "onActivityResult: "+ data.getData());
                    mCutUri = data.getData();
                    insertImagesSync(mCutUri); // URI
                    // Log.e("S", "onActivityResult: imgUri:REQUEST_CROP:" + mCutUri.toString());
                break;
            }
        }
    }

    // 仿照微信弹出图片涂鸦裁剪
    private void WeiXinEditImg(Uri uridata) {
//        Log.e("159753123", "WeiXinEditImg: "+uridata.getAuthority() );


        try {
            avatarBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uridata);
            //此处获得了Bitmap图片，可以用作设置头像等等。
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(this, IMGEditActivity.class);

        try {

//            Log.e("uridata", "WeiXinEditImg: "+ uridata );
//            Log.e("uridata", "WeiXinEditImg: "+ uridata.getAuthority() );
            String uri_path = getFilePathByUri(this, uridata);
//            Log.e("uri_path", "WeiXinEditImg: "+ uri_path );

            Uri uri = Uri.fromFile(new File(uri_path));
            System.out.println(uri.toString());

            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri);
//            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, uri+" - edited");
            //intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,);
            startActivityForResult(intent,REQUEST_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 拍照
    private void takePhone() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = "photo_" + time;
        String path = SDCardUtil.getPictureDir();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        // 要保存的图片文件
        File imgFile = new File(file+File.separator+ fileName + ".jpg");

//        Log.e("00011", "takePhone: "+path+fileName + ".jpg" );
        // 将file转换成uri
        // 注意7.0及以上与之前获取的uri不一样了，返回的是provider路径
        imgUri = getUriForFile(this, imgFile);
//        Log.e("010", "takePhone: "+(imgUri)+"}"+(imgUri+""));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 添加Uri读取权限
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // 添加图片保存位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }


    // 由Uri进行图片裁剪
//    private void cropPhoto(Uri uri, boolean fromCapture) {
//        Log.i("S", "cropPhoto: "+uri);
//        Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
//        intent.setDataAndType(uri, "image/*");
//        intent.putExtra("scale", true);
//
//        // 设置裁剪区域的宽高比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//
//        // 设置裁剪区域的宽度和高度
//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
//
//        // 取消人脸识别
//        intent.putExtra("noFaceDetection", true);
//        // 图片输出格式
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//
//        // 若为false则表示不返回数据
//        intent.putExtra("return-data", false);
//
//
//        // 指定裁剪完成以后的图片所保存的位置,pic info显示有延时
//        if (fromCapture) {
//            // 如果是使用拍照，那么原先的uri和最终目标的uri一致
//            mCutUri = uri;
//        } else { // 从相册中选择，那么裁剪的图片保存在take_photo中
//            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
//            String fileName = "photo_" + time;
//            File mCutFile = new File(Environment.getExternalStorageDirectory() + "/take_photo", fileName + ".jpg");
//            if (!mCutFile.getParentFile().exists()) {
//                mCutFile.getParentFile().mkdirs();
//            }
//
//            ////////////////////////////////////////
//            mCutUri = getUriForFile(this, mCutFile);
//
//
//
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
//        Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();
//        // 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
//        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        intentBc.setData(uri);
//        this.sendBroadcast(intentBc);
//
//        startActivityForResult(intent, REQUEST_CROP); //设置裁剪参数显示图片至ImageVie
//    }

    // 退出取消保存文件
    private void CancelSaveNoteData() {
        if (CheckIsModify()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("确定要取消编辑吗？")
                    .setMessage("您的修改将不会保存。")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create();
            alertDialog.show();
        }
        else
            finish();
    }

    // 文件保存活动处理
    private void saveNoteData(boolean isFinish) {

        String Content = getEditData();


        if (Content.isEmpty()) {
            closeSoftKeyInput();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("没有输入内容，请补全笔记内容")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create();
            alertDialog.show();
            return;
        }


        if (TitleEditText.getText().toString().isEmpty()) {
//            if ("".equals(Content))
//                Content = "图片";
            String Con = Content.replaceAll("<img src=.*", " 图片 ");

            if (Content.length() > CUT_LENGTH + 3)
                TitleEditText.setText(Con.substring(0, CUT_LENGTH) + "...");
            else
                TitleEditText.setText(Con);

        }
        //////////////////////////////////////////////////
        boolean isModify = CheckIsModify();
        note.setTitle(TitleEditText.getText().toString());
        note.setContent(Content);

        int groupId = note.getGroupLabel().getId();
        if (flag == 0) { // NEW
            long noteId = noteDao.insertNote(note);
            note.setId((int)noteId);
            flag = 1;

        }
        else  // MODIFY
            if (isModify)
                noteDao.updateNote(note);

        closeSoftKeyInput();
        Intent intent = new Intent();
        intent.putExtra("isModify", isModify);
        intent.putExtra("modify_note",note);

        setResult(RESULT_OK,intent);

        if (isFinish)
            finish();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    // File2Uri <<<<<<<<<<<<<<<<<<<< FileProvider.getUriForFile
    private static Uri getUriForFile(Context context, File file) {


        // 7.0及以上使用的uri是contentProvider content://com.rain.takephotodemo.FileProvider/images/photo_20180824173621.jpg
        // 6.0使用的uri为file:///storage/emulated/0/take_photo/photo_20180824171132.jpg

        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.baibuti.biji.FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    // Uri2Path <<<<<<<<<<<<<<<<<<<< uri.getPath()
    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }


        Log.e("uri", "getFilePathByUri: "+ uri+":"+isAppDocument(uri) );

        if (isAppDocument(uri)) { // com.baibuti.biji.FileProvider

            // content://com.android.providers.media.documents/document/image%3A235700
            // content://com.baibuti.biji.FileProvider/images/photo_20190323225817.jpg

            // MediaProvider

            final String[] fin = (uri+"").split(File.separator);
            final String filename = Environment.getExternalStorageDirectory() +File.separator+ "take_photo"+File.separator+fin[4];

            return filename;
        }






            // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }

        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    // Path2Uri <<<<<<<<<<<<<<<<<<<< uri.getPath()
    public static Uri getImageStreamFromExternal(String imageName) {
        File externalPubPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        File picPath = new File(externalPubPath, imageName);
        Uri uri = null;
        if(picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }

        return uri;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isAppDocument(Uri uri) {
        return "com.baibuti.biji.FileProvider".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    // 动态判断权限
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查是否有存储和拍照权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            ) {
                hasPermission = true;
//                takePhone();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            }
        }
    }

    // 授予权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                takePhone();
                hasPermission = true;
            } else {
                Toast.makeText(this, "权限授予失败！", Toast.LENGTH_SHORT).show();
                hasPermission = false;
            }
        }
    }

    // 关闭软键盘
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && imm.isActive() && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // 获取 ContentEditText 内容
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

    //ContentEditText.post(new Runnable() -> dealWithContent(););
    private void dealWithContent() {
        ContentEditText.clearAllLayout();
        showDataSync(note.getContent());

        ContentEditText.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        Toast.makeText(ModifyNoteActivity.this, "删除成功：" + imagePath, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // 图片点击事件
        ContentEditText.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                if (!TextUtils.isEmpty(getEditData())){
                    List<String> imageList = StringUtils.getTextFromHtml(getEditData(), true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        int currentPosition = imageList.indexOf(imagePath);
                        Toast.makeText(ModifyNoteActivity.this, "点击图片：" + currentPosition + "：" + imagePath , Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    // 异步显示数据
    private void showDataSync(final String html){
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
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        if (ContentEditText != null) {
                            //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                            ContentEditText.addEditTextAtIndex(ContentEditText.getLastIndex(), "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        Toast.makeText(ModifyNoteActivity.this, "解析错误：图片不存在或已损坏", Toast.LENGTH_SHORT).show();
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

    // 显示数据
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        }catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    // 异步由Uri插入图片
    private void insertImagesSync(final Uri data){
        insertDialog.show();




        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try{
                    ContentEditText.measure(0, 0);
                    Log.e("0", "###data=" + data);
                    String imagePath = SDCardUtil.getFilePathFromUri(getApplicationContext(),  data);
                    Log.e("0", "###path=" + imagePath);
                    Bitmap bitmap = ImageUtils.getSmallBitmap(data+"", screenWidth, screenHeight);//压缩图片
                    //bitmap = BitmapFactory.decodeFile(imagePath);
                    imagePath = SDCardUtil.saveToSdCard(bitmap);
                    Log.e("1", "###imagePath="+imagePath);
                    emitter.onNext(imagePath);


                    // 测试插入网络图片 http://p695w3yko.bkt.clouddn.com/18-5-5/44849367.jpg
                    //subscriber.onNext("http://p695w3yko.bkt.clouddn.com/18-5-5/30271511.jpg");

                    emitter.onComplete();
                }catch (Exception e){
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
                        Toast.makeText(ModifyNoteActivity.this, "图片插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        Toast.makeText(ModifyNoteActivity.this, "图片插入失败", Toast.LENGTH_SHORT).show();
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
