package com.baibuti.biji.Utils;


import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.baibuti.biji.UI.Widget.CustomXWPFDocument;

import org.apache.poi.xslf.usermodel.TextAlign;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * Using POI & IText By AoiHosizora
 */
public class ToDocUtil {

    public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    public static String APP_NAME = "Biji" + File.separator;
    public static String SAVE_FILETYPE = "NoteFile";

    public static int A4_WIDTH = 2480;
    public static int A4_HEIGHT = 3508;
    public static double A4_PXRATE = 215.0 / 700;
    public static int A4_WIDTH_OUTOFEDGE = (int)(1775 * A4_PXRATE);
    public static int A4_HEIGHT_OUTOFEDGE = (int)(5628 * A4_PXRATE);

    private static class Size {
        private int Height;
        private int Width;

        public Size(int width, int height) {
            this.Height = height;
            this.Width = width;
        }

        public int getHeight() {
            return Height;
        }

        public int getWidth() {
            return Width;
        }

        public void setHeight(int height) {
            Height = height;
        }

        public void setWidth(int width) {
            Width = width;
        }
    }

    /**
     * 修改图片尺寸至适合 A4
     * @param motoSize
     * @return
     */
    private static Size HandleImgSize(Size motoSize) {
        int width = motoSize.getWidth();
        int height = motoSize.getHeight();

        Log.e("0", width + ", " + height);

        if (width > A4_WIDTH_OUTOFEDGE) {
            height = (int)(height * (A4_WIDTH_OUTOFEDGE / (double)width));
            width = A4_WIDTH_OUTOFEDGE;
        }

        if (height > A4_HEIGHT_OUTOFEDGE) {
            width = (int)(width * (A4_HEIGHT_OUTOFEDGE / (double)height));
            height = A4_HEIGHT_OUTOFEDGE;
        }

        Log.e("1", width + ", " + height);

        return new Size(height, width);
    }

    /**
     * 获得 生成的文件 默认的保存位置
     * @return
     */
    public static String getDefaultPath() {
        String path = SDCardRoot + APP_NAME + SAVE_FILETYPE + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return path;
    }

    /**
     * 创建 Docx 文件，插入笔记标题与内容
     *
     * @param Path        Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "..."
     * @param NoteTitle
     * @param NoteContent
     * @return
     * @throws IOException
     */
    public static boolean CreateDocxByNote(String Path, String NoteTitle, String NoteContent, boolean IsReWrite) throws Exception {
        File docFile = new File(Path);
        if (docFile.exists())
            if (IsReWrite)
                docFile.delete();
            else
                return false;
        Log.e("TAG", "CreateDocxByNote: "+Path );
        CustomXWPFDocument docx = new CustomXWPFDocument();

        XWPFParagraph para = docx.createParagraph();
        XWPFRun run = para.createRun();
        run.setUnderline(UnderlinePatterns.SINGLE);
        run.setTextPosition(TextAlign.CENTER.ordinal());
        run.setText(NoteTitle);

        HandleDocxRunForNoteContent(docx, NoteContent);

        OutputStream os = new FileOutputStream(Path);

        docx.write(os);
        try {
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * 处理用于 Docx 文件的 XWPFRun 插入文字与图片，CreateDocxByNote() 用
     *
     * @param docx        CustomXWPFDocument
     * @param NoteContent 笔记内容
     */
    private static void HandleDocxRunForNoteContent(CustomXWPFDocument docx, String NoteContent) throws Exception {
        XWPFRun run;

        ////////////////////////////////////////
        List<String> textList = StringUtils.cutStringByImgTag(NoteContent);

        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);
            if (text.contains("<img") && text.contains("src=")) {
                String imagePath = StringUtils.getImgSrc(text);

                //////////

                FileInputStream fis = new FileInputStream(imagePath);

                // 图片信息
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, opt);

                // 后缀名
                String ext = NoteContent.substring(NoteContent.lastIndexOf(".") + 1).toLowerCase();

                // 图片尺寸
                Size newSize = HandleImgSize(new Size(opt.outWidth, opt.outHeight));

                String picId = docx.addPictureData(fis, getPictureType(ext));
                docx.createPicture(picId, docx.getNextPicNameNumber(getPictureType(ext)), newSize.getWidth(), newSize.getHeight());

            }
            else {
                XWPFParagraph para = docx.createParagraph();
                run = para.createRun();
                run.setText(text);
            }
        }
    }

    /**
     * 获取图片类型
     *
     * @param ext 文件后缀名
     * @return XWPFDocument.PICTURE_TYPE_PICT
     */
    private static int getPictureType(String ext) {
        int res = XWPFDocument.PICTURE_TYPE_PICT;

        if (ext != null) {

            if (ext.equalsIgnoreCase("png"))
                res = XWPFDocument.PICTURE_TYPE_PNG;
            else if (ext.equalsIgnoreCase("dib"))
                res = XWPFDocument.PICTURE_TYPE_DIB;
            else if (ext.equalsIgnoreCase("emf"))
                res = XWPFDocument.PICTURE_TYPE_EMF;
            else if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
                res = XWPFDocument.PICTURE_TYPE_JPEG;
            else if (ext.equalsIgnoreCase("wmf"))
                res = XWPFDocument.PICTURE_TYPE_WMF;
        }
        return res;
    }

    /**
     * 创建 Pdf 文件，插入笔记标题与内容
     *
     * @param Path
     * @param NoteTitle
     * @param NoteContent
     * @param IsReWrite
     * @return
     * @throws Exception
     */
    public static boolean CreatePdfByNote(String Path, String NoteTitle, String NoteContent, boolean IsReWrite) throws Exception {
        File docFile = new File(Path);
        if (docFile.exists())
            if (IsReWrite)
                docFile.delete();
            else
                return false;

        PdfItextUtil pdfItextUtil = new PdfItextUtil(Path);

        // 处理标题
        pdfItextUtil.addTitleToPdf(NoteTitle);

        // 处理内容
        HandlePdfiTexForNoteContent(pdfItextUtil, NoteContent);

        if (pdfItextUtil != null)
            pdfItextUtil.close();
        return true;
    }

    /**
     * 处理用于 PDF 文件的 PdfItextUtil 插入文字与图片，CreatePdfByNote() 用
     *
     * @param pdfItextUtil PdfItextUtil
     * @param NoteContent  String
     * @throws Exception
     */
    private static void HandlePdfiTexForNoteContent(PdfItextUtil pdfItextUtil, String NoteContent) throws Exception {

        List<String> textList = StringUtils.cutStringByImgTag(NoteContent);

        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);
            if (text.contains("<img") && text.contains("src=")) {
                String imagePath = StringUtils.getImgSrc(text);

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, opt);

                Size newSize = HandleImgSize(new Size(opt.outWidth, opt.outHeight));
                pdfItextUtil.addImageToPdfCenterH(imagePath, newSize.getWidth(), newSize.getHeight());
            }
            else {
                pdfItextUtil.addTextToPdf(text);
            }
        }
    }
}
