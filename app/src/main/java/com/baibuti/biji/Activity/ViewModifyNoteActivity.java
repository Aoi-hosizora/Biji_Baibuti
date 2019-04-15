package com.baibuti.biji.Activity;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.StringUtils;
import com.sendtion.xrichtext.RichTextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.baibuti.biji.util.CommonUtil.ColorHex_IntEncoding;

public class ViewModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView TitleEditText_View;
    private TextView UpdateTimeTextView_View;
    private TextView GroupNameTextView_View;
    private com.sendtion.xrichtext.RichTextView ContentEditText_View;

    private ProgressDialog loadingDialog;
    private Disposable mDisposable;

    private Note note;
//    private int notePos;

    private boolean isModify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmodifyplainnote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.VMNoteActivity_Title);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage(getResources().getString(R.string.VMNoteActivity_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        note = (Note) getIntent().getSerializableExtra("notedata");

        TitleEditText_View = (TextView) findViewById(R.id.id_modifynote_viewtitle);
        UpdateTimeTextView_View = (TextView) findViewById(R.id.id_modifynote_viewupdatetime);
        GroupNameTextView_View = (TextView) findViewById(R.id.id_modifynote_viewgroup);
        ContentEditText_View = (com.sendtion.xrichtext.RichTextView) findViewById(R.id.id_modifynote_viewcontent);

        TitleEditText_View.setText(note.getTitle());
        UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
        GroupNameTextView_View.setText(note.getGroupLabel().getName());
        GroupNameTextView_View.setTextColor(ColorHex_IntEncoding(note.getGroupLabel().getColor()));

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.id_modifynote_viewcontent:
//                ShowModifyNoteActivity();
//                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewmodifynoteactivity_menu,menu);
        return true;
    }

    private void ShowModifyNoteActivity() {
        Intent intent=new Intent(ViewModifyNoteActivity.this, ModifyNoteActivity.class);
        intent.putExtra("notedata",note);
        intent.putExtra("flag",1); // UPDATE
        startActivityForResult(intent,1); // 1 from CardView
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_viewmodify:
                // isModify

                ShowModifyNoteActivity();

                break;

            case android.R.id.home:
            case R.id.id_menu_modifynote_viewcancel:
                BackToActivity();
                break;

            case R.id.id_menu_modifynote_viewinfo:
                showDetailInfo();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        BackToActivity();
    }

    private void BackToActivity() {
        Intent motointent = new Intent();

        if (isModify) {
            motointent.putExtra("modify_note",note);
//                    motointent.putExtra("modify_note_pos", notePos);
            setResult(RESULT_OK,motointent);
        }
        else
            setResult(RESULT_CANCELED,motointent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // MODIFY
                if (resultCode == RESULT_OK) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        isModify = data.getBooleanExtra("isModify", true);

                        if (isModify) {
                            note = new Note(newnote);

                            TitleEditText_View.setText(note.getTitle());
                            UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
                            GroupNameTextView_View.setText(note.getGroupLabel().getName());
                            GroupNameTextView_View.setTextColor(ColorHex_IntEncoding(note.getGroupLabel().getColor()));
                            ContentEditText_View.post(new Runnable() {
                                @Override
                                public void run() {
                                    dealWithContent();
                                }
                            });
                        }
                    break;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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


    private void dealWithContent(){
        //showEditData(myContent);
        ContentEditText_View.clearAllLayout();
        showDataSync(note.getContent());

        // 图片点击事件
        ContentEditText_View.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                ArrayList<String> imageList = StringUtils.getTextFromHtml(note.getContent(), true);
                int currentPosition = imageList.indexOf(imagePath);
               // showToast("点击图片："+currentPosition+"："+imagePath);

                //点击图片预览
//                PhotoPreview.builder()
//                        .setPhotos(imageList)
//                        .setCurrentItem(currentPosition)
//                        .setShowDeleteButton(false)
//                        .start(NoteActivity.this);
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
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
//                        showToast("解析错误：图片不存在或已损坏");
//                        Log.e(TAG, "onError: " + e.getMessage());
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
                                    //imagePath可能是本地路径，也可能是网络地址
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
