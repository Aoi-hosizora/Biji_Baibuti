package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.daoInterface.IGroupDao;
import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.GroupAdapter;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.dialog.ImagePreviewDialog;
import com.baibuti.biji.ui.fragment.NoteFragment;
import com.baibuti.biji.util.fileUtil.AppPathUtil;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.imgDocUtil.ImageUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.util.fileUtil.SaveNameUtil;
import com.baibuti.biji.util.stringUtil.StringUtil;
import com.sendtion.xrichtext.RichTextEditor;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.kareluo.imaging.IMGEditActivity;
import rx_activity_result2.Result;
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
public class EditNoteActivity extends AppCompatActivity implements IContextHelper {

    /**
     * 笔记标题长度
     */
    private static final int CUT_LENGTH = 15;

    @BindView(R.id.id_modifynote_title)
    private EditText m_txt_title;

    @BindView(R.id.id_modifynote_group)
    private TextView m_txt_group;

    @BindView(R.id.id_modifynote_content)
    private RichTextEditor m_rich_content;

    private Dialog m_InsertImgPopupMenu;
    private Dialog m_LongClickImgPopupMenu;

    private Note currNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifynote);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // intent extra
        currNote = (Note) getIntent().getSerializableExtra(NoteFragment.INT_NOTE_DATA);

        boolean isNew = getIntent().getBooleanExtra(NoteFragment.INT_IS_NEW, true);
        setTitle(isNew ? "新建笔记" : "编辑笔记");

        m_txt_title.setText(currNote.getTitle());
        ((TextView) findViewById(R.id.id_modifynote_updatetime)).setText(currNote.getUpdateTime_ShortString());
        m_txt_group.setText(currNote.getGroup().getName());
        m_txt_group.setTextColor(currNote.getGroup().getIntColor());

        // m_rich_content

        m_rich_content.post(() -> initRichTextEditor(currNote.getContent()));
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modifynoteactivity_menu, menu);
        return true;
    }

    /**
     * 返回键取消保存判断
     */
    @Override
    public void onBackPressed() {
        ToolbarCancelSaveBack_Clicked();
    }

    // region Popup Image OCR

    /**
     * 插入图片显示弹出菜单
     */
    @OnItemSelected(R.id.id_menu_modifynote_img)
    private void ToolbarInsertImage_Clicked() {
        m_InsertImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_InsertImgPopupMenu, R.layout.popup_edit_note_insert_image);
        m_InsertImgPopupMenu.setOnCancelListener(null);

        root.findViewById(R.id.id_popmenu_choose_img).setOnClickListener((view) -> ChooseImgPopup_Clicked());
        root.findViewById(R.id.id_popmenu_open_camera).setOnClickListener((view) -> OpenCameraPopup_Clicked());
        root.findViewById(R.id.id_popmenu_cancel).setOnClickListener((view) -> m_InsertImgPopupMenu.cancel());

        m_InsertImgPopupMenu.show();
    }

    /**
     * 长按图片显示弹出菜单
     */
    private void imagePopup_LongClicked(String LongClickImgPath) {
        m_LongClickImgPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_LongClickImgPopupMenu, R.layout.popup_edit_note_long_click_image);
        m_LongClickImgPopupMenu.setOnCancelListener(null);

        root.findViewById(R.id.id_MNoteAct_PopupMenu_OCR).setOnClickListener((view) -> OCRLongClickImagePopup_Clicked(LongClickImgPath));
        root.findViewById(R.id.id_MNoteAct_PopupMenu_OCRCancel).setOnClickListener((view) -> m_LongClickImgPopupMenu.cancel());
    }

    /**
     * 从相册选择图片
     */
    private void ChooseImgPopup_Clicked() {
        m_InsertImgPopupMenu.cancel();
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        // 打开相册
        RxActivityResult.on(this).startIntent(galleryIntent)
            .map(Result::data)
            .subscribe((returnIntent) -> {
                // 编辑相册图片

                Uri uri = returnIntent.getData();
                if (uri != null) {
                    String imgPath = AppPathUtil.getFilePathByUri(this, uri);
                    if (imgPath == null || imgPath.isEmpty()) {
                        showAlert(this, "插入图片", "从相册获取的图片不存在，请重试。");
                        return;
                    }

                    Intent imgEditIntent = new Intent(this, IMGEditActivity.class);
                    imgEditIntent.putExtra(IMGEditActivity.INT_IMAGE_URI, uri);
                    imgEditIntent.putExtra(IMGEditActivity.INT_IMAGE_SAVE_URI, SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.EDITED));

                    RxActivityResult.on(this).startIntent(imgEditIntent)
                        .map(Result::data)
                        .subscribe((returnIntent2) -> {
                            Uri uri1 = returnIntent2.getData();
                            if (uri1 != null) {
                                // 无需删除图片
                                insertImagesSync(uri1);
                            }

                        }).isDisposed();
                }
            }).isDisposed();
    }

    /**
     * 打开相机
     */
    private void OpenCameraPopup_Clicked() {
        m_InsertImgPopupMenu.cancel();

        // 要保存的图片文件 _PHOTO 格式
        String filename = SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.PHOTO);

        // 拍照时返回的uri
        Uri imgUri = AppPathUtil.getUriByPath(this, filename);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);

        // 打开相机
        RxActivityResult.on(this).startIntent(cameraIntent)
            .map(Result::data)
            .subscribe((returnIntent) -> {
                // 编辑拍摄图片，并删除原图
                Uri uri = returnIntent.getData();
                if (uri != null) {
                    String imgPath = AppPathUtil.getFilePathByUri(this, uri);
                    if (imgPath == null || imgPath.isEmpty()) {
                        showAlert(this, "插入图片", "拍照得到的图片不存在，请重试。");
                        return;
                    }

                    Intent imgEditIntent = new Intent(this, IMGEditActivity.class);
                    imgEditIntent.putExtra(IMGEditActivity.INT_IMAGE_URI, uri);
                    imgEditIntent.putExtra(IMGEditActivity.INT_IMAGE_SAVE_URI, SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.EDITED));

                    RxActivityResult.on(this).startIntent(imgEditIntent)
                        .map(Result::data)
                        .subscribe((returnIntent2) -> {
                            Uri uri1 = returnIntent2.getData();
                            if (uri1 != null) {
                                // 拍照删除图片
                                AppPathUtil.deleteFile(AppPathUtil.getFilePathByUri(this, imgUri));
                                insertImagesSync(uri1);
                            }
                        }).isDisposed();
                }

            }, (throwable) -> {
                throwable.printStackTrace();
                showAlert(this, "错误", "相机打开失败，可能该设备没有相机或者设备出错，拍照插入图片功能暂时不可用。");
            }).isDisposed();
    }

    /**
     * 文字识别图片
     */
    private void OCRLongClickImagePopup_Clicked(String imgPath) {
        Intent intent = new Intent(EditNoteActivity.this, OCRActivity.class);
        intent.putExtra(OCRActivity.INT_IMAGE_PATH, imgPath);
        startActivity(intent);
    }

    // endregion

    // region Save

    /**
     * 判断是否修改
     */
    private Boolean checkIsNoteModify() {
        return
            !m_txt_title.getText().toString().equals(currNote.getTitle()) ||
            !m_txt_group.getText().toString().equals(currNote.getGroup().getName()) ||
            !getRichTextContent(m_rich_content).equals(currNote.getContent());
    }

    /**
     * 取消保存文件退出
     */
    @OnItemSelected({R.id.id_menu_modifynote_cancel, android.R.id.home})
    private void ToolbarCancelSaveBack_Clicked() {
        if (checkIsNoteModify())
            showAlert(this,
                "退出提醒", "确定要取消编辑吗？您的修改将不会保存。",
                "取消", null,
                "离开", (dialog, w) -> finish());
        else
            finish();
    }

    /**
     * 文件保存活动处理
     */
    @OnItemSelected(R.id.id_menu_modifynote_finish)
    private void ToolbarSaveNote_Clicked() {
        boolean[] isContinue = new boolean[] { true };

        CommonUtil.closeSoftKeyInput(this);

        // 获得笔记内容
        String Content = getRichTextContent(m_rich_content);
        if (Content.isEmpty())
            showAlert(this, "提醒", "没有输入内容，请补全笔记内容。");

        // 标题空
        if (m_txt_title.getText().toString().isEmpty()) {
            // 替换换行
            String newTitle = Content
                .replaceAll("[\n|\r].*", "")
                .replaceAll("<img src=.*", "[图片]");

            // 舍去过长的内容
            if (newTitle.length() > CUT_LENGTH + 3)
                newTitle = newTitle.substring(0, CUT_LENGTH) + "...";

            m_txt_title.setText(newTitle);
        }

        //////////////////////////////////////////////////
        // 具体保存过程

        Intent fromIntent = getIntent();
        boolean isNew = fromIntent.getBooleanExtra(NoteFragment.INT_IS_NEW, true);

        // 判断是否修改
        if (!checkIsNoteModify()) {
            Intent intent = new Intent();
            intent.putExtra(NoteFragment.INT_NOTE_DATA, currNote);
            intent.putExtra(NoteFragment.INT_IS_NEW, isNew); // <<< false
            intent.putExtra(NoteFragment.INT_IS_MODIFIED, false); // <<<
            setResult(RESULT_OK, intent);
            finish();

            return;
        }

        // 设置内容
        currNote.setTitle(m_txt_title.getText().toString());
        currNote.setContent(Content);

        IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(this);
        try {
            Group newGroup = groupDao.queryGroupByName(m_txt_group.getText().toString());
            if (newGroup == null)
                newGroup = groupDao.queryDefaultGroup();
            currNote.setGroup(newGroup);
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(this,
                "错误", "分组获取错误，将使用默认分组。",
                "确定", (dialog, which) -> {
                    try {
                        currNote.setGroup(groupDao.queryDefaultGroup());
                    } catch (ServerException innerEx) {
                        innerEx.printStackTrace();
                        showAlert(this,
                            "错误", "无网络相应，请检查网络连接",
                            "确定", (dialog2, w2) -> isContinue[0] = false
                        );
                    }
                }
            );
        }

        if (!isContinue[0]) return;

        // 保存操作
        try {
            INoteDao noteDao = DaoStrategyHelper.getInstance().getNoteDao(this);
            if (isNew) {
                long noteId = noteDao.insertNote(currNote);
                currNote.setId((int) noteId);
            } else {
                noteDao.updateNote(currNote);
            }

            Intent intent = new Intent();
            intent.putExtra(NoteFragment.INT_NOTE_DATA, currNote);
            intent.putExtra(NoteFragment.INT_IS_NEW, isNew); // <<<
            intent.putExtra(NoteFragment.INT_IS_MODIFIED, true);
            setResult(RESULT_OK, intent);
            finish();

        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(this,
                "错误", "无网络相应，请检查网络连接。",
                "确定", null
            );
        }
    }

    // endregion

    // region Info Group

    /**
     * 显示笔记详细信息
     */
    @OnItemSelected(R.id.id_menu_modifynote_info)
    private void ToolbarShowInfo_Clicked() {
        Intent fromIntent = getIntent();
        boolean isNew = fromIntent.getBooleanExtra(NoteFragment.INT_IS_NEW, true);

        if (isNew) {
            showAlert(this,
                "详细信息", "当前笔记还没保存，是否要保存？",
                "保存", (dialog, which) -> ToolbarSaveNote_Clicked(),
                "取消", null
            );
        }
        else {
            final String info =
                "笔记标题："     + currNote.getTitle()                   + "\n" +
                "笔记内容长度：" + currNote.getContent().length()        + "\n" +
                "创建时间："     + currNote.getCreateTime_FullString()   + "\n" +
                "最近修改时间：" + currNote.getUpdateTime_FullString()   + "\n\n" +
                "笔记分组："     + currNote.getGroup().getName();

            String modifyFlag = checkIsNoteModify() ? " (已修改)" : "";

            showAlert(this,
                "笔记信息" + modifyFlag, info,
                "复制", (dialog, which) -> {
                if (CommonUtil.copyText(this, info))
                    showToast(this, "信息复制成功");
                },
                "确定", null
            );
        }
    }

    /**
     * 显示分组设置
     */
    @OnItemSelected(R.id.id_menu_modifynote_group)
    private void ToolbarGroupSetting_Clicked() {
        boolean[] isContinue = new boolean[] { true };

        IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(this);
        final List<Group> groups = new ArrayList<>();

        try {
            groups.addAll(groupDao.queryAllGroups());
        } catch (ServerException ex) {
            showAlert(this,
                "错误", "无网络相应，请检查网络连接",
                "确定", (dialog2, w2) -> isContinue[0] = false
            );
        }

        if (!isContinue[0]) return;

        GroupAdapter groupAdapter = new GroupAdapter(this);
        groupAdapter.setList(groups);

        showAlert(this, "笔记分类",
            groupAdapter, (dialog, which) -> {
                m_txt_group.setText(groups.get(which).getName());
                m_txt_group.setTextColor(groups.get(which).getIntColor());
            }
        );
    }

    // endregion

    // region RichText ClickImage

    /**
     * 初始化富文本框，加载数据，图片点击...
     * @param text 初始化数据显示
     * m_rich_content.post(new Runnable() -> initRichTextEditor(););
     */
    private void initRichTextEditor(String text) {
        m_rich_content.clearAllLayout();
        showRichTextContentAsync(text);

        m_rich_content.setOnRtImageDeleteListener((imagePath) -> {
            if (!TextUtils.isEmpty(imagePath))
                if (AppPathUtil.deleteFile(imagePath))
                    Toast.makeText(EditNoteActivity.this, "图片删除成功", Toast.LENGTH_SHORT).show();
        });

        // 图片点击事件
        m_rich_content.setOnRtImageClickListener((imagePath) -> {
            if (!TextUtils.isEmpty(getRichTextContent(m_rich_content))) {
                List<String> imageList = StringUtil.getTextFromHtml(getRichTextContent(m_rich_content), true);
                int currentPosition = imageList.indexOf(imagePath);
                onClickImage(imageList, currentPosition);
            }
        });
    }

    /**
     * 图片点击预览
     * TODO 待改
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取 RichTextEditor 内容
     * @param richTextEditor RichTextEditor
     * @return 笔记内容
     */
    private String getRichTextContent(RichTextEditor richTextEditor) {
        if (richTextEditor == null) return "";

        List<RichTextEditor.EditData> editList = richTextEditor.buildEditData();
        StringBuilder content = new StringBuilder();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null)
                content.append(itemData.inputStr);
            else if (itemData.imagePath != null)
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
        }
        return content.toString();
    }

    /**
     * 异步显示笔记内容
     * @param html currNote.getContent()
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
                    if (text.contains("<img") && text.contains("src=")) {
                        // imagePath可能是本地路径，也可能是网络地址
                        String imagePath = StringUtil.getImgSrc(text);
                        // 插入空的EditText，以便在图片前后插入文字
                        m_rich_content.addEditTextAtIndex(m_rich_content.getLastIndex(), "");
                        m_rich_content.addImageViewAtIndex(m_rich_content.getLastIndex(), imagePath);
                    } else {
                        m_rich_content.addEditTextAtIndex(m_rich_content.getLastIndex(), text);
                    }
                },
                (throwable) -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    showToast(this, "解析错误：图片不存在或已损坏");
                },
                () -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    // 在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                    if (m_rich_content != null)
                        m_rich_content.addEditTextAtIndex(m_rich_content.getLastIndex(), "");
                }

            ).isDisposed();
    }

    /**
     * 异步插入图片
     * @param data 图片 Uri
     */
    private void insertImagesSync(final Uri data) {
        ProgressDialog progressDialog = showProgress(this, "插入图片中...", false, null);

        // TODO 整理

        Observable.create((ObservableEmitter<String> emitter) -> {
            try {
                m_rich_content.measure(0, 0);
                // data: _Edited
                int screenWidth = CommonUtil.getScreenWidth(this);
                int screenHeight = CommonUtil.getScreenHeight(this);



                Bitmap bitmap = ImageUtil.getBitmapFromPath(data.toString());
                bitmap = ImageUtil.compressImage(bitmap, screenWidth, screenHeight, true); // 等屏幕大小 压缩图片
                bitmap = ImageUtil.compressImage(bitmap); // 质量 压缩图片

                String smallImagePath = SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.SMALL);
                ImageUtil.saveBitmap(bitmap, smallImagePath);

                // smallImagePath: _Small

                AppPathUtil.deleteFile(data.toString()); // 删除 Edited
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

        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            (imagePath) -> m_rich_content.insertImage(imagePath, m_rich_content.getMeasuredWidth()),
            (throwable) -> {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(EditNoteActivity.this, "图片插入失败", Toast.LENGTH_SHORT).show();
            },
            () -> {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(EditNoteActivity.this, "图片插入成功", Toast.LENGTH_SHORT).show();
            }
        ).isDisposed();
    }

    // endregion
}
