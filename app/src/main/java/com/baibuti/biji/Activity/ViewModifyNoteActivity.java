package com.baibuti.biji.Activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.View.ImageLoader;
import com.baibuti.biji.util.CommonUtil;
import com.baibuti.biji.util.StringUtils;
import com.baibuti.biji.util.ToDocUtil;
import com.hitomi.tilibrary.transfer.Transferee;
import com.previewlibrary.GPreviewBuilder;
import com.previewlibrary.ZoomMediaLoader;
import com.previewlibrary.enitity.ThumbViewInfo;
import com.sendtion.xrichtext.RichTextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ViewModifyNoteActivity extends AppCompatActivity implements View.OnClickListener, IShowLog {

    private TextView TitleEditText_View;
    private TextView UpdateTimeTextView_View;
    private TextView GroupNameTextView_View;
    private com.sendtion.xrichtext.RichTextView ContentEditText_View;

    private ProgressDialog loadingDialog;
    private ProgressDialog savingDialog;
    private Disposable mDisposable;

    private Note note;

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    private static final int REQ_CHOOSE_FOLDER_PATH = 2;

    private boolean isModify = false;
    private int flag = NOTE_NEW;

    private Transferee transferee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmodifynote);

        transferee = Transferee.getDefault(this);
        ZoomMediaLoader.getInstance().init(new ImageLoader());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.VMNoteActivity_Title);

        savingDialog = new ProgressDialog(this);
        savingDialog.setMessage(getResources().getString(R.string.VMNoteActivity_SavingData));
        savingDialog.setCanceledOnTouchOutside(false);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage(getResources().getString(R.string.VMNoteActivity_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();


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
//        ContentEditText_View.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()){
//                    case MotionEvent.ACTION_UP:
////                        Log.d("YYPT", "click the scrollView");
//                        //点击整个页面都会让内容框获得焦点，且弹出软键盘
//                        v.setFocusable(true);
//                        v.setFocusableInTouchMode(true);
//                        v.requestFocus();
//                        ShowModifyNoteActivity();
//                        break;
//                }
//                return false;
//            }
//        });

    }

    /**
     * IShowLog 接口，全局设置 Log 格式
     * @param FunctionName
     * @param Msg
     */
    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ViewModifyNoteActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewmodifynoteactivity_menu,menu);
        return true;
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
                CreateFileAsNote();
            break;
        }
        return true;
    }

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

    /**
     * 返回键返回主界面
     */
    @Override
    public void onBackPressed() {
        BackToActivity();
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
//                if (resultCode == RESULTCODE) {
//                    ArrayList<String> resPath = data.getStringArrayListExtra(SELECTPATH);
//                    Log.d("ZWW", resPath.toString());
//                 Toast.makeText(this, resPath.toString(), Toast.LENGTH_SHORT).show();
//                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 分享笔记，待修改
     */
    private void ShareNoteContent() {
        CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null); //分享图文
    }

    /**
     * 另存为文件，选择类型
     */
    private void CreateFileAsNote() {

        // 选择保存为什么类型
        AlertDialog TypealertDialog = new AlertDialog
                .Builder(this)
                .setTitle("保存为文件")
                .setMessage("请选择保存的类型，笔记将默认保存在 " + ToDocUtil.getDefaultPath())
                .setPositiveButton("PDF", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavingFileAsNote(false, note);
                    }
                })
                .setNegativeButton("DOCX", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavingFileAsNote(true, note);
                    }
                })
                .setNeutralButton("取消", null).create();
        TypealertDialog.show();

    }

    /**
     * 根据类型保存为 Pdf 还是 Docx
     * @param isSaveAsDocx
     * @param mnote
     */
    public void SavingFileAsNote(final boolean isSaveAsDocx, final Note mnote) {

        // 文件路径待改
        final String Path_Docx = ToDocUtil.getDefaultDocxPath(note.getTitle());
        final String Path_Pdf = ToDocUtil.getDefaultPdfPath(note.getTitle());

        File file;
        if (isSaveAsDocx)
            file = new File(Path_Docx);
        else
            file = new File(Path_Pdf);

        final String FilePath = file.getPath();

        // 判断文件是否存在
        if (file.exists()) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(this)
                    .setTitle("保存")
                    .setMessage("文件 " + FilePath + " 已存在，是否覆盖？")
                    .setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isSaveAsDocx)
                                SavingDocxAsNote(FilePath, note);
                            else
                                SavingPdfAsNote(FilePath, note);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    }) .create();
            alertDialog.show();
        }
        else
            if (isSaveAsDocx)
                SavingDocxAsNote(FilePath, note);
            else
                SavingPdfAsNote(FilePath, note);
    }

    /**
     * 具体保存笔记为 Docx，CreateFileAsNote()用
     * @param Path
     * @param mnote
     */
    private void SavingDocxAsNote(final String Path, final Note mnote) {
        ShowLogE("SavingDocxAsNote", "Path: " + Path);

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
                            ToDocUtil.CreateDocxByNote(Path, mnote.getTitle(), mnote.getContent(), true);
                            if (savingDialog != null)
                                savingDialog.dismiss();

                            Toast.makeText(ViewModifyNoteActivity.this, "文件 " + Path + " 保存成功。", Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 保存笔记为 Pdf，CreateFileAsNote()用
     * @param Path
     * @param mnote
     */
    private void SavingPdfAsNote(final String Path, final Note mnote) {
        ShowLogE("SavingDocxAsNote", "Path: " + Path);

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
                            ToDocUtil.CreatePdfByNote(Path, mnote.getTitle(), mnote.getContent(), true);

                            if (savingDialog != null)
                                savingDialog.dismiss();

                            Toast.makeText(ViewModifyNoteActivity.this, "文件 " + Path + " 保存成功。", Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

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
     * 点击图片后弹出预览窗口，待改
     * @param imageList
     * @param currentPosition
     */
    private void ShowClickImg(ArrayList<String> imageList, int currentPosition) {
        ArrayList<ThumbViewInfo> mThumbViewInfoList = new ArrayList<>(); // 这个最好定义成成员变量
        ThumbViewInfo item;
        mThumbViewInfoList.clear();

        for (int i = 0;i < imageList.size(); i++) {
            Rect bounds = new Rect();
            //new ThumbViewInfo(图片地址);
            item=new ThumbViewInfo(imageList.get(i));
            item.setBounds(bounds);
            mThumbViewInfoList.add(item);
        }


        GPreviewBuilder.from(ViewModifyNoteActivity.this)
                .to(CustomActivity.class) // 是否使用自定义预览界面，当然8.0之后因为配置问题，必须要使用
                .setData(mThumbViewInfoList)
                .setCurrentIndex(currentPosition)
                .setSingleFling(true)
                .setType(GPreviewBuilder.IndicatorType.Number)
                // 小圆点
                //  .setType(GPreviewBuilder.IndicatorType.Dot)
                .start();//启动
    }

    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////

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
}
