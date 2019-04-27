package com.baibuti.biji.util;


import com.baibuti.biji.Data.Note;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Using POI By AoiHosizora
 */
public class ToDocUtil {

    /**
     * 创建 Docx 文件，插入笔记标题与内容
     * @param Path Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "..."
     * @param NoteTitle
     * @param NoteContent
     * @return
     * @throws IOException
     */
    public static boolean CreateDocxByNote(String Path, String NoteTitle, String NoteContent) throws Exception {
        File docFile = new File(Path);
        if (docFile.exists())
            return false;

        XWPFDocument docx = new XWPFDocument();
        XWPFParagraph para = docx.createParagraph();

        XWPFRun run = para.createRun();

        // 处理标题
        run.setUnderline(UnderlinePatterns.SINGLE);
        run.setText(NoteTitle);

        // 处理内容
        para = docx.createParagraph();
        HandleDocxRunForNoteContent(para, NoteContent);

        OutputStream os = new FileOutputStream(Path);
        docx.write(os);

        try {
            os.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * 处理用于 Docx 文件的 XWPFRun 插入文字与图片
     * @param para XWPFParagraph
     * @param NoteContent 笔记内容
     */
    private static void HandleDocxRunForNoteContent(XWPFParagraph para, String NoteContent) {
        XWPFRun run = para.createRun();
        run.setText(NoteContent);
    }

    /**
     * 创建 Doc 文件，插入笔记标题与内容
     * @param Path Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "..."
     * @param NoteTitle
     * @param NoteContent
     * @return
     * @throws IOException
     */
    public static boolean CreateDocByNote(String Path, String NoteTitle, String NoteContent) throws IOException {
        File docFile = new File(Path);
        if (docFile.exists())
            return false;
        docFile.createNewFile();

        InputStream is = new FileInputStream(docFile);
        HWPFDocument doc = new HWPFDocument(is);

        //获取Range
        Range range = doc.getRange();
        // 处理标题
        range.insertAfter(NoteTitle);
        // 处理内容
        HandleDocRangeForNoteContent(range, NoteContent);

        OutputStream os = new FileOutputStream(Path);
        doc.write(os);

        try {
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 处理用于 Doc 文件的 Range 插入文字与图片
     * @param range Range
     * @param NoteContent 笔记内容
     */
    private static void HandleDocRangeForNoteContent(Range range, String NoteContent) {
        range.insertAfter(NoteContent);
    }
}
