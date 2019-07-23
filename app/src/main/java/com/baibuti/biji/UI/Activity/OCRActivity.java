package com.baibuti.biji.UI.Activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.Net.Models.Region;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.OCRView.OCRRegionGroupLayout;
import com.baibuti.biji.Utils.BitmapUtils;
import com.baibuti.biji.Utils.OCRRegionUtil;

/**
 * OCR 用
 *
 * Intent: INT_BUNDLE(Bundle) 传入 Bundle
 *
 * Bundle: INT_IMGPATH(String) 传入 图片路径
 */
public class OCRActivity extends AppCompatActivity implements IShowLog, View.OnClickListener {

    public static final String INT_BUNDLE = "INT_BUNDLE";
    public static final String INT_IMGPATH = "INT_IMGPATH";

    private ProgressDialog m_ocringProgressDlg;
    private OCRRegionGroupLayout m_ocrRegionGroupLayout;
    private TextView m_ocrResultTextView;
    private TextView m_ocrResultLabelTextView;
    private Button m_CopyButton;
    private Button m_SelectAllButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        Intent lastIntent = getIntent();
        Bundle bundle = lastIntent.getBundleExtra(INT_BUNDLE);
        String ImgPath = bundle.getString(INT_IMGPATH, "");


        initView(ImgPath);
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "OCRActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    /**
     * 初始化界面，显示图片和等待框
     * @param ImgPath
     */
    private void initView(String ImgPath) {
        setTitle(R.string.OCRActivity_Title);

        ShowLogE("initView", ImgPath);

        m_ocringProgressDlg = new ProgressDialog(this);
        m_ocringProgressDlg.setMessage(getString(R.string.OCRActivity_Loading));
        // m_ocringProgressDlg.show();

        m_ocrResultTextView = findViewById(R.id.id_OCRActivity_OCRResultTextView);
        m_ocrResultLabelTextView = findViewById(R.id.id_OCRActivity_OCRResultLabelTextView);
        m_ocrRegionGroupLayout = findViewById(R.id.id_OCRActivity_OCRRegionGroupLayout);
        m_CopyButton = findViewById(R.id.id_OCRActivity_CopyButton);
        m_SelectAllButton = findViewById(R.id.id_OCRActivity_SelectAllButton);

        m_CopyButton.setOnClickListener(this);
        m_SelectAllButton.setOnClickListener(this);
        m_ocrResultTextView.setText(R.string.OCRActivity_PleaseSelectHint);
        setEnabled(m_CopyButton, false);

        Region region = getRetTmp();

        m_ocrRegionGroupLayout.setImgBG(BitmapUtils.getBitmapFromFile(ImgPath));
        m_ocrRegionGroupLayout.setRegion(region);
        m_ocrRegionGroupLayout.setOnClickRegionsListener(new OCRRegionGroupLayout.onClickFramesListener() {

            @Override
            public void onClickFrames(Region.Frame[] frames) {
                // 文字更新
                m_ocrResultTextView.setText(OCRRegionUtil.getStrFromFrames(frames));

                // 数目更新
                int cnt = frames.length;
                setRetLabelText(cnt, m_ocrRegionGroupLayout.getRegion().getFrames().length);

                // 全选
                if (cnt == region.getFrames().length)
                    m_SelectAllButton.setText(getString(R.string.OCRActivity_CancelSelectAllButton));
                else
                    m_SelectAllButton.setText(getString(R.string.OCRActivity_SelectAllButton));

                // 复制
                setEnabled(m_CopyButton, cnt != 0);

                // 文字空
                if (cnt == 0)
                    m_ocrResultTextView.setText(R.string.OCRActivity_PleaseSelectHint);
            }
        });
    }

    private void setEnabled(Button button, boolean en) {
        button.setEnabled(en);
        if (en)
            button.setTextColor(getResources().getColor(R.color.light_pink));
        else
            button.setTextColor(getResources().getColor(R.color.grey_700));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_OCRActivity_CopyButton:
                CopyButton_Click();
            break;
            case R.id.id_OCRActivity_SelectAllButton:
                SelectAllButton_Click();
            break;
        }
    }

    /**
     * 复制点击
     */
    private void CopyButton_Click() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getResources().getString(R.string.MNoteActivity_OCRSyncResultAlertCopyClipLabel), m_ocrResultTextView.getText().toString());
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(this, R.string.OCRActivity_CopySuccess, Toast.LENGTH_SHORT).show();
    }

    /**
     * 选择全部点击
     */
    private void SelectAllButton_Click() {
        boolean isSelectAll = m_SelectAllButton.getText().toString().equals(getString(R.string.OCRActivity_SelectAllButton));
        m_ocrRegionGroupLayout.setAllFramesChecked(isSelectAll);
        if (isSelectAll)
            m_SelectAllButton.setText(getString(R.string.OCRActivity_CancelSelectAllButton));
        else
            m_SelectAllButton.setText(getString(R.string.OCRActivity_SelectAllButton));
    }

    /**
     * 设置标签提示的数量
     * @param selectCnt
     * @param allCnt
     */
    private void setRetLabelText(int selectCnt, int allCnt) {
        m_ocrResultLabelTextView.setText(String.format(getString(R.string.OCRActivity_RetLabel), selectCnt, allCnt));
    }

    private Region getRetTmp() {
        Region ret = new Region(
            new Region.Point(600, 400),
            3,
            new Region.Frame[] {
                new Region.Frame(
                    new Region.Point[] {
                        new Region.Point(342, 150),
                        new Region.Point(664, 115),
                        new Region.Point(679, 270),
                        new Region.Point(358, 305)
                    },
                    0.9,
                    "Half"
                ),
                new Region.Frame(
                    new Region.Point[] {
                        new Region.Point(878, 653),
                        new Region.Point(1021, 653),
                        new Region.Point(1021, 672),
                        new Region.Point(878, 672)
                    },
                    0.9,
                    "pm0336-1683hy"
                ),
                new Region.Frame(
                    new Region.Point[] {
                        new Region.Point(0, 651),
                        new Region.Point(251, 652),
                        new Region.Point(251, 682),
                        new Region.Point(0, 671)
                    },
                    0.9,
                    "全景网www.quanjing.com"
                )
            }
        );
        setRetLabelText(0, ret.getFrames().length);
        return ret;
    }
}
