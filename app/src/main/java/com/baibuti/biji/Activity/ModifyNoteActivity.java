package com.baibuti.biji.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

    public final int CUTLENGTH = 17;
    private int screenWidth;
    private int screenHeight;

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

        if (flag == 0)
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

    private Boolean CheckIsModify() {
        if (!TitleEditText.getText().toString().equals(note.getTitle()) || !getEditData().equals(note.getContent()))
            return true;
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu,menu);
        this.menu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.id_menu_modifynote_finish:

                saveNoteData();
                break;

            case android.R.id.home:
            case R.id.id_menu_modifynote_cancel:
                closeSoftKeyInput();
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

                break;

            case R.id.id_menu_modifynote_img:
                Dialog mCameraDialog = new Dialog(this, R.style.BottomDialog);
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
        return super.onOptionsItemSelected(item);
    }

    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
    private static final int REQUEST_CROP = 1;// 裁剪
    private static final int SCAN_OPEN_PHONE = 2;// 相册
    private static final int REQUEST_PERMISSION = 100;
    private Uri imgUri; // 拍照时返回的uri
    private Uri mCutUri;// 图片裁剪时返回的uri
    private boolean hasPermission = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_popmenu_choose_img:
                checkPermissions();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CROP/*SCAN_OPEN_PHONE*/);
            break;
            case R.id.id_popmenu_open_camera:
                checkPermissions();

            break;
            case R.id.id_popmenu_cancel:

            break;
        }
    }

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
    // 图片裁剪
    private void cropPhoto(Uri uri, boolean fromCapture) {
        Log.i("S", "cropPhoto: "+uri);
        Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("scale", true);

        // 设置裁剪区域的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置裁剪区域的宽度和高度
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        // 取消人脸识别
        intent.putExtra("noFaceDetection", true);
        // 图片输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        // 若为false则表示不返回数据
        intent.putExtra("return-data", false);


        // 指定裁剪完成以后的图片所保存的位置,pic info显示有延时
        if (fromCapture) {
            // 如果是使用拍照，那么原先的uri和最终目标的uri一致
            mCutUri = uri;
        } else { // 从相册中选择，那么裁剪的图片保存在take_photo中
            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
            String fileName = "photo_" + time;
            File mCutFile = new File(Environment.getExternalStorageDirectory() + "/take_photo", fileName + ".jpeg");
            if (!mCutFile.getParentFile().exists()) {
                mCutFile.getParentFile().mkdirs();
            }

            ////////////////////////////////////////
            mCutUri = getUriForFile(this, mCutFile);



        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
        Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();
        // 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(uri);
        this.sendBroadcast(intentBc);

        startActivityForResult(intent, REQUEST_CROP); //设置裁剪参数显示图片至ImageVie
    }

    // 从file中获取uri
    // 7.0及以上使用的uri是contentProvider content://com.rain.takephotodemo.FileProvider/images/photo_20180824173621.jpg
    // 6.0使用的uri为file:///storage/emulated/0/take_photo/photo_20180824171132.jpg
    private static Uri getUriForFile(Context context, File file) {
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 拍照并进行裁剪
                case REQUEST_TAKE_PHOTO:
                    // Log.e("S", "onActivityResult: imgUri:REQUEST_TAKE_PHOTO:" + imgUri.toString());
                    cropPhoto(imgUri, true);
                    break;

                // 裁剪后设置图片
                case REQUEST_CROP:
                    insertImagesSync(data.getData());
                    // Log.e("S", "onActivityResult: imgUri:REQUEST_CROP:" + mCutUri.toString());
                    break;
                // 打开图库获取图片并进行裁剪
                case SCAN_OPEN_PHONE:
                    // Log.e("S", "onActivityResult: SCAN_OPEN_PHONE:" + data.getData().toString());
                    cropPhoto(data.getData(), false);
                    break;
            }
        }
    }
    /**
     * 异步方式插入图片
     * @param data
     */
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
                    Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
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


    /**
     * 关闭软键盘
     */
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && imm.isActive() && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

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

    private void saveNoteData() {

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
            if (Content.length() > CUTLENGTH + 3)
                TitleEditText.setText(Content.substring(0, CUTLENGTH) + "...");
            else
                TitleEditText.setText(Content);
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
        finish();


    }

}
