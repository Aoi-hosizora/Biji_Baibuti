package com.baibuti.biji.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.service.ocr.OCRService;
import com.baibuti.biji.service.ocr.dto.OCRFrame;
import com.baibuti.biji.service.ocr.dto.OCRPoint;
import com.baibuti.biji.service.ocr.dto.OCRRegion;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.widget.ocrView.OCRRegionGroupLayout;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.filePathUtil.FileNameUtil;
import com.baibuti.biji.util.imgTextUtil.ImageUtil;
import com.baibuti.biji.util.otherUtil.CommonUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * OCR 用
 *
 * Intent: INT_BUNDLE(Bundle) 传入 Bundle
 *
 * Bundle: INT_IMAGE_PATH(String) 传入 图片路径
 */
public class OCRActivity extends AppCompatActivity implements IContextHelper {

    public static final String INT_IMAGE_PATH = "INT_IMAGE_PATH";

    private ProgressDialog m_ocrProgressDialog;
    private boolean isOCRCancel = false;

    @BindView(R.id.id_OCRActivity_OCRRegionGroupLayout)
    OCRRegionGroupLayout m_layout_region;

    @BindView(R.id.id_OCRActivity_OCRResultTextView)
    TextView m_txt_result;

    @BindView(R.id.id_OCRActivity_OCRResultLabelTextView)
    TextView m_txt_count;

    @BindView(R.id.id_OCRActivity_CopyButton)
    Button m_btn_copy;

    @BindView(R.id.id_OCRActivity_SelectAllButton)
    Button m_btn_selectAll;

    /**
     * 获取数据延迟
     */
    private static final int MS_GET_DATA_DELAY = 1000;

    private OCRRegion m_region;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        String imgPath = getIntent().getStringExtra(INT_IMAGE_PATH); // 本地 / 网络地址
        initView(imgPath);
    }

    /**
     * 初始化界面，显示图片和等待框
     * @param imgPath 本地 (原始图片) / 网络地址
     */
    private void initView(String imgPath) {
        setTitle("文字识别");

        Log.i("", "initView: " + imgPath);

        m_ocrProgressDialog = showProgress(this,
            "正在识别文字...",
            true, (dialog) -> showAlert(this,
                "文字识别", "是否取消操作？",
                "取消", (dialog1, w1) -> {
                    isOCRCancel = true;
                    dialog1.dismiss();
                    finish();
                    showToast(this, "操作已取消");
                },
                "保留", (dialog1, w1) -> {
                    isOCRCancel = false;
                    dialog1.dismiss();
                }
            )
        );

        m_txt_result.setText(R.string.OCRActivity_PleaseSelectHint);
        setEnabled(m_btn_copy, false);
        setRetLabelText(0, 0);

        initBG(imgPath);
    }

    /**
     * 加载背景
     * @param imgPath 本地 / 网络地址
     */
    private void initBG(String imgPath) {

        // 压缩图片

        // imgPath _Edited
        // smallImagePath _Small

        int screenWidth = CommonUtil.getScreenWidth(this);
        int screenHeight = CommonUtil.getScreenHeight(this);

        Bitmap bg = ImageUtil.getBitmapFromPath(imgPath);

        if (bg == null) {
            new Thread(() -> ImageUtil.getImgAsync(this, imgPath, (bitmap) -> {

                if (bitmap == null) {
                    showAlert(this, "错误", "网络连接错误，请重试", "返回", (dialog, which) -> finish());
                    return;
                }

                // TODO 保存文件
                Bitmap net_bg = ImageUtil.compressImage(bitmap, screenWidth, screenHeight, true);
                net_bg = ImageUtil.compressImage(net_bg);
                String fileName = FileNameUtil.getImageFileName(FileNameUtil.SaveType.OCR);
                ImageUtil.saveBitmap(bitmap, fileName);

                m_layout_region.setImgBG(net_bg);
                toOCRHandler(fileName);

            })).start();
        }
        else {
            bg = ImageUtil.compressImage(bg, screenWidth, screenHeight, true);
            bg = ImageUtil.compressImage(bg);

            m_layout_region.setImgBG(bg);
            toOCRHandler(imgPath);
        }
    }

    /**
     * 加载完背景，启动 OCR 延迟
     * @param localDir 本地地址
     */
    private void toOCRHandler(String localDir) {

        Log.e("", "toOCRHandler: localDir = " + localDir );

        // 延迟
        new Handler().postDelayed(() -> {
            getRetTmp(localDir);
            // TODO !!!!!
            // getRet($bitmap);
        }, MS_GET_DATA_DELAY); // 1000
    }

    /**
     * 获得数据后更新界面
     */
    private void setupOCRLayout() {

        // 1.Label Cnt
        setRetLabelText(0, m_region.getFrames().length);

        // 2.OCRRegion
        m_layout_region.setRegion(m_region);

        // 3.Click
        m_layout_region.setOnClickRegionsListener((OCRFrame[] frames) -> {
            // 文字更新
            m_txt_result.setText(OCRFrame.getStrFromFrames(frames));

            // 数目更新
            int cnt = frames.length;
            setRetLabelText(cnt, m_layout_region.getRegion().getFrames().length);

            // 全选
            if (cnt == m_region.getFrames().length)
                m_btn_selectAll.setText(getString(R.string.OCRActivity_CancelSelectAllButton));
            else
                m_btn_selectAll.setText(getString(R.string.OCRActivity_SelectAllButton));

            // 复制
            setEnabled(m_btn_copy, cnt != 0);

            // 文字空
            if (cnt == 0)
                m_txt_result.setText(R.string.OCRActivity_PleaseSelectHint);
        });
    }

    /**
     * 自定义按钮的 setEnabled，手动置灰
     */
    private void setEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setTextColor(getResources().getColor(enabled ? R.color.colorAccent : R.color.grey_700));
    }

    /**
     * 复制
     */
    @OnClick(R.id.id_OCRActivity_CopyButton)
    void CopyButton_Click() {
        if (CommonUtil.copyText(this, m_txt_result.getText().toString()))
            Toast.makeText(this, R.string.OCRActivity_CopySuccess, Toast.LENGTH_SHORT).show();
    }

    /**
     * 全选
     */
    @OnClick(R.id.id_OCRActivity_SelectAllButton)
    void SelectAllButton_Click() {
        boolean isSelectAll = m_btn_selectAll.getText().toString().equals(getString(R.string.OCRActivity_SelectAllButton));
        m_layout_region.setAllFramesChecked(isSelectAll);
        if (isSelectAll)
            m_btn_selectAll.setText(getString(R.string.OCRActivity_CancelSelectAllButton));
        else
            m_btn_selectAll.setText(getString(R.string.OCRActivity_SelectAllButton));
    }

    /**
     * 菜单 返回
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    /**
     * 设置标签提示的数量
     * @param selectCnt 选择了几项
     * @param allCnt 共几项
     */
    private void setRetLabelText(int selectCnt, int allCnt) {
        m_txt_count.setText(String.format(getString(R.string.OCRActivity_RetLabel), selectCnt, allCnt));
    }

    /**
     * 获得 结果，并调用设置布局 (dev env)
     * @param localDir 本地地址
     */
    private void getRetTmp(String localDir) {

        // OCR 临时存储
        if (localDir.contains(AppPathUtil.getOCRTmpDir()))
            if (AppPathUtil.deleteFile(localDir))
                Log.e("", "run: delete OCR tmp" + localDir );

        m_region = new OCRRegion(
            new OCRPoint(600, 400),
            3,
            new OCRFrame[] {
                new OCRFrame(342, 150, 664, 115, 679, 270, 358, 305, 0.9, "Half"),
                new OCRFrame(878, 653, 1021, 653, 1021, 672, 879, 673,0.9, "pm0336-1683hy"),
                new OCRFrame(0, 651, 251, 652, 251, 682, 0, 671,0.9,"全景网www.quanjing.com"),
                new OCRFrame(167, 430, 843, 366, 854, 496, 179, 560, 0.9, "Weschubiahnten"),
                new OCRFrame(426, 308, 556, 303, 559, 381, 430, 388, 0.9, "执待")
            }
        );

        if (m_ocrProgressDialog != null && m_ocrProgressDialog.isShowing())
            m_ocrProgressDialog.dismiss();

        if (!isOCRCancel)
            setupOCRLayout();
        else
            finish();
    }

    /**
     * 访问网络获得获得结果
     * @param localDir 本地地址
     */
    private void getRet(String localDir) {

        m_region = OCRService.getOCRRet(localDir);

        if (m_ocrProgressDialog != null && m_ocrProgressDialog.isShowing())
            m_ocrProgressDialog.dismiss();

        if (m_region == null) {
            showAlert(this, "错误", "识别失败，请重试。", "返回", (dialog, w) -> finish());
        }
        else if (!isOCRCancel)
            setupOCRLayout();
        else
            finish();
    }
}
