package com.baibuti.biji.UI.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.Net.Models.RespObj.Region;
import com.baibuti.biji.Net.OCR.OCRRetUtil;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.OCRView.OCRRegionGroupLayout;
import com.baibuti.biji.Utils.ImgDocUtils.BitmapUtils;
import com.baibuti.biji.Utils.LayoutUtils.OCRRegionUtil;

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

    /**
     * 获取数据延迟
     */
    private int MS_GETDATA = 1000;

    /**
     * 是否已经取消识别
     */
    private boolean isCanceled = false;

    private Region m_region;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        Intent lastIntent = getIntent();
        Bundle bundle = lastIntent.getBundleExtra(INT_BUNDLE);
        String ImgPath = bundle.getString(INT_IMGPATH, "");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ShowLogE("onCreate", "ImgPath" + ImgPath);
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
        m_ocringProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                new AlertDialog.Builder(OCRActivity.this)
                    .setTitle(R.string.OCRActivity_CancelCheckTitle)
                    .setMessage(R.string.OCRActivity_CancelCheckMsg)
                    .setPositiveButton(R.string.OCRActivity_CancelCheckOK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isCanceled = true;
                            dialog.dismiss();
                            finish();
                            Toast.makeText(OCRActivity.this, R.string.OCRActivity_Canceled, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.OCRActivity_CancelCheckNo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isCanceled = false;
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            }
        });
        m_ocringProgressDlg.show();

        m_ocrResultTextView = findViewById(R.id.id_OCRActivity_OCRResultTextView);
        m_ocrResultLabelTextView = findViewById(R.id.id_OCRActivity_OCRResultLabelTextView);
        m_ocrRegionGroupLayout = findViewById(R.id.id_OCRActivity_OCRRegionGroupLayout);
        m_CopyButton = findViewById(R.id.id_OCRActivity_CopyButton);
        m_SelectAllButton = findViewById(R.id.id_OCRActivity_SelectAllButton);

        m_CopyButton.setOnClickListener(this);
        m_SelectAllButton.setOnClickListener(this);
        m_ocrResultTextView.setText(R.string.OCRActivity_PleaseSelectHint);
        setEnabled(m_CopyButton, false);
        setRetLabelText(0, 0);

        m_ocrRegionGroupLayout.setImgBG(BitmapUtils.getBitmapFromFile(ImgPath));

        // 延迟
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getRetTmp(ImgPath);
                // TODO !!!!!
//                getRet(ImgPath);
            }
        }, MS_GETDATA); // 1000
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 获得数据后更新界面
     */
    private void setupOCRLayout() {

        // 1.Label Cnt
        setRetLabelText(0, m_region.getFrames().length);

        // 2.Region
        m_ocrRegionGroupLayout.setRegion(m_region);

        // 3.Click
        m_ocrRegionGroupLayout.setOnClickRegionsListener(new OCRRegionGroupLayout.onClickFramesListener() {

            @Override
            public void onClickFrames(Region.Frame[] frames) {
                // 文字更新
                m_ocrResultTextView.setText(OCRRegionUtil.getStrFromFrames(frames));

                // 数目更新
                int cnt = frames.length;
                setRetLabelText(cnt, m_ocrRegionGroupLayout.getRegion().getFrames().length);

                // 全选
                if (cnt == m_region.getFrames().length)
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

    /**
     * 获得 结果，并调用设置布局 (dev env)
     * @param url
     */
    private void getRetTmp(String url) {
        Region ret = new Region(
            new Region.Point(600, 400),
            3,
            new Region.Frame[] {
                new Region.Frame(342, 150, 664, 115, 679, 270, 358, 305, 0.9, "Half"),
                new Region.Frame(878, 653, 1021, 653, 1021, 672, 879, 673,0.9, "pm0336-1683hy"),
                new Region.Frame(0, 651, 251, 652, 251, 682, 0, 671,0.9,"全景网www.quanjing.com"),
                new Region.Frame(167, 430, 843, 366, 854, 496, 179, 560, 0.9, "Weschubiahnten"),
                new Region.Frame(426, 308, 556, 303, 559, 381, 430, 388, 0.9, "执待")
            }
        );

        m_region = ret;

        if (m_ocringProgressDlg.isShowing())
            m_ocringProgressDlg.dismiss();

        if (!isCanceled)
            setupOCRLayout();
    }

    /**
     * 访问网络获得获得结果，等待信号
     * @param url
     */
    private void getRet(String url) {

        // TODO
        new Thread(new Runnable() {
            @Override
            public void run() {
                Region ret = OCRRetUtil.getOCRRet(url);

                Message message = new Message();
                message.what = HandleWhat.HND_OCRRet;

                Bundle bundle = new Bundle();
                bundle.putSerializable(HandleWhat.BND_Region, ret);
                message.setData(bundle);

                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 返回 OCR 结果，并调用设置布局
     * @param msg
     */
    private void onHandleRetOCR(Message msg) {
        Bundle bundle = msg.getData();
        m_region = (Region) bundle.getSerializable(HandleWhat.BND_Region);

        if (m_ocringProgressDlg.isShowing())
            m_ocringProgressDlg.dismiss();

        if (m_region == null)
            new AlertDialog.Builder(this)
                .setTitle(R.string.OCRActivity_ErrorTitle)
                .setMessage(R.string.OCRActivity_ErrorMessage)
                .setPositiveButton(R.string.OCRActivity_ErrorReturnButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .create().show();
        else
            if (!isCanceled)
                setupOCRLayout();
    }

    class HandleWhat {
        /**
         * 处理搜索响应
         */
        static final int HND_OCRRet = 1;

        static final String BND_Region = "BND_Region";
    }


    /**
     * 网络信号处理
     */
    private final Handler handler = new Handler((Message msg) -> {
        switch (msg.what) {
            case HandleWhat.HND_OCRRet:
                onHandleRetOCR(msg);
                break;
        }
        return false;
    });
}
