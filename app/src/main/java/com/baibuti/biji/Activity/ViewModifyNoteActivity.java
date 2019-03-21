package com.baibuti.biji.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class ViewModifyNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView TitleEditText_View;
    private TextView UpdateTimeTextView_View;
    private TextView GroupNameTextView_View;
    private com.sendtion.xrichtext.RichTextView ContentEditText_View;

    private ProgressDialog loadingDialog;
    private Disposable mDisposable;

    private Note note;
    private int notePos;

    private boolean isModify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmodifyplainnote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("笔记详情");

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        note = (Note) getIntent().getSerializableExtra("notedata");
        notePos = getIntent().getIntExtra("notepos",0);

        TitleEditText_View = (TextView) findViewById(R.id.id_modifynote_viewtitle);
        UpdateTimeTextView_View = (TextView) findViewById(R.id.id_modifynote_viewupdatetime);
        GroupNameTextView_View = (TextView) findViewById(R.id.id_modifynote_viewgroup);
        ContentEditText_View = (com.sendtion.xrichtext.RichTextView) findViewById(R.id.id_modifynote_viewcontent);

        TitleEditText_View.setText(note.getTitle());
        UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
        GroupNameTextView_View.setText(note.getGroupLabel().getName());

        //////////////////////////////////////////////////
        ContentEditText_View.post(new Runnable() {
            @Override
            public void run() {
                dealWithContent();
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_modifynote_viewmodify:
                // isModify

                Intent intent=new Intent(ViewModifyNoteActivity.this, ModifyNoteActivity.class);
                intent.putExtra("notedata",note);
                intent.putExtra("flag",1); // UPDATE
                startActivityForResult(intent,1); // 1 from CardView

                break;

            case android.R.id.home:
            case R.id.id_menu_modifynote_viewcancel:
                Intent motointent = new Intent();

                if (isModify) {
                    motointent.putExtra("modify_note",note);
                    motointent.putExtra("modify_note_pos", notePos);
                    setResult(RESULT_OK,motointent);
                }
                else
                    setResult(RESULT_CANCELED,motointent);

                finish();
                break;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // MODIFY
                if (resultCode == RESULT_OK) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        isModify = data.getBooleanExtra("isModify", false);

                        if (isModify) {
                            note = new Note(newnote);

                            TitleEditText_View.setText(note.getTitle());
                            UpdateTimeTextView_View.setText(note.getUpdateTime_ShortString());
                            GroupNameTextView_View.setText(note.getGroupLabel().getName());
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
                        if (ContentEditText_View !=null) {
                            if (text.contains("<img") && text.contains("src=")) {
                                //imagePath可能是本地路径，也可能是网络地址
                                String imagePath = StringUtils.getImgSrc(text);
                                ContentEditText_View.addImageViewAtIndex(ContentEditText_View.getLastIndex(), imagePath);
                            } else {
                                ContentEditText_View.addTextViewAtIndex(ContentEditText_View.getLastIndex(), text);
                            }
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
