package com.baibuti.biji.UI.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.Net.Models.Region;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.OCRView.OCRRegionGroupLayout;

/**
 * OCR 用
 *
 * Intent: INT_BUNDLE(Bundle) 传入 Bundle
 *
 * Bundle: INT_IMGPATH(String) 传入 图片路径
 */
public class OCRActivity extends AppCompatActivity implements IShowLog {

    public static final String INT_BUNDLE = "INT_BUNDLE";
    public static final String INT_IMGPATH = "INT_IMGPATH";

    private ProgressDialog m_ocringProgressDlg;
    private OCRRegionGroupLayout m_ocrRegionGroupLayout;

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
        setTitle(R.string.OCTActivity_Title);

        ShowLogE("initView", ImgPath);

        m_ocringProgressDlg = new ProgressDialog(this);
        m_ocringProgressDlg.setMessage(getString(R.string.OCTActivity_Loading));
        // m_ocringProgressDlg.show();

        m_ocrRegionGroupLayout = findViewById(R.id.id_OCRActivity_OCRRegionGroupLayout);

        Region region = new Region(
            new Region.Point(600, 400),
            1,
            new Region.Frame[] {
                new Region.Frame(
                    new Region.Point[] {
                        new Region.Point(20, 50),
                        new Region.Point(260, 50),
                        new Region.Point(260, 400),
                        new Region.Point(20, 400)
                    },
                    0.9,
                    "111"
                ),
                new Region.Frame(
                    new Region.Point[] {
                        new Region.Point(120, 150),
                        new Region.Point(600, 150),
                        new Region.Point(600, 200),
                        new Region.Point(120, 200)
                    },
                    0.9,
                    "222"
                )
            }
        );

        m_ocrRegionGroupLayout.setImgUrl(ImgPath);
        m_ocrRegionGroupLayout.setRegion(region);
    }
}
