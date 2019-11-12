package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.dialog.ImagePreviewDialog;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.fragment.NoteFragment;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.util.imgTextUtil.StringUtil;
import com.baibuti.biji.util.imgTextUtil.DocumentUtil;
import com.sendtion.xrichtext.RichTextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rx_activity_result2.RxActivityResult;

/**
 * Intent Extra:
 *      (Object) NoteFragment.INT_NOTE_DATA
 *      (boolean) NoteFragment.INT_IS_NEW
 * Return:
 *      (Object) NoteFragment.INT_NOTE_DATA
 *      (boolean) NoteFragment.INT_IS_NEW
 *      (boolean) NoteFragment.INT_IS_MODIFIED
 */
public class ViewNoteActivity extends AppCompatActivity implements IContextHelper {

    @BindView(R.id.id_modifynote_viewtitle)
    private TextView m_txt_title;

    @BindView(R.id.id_modifynote_viewcontent)
    private RichTextView m_rich_content;

    @BindView(R.id.id_modifynote_viewgroup)
    private TextView m_txt_group;

    @BindView(R.id.id_modifynote_viewupdatetime)
    private TextView m_txt_time;

    private Dialog m_LongClickImgPopupMenu;

    private Note currNote;
    /**
     * 每次从 EditNoteAct 都会更新
     */
    private boolean isModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmodifynote);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("查看笔记");

        currNote = (Note) getIntent().getSerializableExtra(NoteFragment.INT_NOTE_DATA);

        m_txt_title.setText(currNote.getTitle());
        m_txt_time.setText(currNote.getUpdateTime_ShortString());
        m_txt_group.setText(currNote.getGroup().getName());
        m_txt_group.setTextColor(currNote.getGroup().getIntColor());

        //////////////////////////////////////////////////

        m_rich_content.post(() -> initRichTextEditor(currNote.getContent()));
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewmodifynoteactivity_menu,menu);
        return true;
    }

    /**
     * 返回
     */
    @Override
    public void onBackPressed() {
        ToolbarCancelSaveBack_Clicked();
    }

    // region Edit Back

    /**
     * 编辑笔记
     */
    @OnItemSelected(R.id.id_menu_modifynote_viewmodify)
    private void ToolbarEditNote_Clicked() {

        Intent intent = new Intent(ViewNoteActivity.this, EditNoteActivity.class);

        intent.putExtra(NoteFragment.INT_NOTE_DATA, currNote);
        intent.putExtra(NoteFragment.INT_IS_NEW, false);

        RxActivityResult.on(this).startIntent(intent)
            .subscribe((result) -> {
                if (result.resultCode() == RESULT_OK) {
                    Intent returnIntent = result.data();

                    currNote = (Note) returnIntent.getSerializableExtra(NoteFragment.INT_NOTE_DATA);
                    // 没更新过才更新状态
                    if (!isModified)
                    isModified = returnIntent.getBooleanExtra(NoteFragment.INT_IS_MODIFIED, true);

                    if (isModified) {
                        m_txt_title.setText(currNote.getTitle());
                        m_txt_time.setText(currNote.getUpdateTime_ShortString());
                        m_txt_group.setText(currNote.getGroup().getName());
                        m_txt_group.setTextColor(currNote.getGroup().getIntColor());
                        m_rich_content.post(() -> initRichTextEditor(currNote.getContent()));
                    }
                }
            }).isDisposed();
    }

    /**
     * 返回主界面 修改或未修改
     */
    @OnItemSelected({R.id.id_menu_modifynote_viewcancel, android.R.id.home})
    private void ToolbarCancelSaveBack_Clicked() {
        Intent motoIntent = getIntent();
        Intent intent = new Intent();

        boolean isNew = motoIntent.getBooleanExtra(NoteFragment.INT_IS_NEW, true);

        if (isModified) {
            // isNew:  NoteFrag -> EditNote -> NoteFrag --> ViewNote
            // !isNew: NoteFrag -> ViewNote -> EditNote -> ViewNote

            intent.putExtra(NoteFragment.INT_NOTE_DATA, currNote);
            intent.putExtra(NoteFragment.INT_IS_NEW, isNew);
            intent.putExtra(NoteFragment.INT_IS_MODIFIED, true);
            setResult(RESULT_OK, intent);
        }
        else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    // endregion

    // region Info Share Save

    /**
     * 显示笔记详细信息
     */
    @OnItemSelected(R.id.id_menu_modifynote_info)
    private void ToolbarShowInfo_Clicked() {
        final String info =
            "笔记标题：" + currNote.getTitle() + "\n" +
            "笔记内容长度：" + currNote.getContent().length() + "\n" +
            "创建时间：" + currNote.getCreateTime_FullString() + "\n" +
            "最近修改时间：" + currNote.getUpdateTime_FullString() + "\n\n" +
            "笔记分组：" + currNote.getGroup().getName();

        showAlert(this,
            "笔记信息", info,
            "复制", (dialog, which) -> {
                if (CommonUtil.copyText(this, info))
                    showToast(this, "信息复制成功");
            },
            "确定", null
        );
    }

    /**
     * 分享笔记
     * TODO
     */
    @OnItemSelected(R.id.id_menu_modifynote_sharenote)
    private void ToolbarShare_Clicked() {
        CommonUtil.shareTextAndImage(this, currNote.getTitle(), currNote.getContent(), null); //分享图文
    }

    /**
     * 打开保存为文件的路径选择活动
     */
    @OnItemSelected(R.id.id_menu_modifynote_turntofile)
    private void ToolbarSaveDocument_Clicked() {
        Intent choosePathIntent=new Intent(this, OpenSaveFileActivity.class);

        choosePathIntent.putExtra("isSaving", true);
        choosePathIntent.putExtra("FileType", ".docx");
        choosePathIntent.putExtra("CurrentDir", AppPathUtil.getFileNoteDir());
        choosePathIntent.putExtra("FileName", currNote.getTitle());
        choosePathIntent.putExtra("FileFilterType", "docx|pdf");

        // 返回含后缀名的文件名，并且单独返回后缀名
        choosePathIntent.putExtra("isReturnType", "true");

        // 打开选择文件名与类型
        RxActivityResult.on(this).startIntent(choosePathIntent)
            .subscribe((result) -> {
                if (result.resultCode() == RESULT_OK) {
                    // -> ToolbarSaveDocument_Clicked() 访问 OpenSaveFileActivity

                    Intent returnIntent = result.data();

                    String path = returnIntent.getStringExtra("filePath"); // 包含后缀名
                    String type = returnIntent.getStringExtra("type");

                    // 确认保存格式与路径
                    if (!path.isEmpty()) {
                        boolean saveAsDocx = !".pdf".equals(type);

                        String msg = String.format(Locale.CHINA, "确定将笔记保存为 %s 类型，并保存在以下路径吗？\\n%s", type, path);
                        if (!saveAsDocx)
                            msg += "\\n(提醒：PDF 格式对符号支持不好)";

                        showAlert(this,
                            "保存为文件", msg,
                            "保存", (dialog, w) -> {

                                // 判断是否覆盖
                                File file = new File(path);
                                if (file.exists()) {
                                    showAlert(this,
                                        "保存", String.format(Locale.CHINA, "文件 \"%s\" 已存在，是否覆盖？", path),
                                        "覆盖", (dialog1, w1) -> saveDocument(saveAsDocx, path, currNote),
                                        "取消", null
                                    );
                                } else
                                    // 保存
                                    saveDocument(saveAsDocx, path, currNote);
                            },
                            "取消", null
                        );
                    }
                }
            }).isDisposed();
    }

    /**
     * 保存笔记
     */
    private void saveDocument(boolean isSaveAsDocx, String path, Note note) {
        boolean[] dismiss = new boolean[] { false };
        ProgressDialog progressDialog = showProgress(this,
            String.format(Locale.CHINA, "%s 文件正在保存到 \"%s\"...", isSaveAsDocx ? "Docx" : "Pdf", path),
            true, (dialog) -> dismiss[0] = true
        );

        new Thread(() -> {
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            boolean isOk;
            if (isSaveAsDocx)
                isOk = DocumentUtil.CreateDocxByNote(path, note.getTitle(), note.getContent(), true);
            else
                isOk = DocumentUtil.CreatePdfByNote(path, note.getTitle(), note.getContent(), true);

            runOnUiThread(() -> {
                if (!dismiss[0]) {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();

                    showToast(this, String.format(Locale.CHINA, "文件 \"%s\" 保存 %s", path, isOk ? "成功" : "失败"));
                } else {
                    AppPathUtil.deleteFile(path);
                }
            });
        }).start();
    }

    // endregion

    // region Image OCR

    /**
     * 图片点击预览
     */
    private void onClickImage(List<String> imageList, int currentPosition) {
        try {
            String[] imagePaths = imageList.toArray(new String[0]);
            ImagePreviewDialog dialog = new ImagePreviewDialog(this, imagePaths, currentPosition);
            dialog.setOnLongClickImageListener((v, index) -> imagePopup_LongClicked(imagePaths[index]));
            dialog.show();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 长按图片 弹出菜单
     */
    private void imagePopup_LongClicked(String imagePath) {
        m_LongClickImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_LongClickImgPopupMenu, R.layout.popup_view_note_long_click_image);
        m_LongClickImgPopupMenu.setOnCancelListener(null);

        root.findViewById(R.id.id_VMNoteAct_PopupMenu_OCR).setOnClickListener((view) -> OCRLongClickImagePopup_Clicked(imagePath));
        root.findViewById(R.id.id_VMNoteAct_PopupMenu_Cancel).setOnClickListener((view) -> m_LongClickImgPopupMenu.cancel());

        m_LongClickImgPopupMenu.show();
    }

    /**
     * 文字识别
     */
    private void OCRLongClickImagePopup_Clicked(String imagePath) {
        Intent intent = new Intent(ViewNoteActivity.this, OCRActivity.class);
        intent.putExtra(OCRActivity.INT_IMAGE_PATH, imagePath);
        startActivity(intent);
    }

    // endregion

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // region RichTextView

    /**
     * 初始化富文本框，加载数据，图片点击...
     * @param text 初始化数据显示
     * m_rich_content.post(new Runnable() -> initRichTextEditor(););
     */
    private void initRichTextEditor(String text) {
        m_rich_content.clearAllLayout();
        showRichTextContentAsync(text);

        m_rich_content.setOnRtImageClickListener((imagePath) -> {
            List<String> imageList = StringUtil.getTextFromHtml(currNote.getContent(), true);
            int currentPosition = imageList.indexOf(imagePath);
            onClickImage(imageList, currentPosition);
        });
    }

    /**
     * 异步显示笔记内容
     */
    private void showRichTextContentAsync(final String html) {
        ProgressDialog progressDialog = showProgress(this, "数据加载中...", false, null);

        Observable.create((ObservableEmitter<String> emitter) -> {
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
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                (text) -> {
                    try {
                        if (text.contains("<img") && text.contains("src=")) {
                            // imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtil.getImgSrc(text);
                            m_rich_content.addImageViewAtIndex(m_rich_content.getLastIndex(), imagePath);
                        } else {
                            m_rich_content.addTextViewAtIndex(m_rich_content.getLastIndex(), text);
                        }
                    } catch (Exception ex) {
                        showToast(this, "笔记中图片显示错误，可能由于源文件被删除。");
                    }
                },
                (throwable) -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    // showToast(this, "解析错误：图片不存在或已损坏");
                },
                () -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            ).isDisposed();
    }

    // endregion
}
