package com.baibuti.biji.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.SDCardUtil;
import com.baibuti.biji.util.StringUtils;
import com.sendtion.xrichtext.RichTextEditor;

import java.util.Date;
import java.util.List;

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
    private com.sendtion.xrichtext.RichTextEditor ContentEditText;
    private TextView UpdateTimeTextView;
    private TextView GroupNameTextView;

    private ProgressDialog loadingDialog;
    private Disposable subsLoading;

    private Note note;
    private int notePos;
    private GroupDao groupDao;
    private NoteDao noteDao;

    private int flag; // 0: NEW, 1: UPDATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifyplainnote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        note = (Note) getIntent().getSerializableExtra("notedata");
        notePos = getIntent().getIntExtra("notepos",0);
        flag = getIntent().getIntExtra("flag",0);

        if (flag == 0)
            setTitle("新建笔记");
        else
            setTitle("编辑笔记");

        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);

        TitleEditText = (EditText) findViewById(R.id.id_modifynote_title);
        ContentEditText = (com.sendtion.xrichtext.RichTextEditor) findViewById(R.id.id_modifynote_content);
        UpdateTimeTextView = (TextView) findViewById(R.id.id_modifynote_updatetime);
        GroupNameTextView = (TextView) findViewById(R.id.id_modifynote_group);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu,menu);
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
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        }).create();
                alertDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

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

        if (TitleEditText.getText().toString().length() == 0) {
            closeSoftKeyInput();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("没有输入标题，请补全标题")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create();
            alertDialog.show();
        } else {
            note.setTitle(TitleEditText.getText().toString());
            note.setContent(Content);

            Intent intent = new Intent();
            intent.putExtra("modify_note",note);

            if (flag==1) // UPDATE
                intent.putExtra("modify_note_pos",notePos);

            setResult(RESULT_OK,intent);
            closeSoftKeyInput();
            finish();

        }
    }

}
