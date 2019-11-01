package com.baibuti.biji.util.imgDocUtil;

import android.util.Size;

import com.baibuti.biji.util.stringUtil.StringUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.xslf.usermodel.TextAlign;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Using POI & IText By AoiHosizora
 */
public class DocumentUtil {

    // private static int A4_WIDTH = 2480;
    // private static int A4_HEIGHT = 3508;
    private static double A4_PX_RATE = 215.0 / 700;
    private static int A4_MAX_WIDTH = (int)(1775 * A4_PX_RATE);
    private static int A4_MAX_HEIGHT = (int)(5628 * A4_PX_RATE);

    /**
     * 修改图片尺寸至适合尺寸
     */
    private static Size handleImgSize(Size motoSize) {
        int width = motoSize.getWidth();
        int height = motoSize.getHeight();

        if (width > A4_MAX_WIDTH) {
            height = (int) (height * (A4_MAX_WIDTH / (double) width));
            width = A4_MAX_WIDTH;
        }

        if (height > A4_MAX_HEIGHT) {
            width = (int) (width * (A4_MAX_HEIGHT / (double) height));
            height = A4_MAX_HEIGHT;
        }

        return new Size(width, height);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建 Docx 文件，插入笔记标题与内容
     * @return 是否保存成功
     */
    public static boolean CreateDocxByNote(String path, String title, String content, boolean isReWrite) {
        File docFile = new File(path);
        if (docFile.exists() && isReWrite && !docFile.delete())
            return false;

        // Open
        XWPFDocument docx = new XWPFDocument();

        // Title
        XWPFParagraph para = docx.createParagraph();
        XWPFRun run = para.createRun();
        run.setUnderline(UnderlinePatterns.SINGLE);
        run.setTextPosition(TextAlign.CENTER.ordinal());
        run.setText(title);

        // Content
        insertContentToXWPFDoc(docx, content);

        // Save
        try {
            OutputStream os = new FileOutputStream(path);
            docx.write(os);
            os.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * XWPFRun 插入文字与图片
     * CreateDocxByNote() 用
     *
     * @param docx    XWPFDocument
     * @param content 笔记内容
     */
    private static void insertContentToXWPFDoc(XWPFDocument docx, String content) {
        XWPFRun run;

        ////////////////////////////////////////
        List<String> textList = StringUtil.cutStringByImgTag(content);

        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);

            if (text.contains("<img") && text.contains("src=")) {
                // Image

                String imagePath = StringUtil.getImgSrc(text);

                if (imagePath.startsWith("http")) {
                    // TODO 尚未处理网络图片
                    XWPFParagraph para = docx.createParagraph();
                    run = para.createRun();
                    // run.setText("!!!!!: " + text + " :!!!!!");
                    run.setText(imagePath);
                }
                else {
                    // 本地图片
                    try {
                        Size newSize = handleImgSize(ImageUtil.getImgSize(imagePath));
                        insertImageToDocx(docx, imagePath, newSize.getWidth(), newSize.getHeight());
                    }
                    catch (Exception ex) {
                        XWPFParagraph para = docx.createParagraph();
                        run = para.createRun();
                        run.setText(text);
                    }
                }
            }
            else {
                // Text
                XWPFParagraph para = docx.createParagraph();
                run = para.createRun();
                run.setText(text);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建 Pdf 文件，插入笔记标题与内容
     */
    public static boolean CreatePdfByNote(String path, String title, String content, boolean isReWrite) {
        File pdfFile = new File(path);
        if (pdfFile.exists() && isReWrite && !pdfFile.delete())
            return false;

        // Open
        Document pdfDoc = new Document(PageSize.A4,50,50,30,30);
        try {
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(path));
            pdfDoc.open();
        }
        catch (FileNotFoundException | DocumentException ex) {
            ex.printStackTrace();
            return false;
        }

        // Title
        Paragraph elements = new Paragraph(title, getChineseFont(16));
        elements.setAlignment(Element.ALIGN_CENTER);
        try {
            pdfDoc.add(elements);
        }
        catch (DocumentException ex) {
            ex.printStackTrace();
        }

        // Content
        insertContentToPdfDoc(pdfDoc, content);

        // Save
        if (pdfDoc.isOpen())
            pdfDoc.close();

        return true;
    }

    /**
     * Document 插入文字与图片
     * CreatePdfByNote() 用
     * @param content Img + Text
     */
    private static void insertContentToPdfDoc(Document pdfDoc, String content) {

        List<String> textList = StringUtil.cutStringByImgTag(content);

        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);

            if (text.contains("<img") && text.contains("src=")) {
                // Image

                String imagePath = StringUtil.getImgSrc(text);

                if (imagePath.startsWith("http")) {
                    // TODO 尚未处理网络图片
                    try {
                        // Paragraph elements = new Paragraph("!!!!!: " + text + " :!!!!!", getChineseFont());
                        Paragraph elements = new Paragraph(content, getChineseFont());
                        elements.setAlignment(Element.ALIGN_BASELINE);
                        pdfDoc.add(elements);
                    }
                    catch (DocumentException ex) {
                        insertTextToPdfDoc(pdfDoc, text);
                    }
                }
                else {
                    try {
                        Size newSize = handleImgSize(ImageUtil.getImgSize(imagePath));

                        try {
                            Image img = Image.getInstance(imagePath);
                            img.setAlignment(Element.ALIGN_CENTER);
                            img.scaleToFit(newSize.getWidth(), newSize.getHeight());
                            pdfDoc.add(img);
                        }
                        catch (DocumentException ex) {
                            insertTextToPdfDoc(pdfDoc, text);
                        }
                    }
                    catch (Exception ex) {
                        insertTextToPdfDoc(pdfDoc, text);
                    }
                }
            }
            else {
                // Text
                insertTextToPdfDoc(pdfDoc, text);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Document 插入文字
     * @param text Text
     */
    private static void insertTextToPdfDoc(Document pdfDoc, String text) {
        Paragraph elements = new Paragraph(text, getChineseFont());
        elements.setAlignment(Element.ALIGN_BASELINE);
        try {
            pdfDoc.add(elements);
        }
        catch (DocumentException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * XWPFDocument 插入图片
     *
     * https://blog.csdn.net/zhyh1986/article/details/8717585
     * 使用：
     *      String picId = document.addPictureData(new FileInputStream("E:/20130325133325.png"), XWPFDocument.PICTURE_TYPE_PNG);
     * 		document.createPicture(picId, document.getNextPicNameNumber(XWPFDocument.PICTURE_TYPE_PNG), 200, 150);
     */
    private static void insertImageToDocx(XWPFDocument docx, String imagePath, int width, int height) throws Exception {
        final int EMU = 9525;
        width *= EMU;
        height *= EMU;

        FileInputStream fis = new FileInputStream(imagePath);
        String ext = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();

        int imgType = getImageXWPFDocType(ext);

        String blipId = docx.addPictureData(fis, imgType);
        int nextPicId = docx.getNextPicNameNumber(imgType);

        CTInline inline = docx.createParagraph().createRun().getCTR().addNewDrawing().addNewInline();

        String picXml = "" +
            "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
            "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
            "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
            "         <pic:nvPicPr>" +
            "            <pic:cNvPr id=\"" + nextPicId + "\" docName=\"Generated\"/>" +
            "            <pic:cNvPicPr/>" +
            "         </pic:nvPicPr>" +
            "         <pic:blipFill>" +
            "            <a:blip r:embed=\"" + blipId + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
            "            <a:stretch>" +
            "               <a:fillRect/>" +
            "            </a:stretch>" +
            "         </pic:blipFill>" +
            "         <pic:spPr>" +
            "            <a:xfrm>" +
            "               <a:off x=\"0\" y=\"0\"/>" +
            "               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>" +
            "            </a:xfrm>" +
            "            <a:prstGeom prst=\"rect\">" +
            "               <a:avLst/>" +
            "            </a:prstGeom>" +
            "         </pic:spPr>" +
            "      </pic:pic>" +
            "   </a:graphicData>" +
            "</a:graphic>";

        XmlToken xmlToken = null;
        try {
            xmlToken = XmlToken.Factory.parse(picXml);
        } catch (XmlException xe) {
            xe.printStackTrace();
        }
        inline.set(xmlToken);

        inline.setDistT(0);
        inline.setDistB(0);
        inline.setDistL(0);
        inline.setDistR(0);

        CTPositiveSize2D extent = inline.addNewExtent();
        extent.setCx(width);
        extent.setCy(height);

        CTNonVisualDrawingProps docPr = inline.addNewDocPr();
        docPr.setId(nextPicId);
        docPr.setName("Picture " + nextPicId);
        docPr.setDescr("Generated");
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取图片类型
     * insertImageToDocx 用
     * @param ext 文件后缀名
     * @return XWPFDocument.PICTURE_TYPE_PICT
     */
    private static int getImageXWPFDocType(String ext) {
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
     * 获得默认字体 + 默认字号 (12)
     */
    private static Font getChineseFont() {
        return getChineseFont(12);
    }

    /**
     * 获得默认字体
     *      STSong-Light : Adobe的字体
     *      UniGB-UCS2-H : pdf 字体
     * @param size (default for 12)
     */
    private static Font getChineseFont(int size) {
        Font fontChinese = new Font();
        try {
            BaseFont bf = BaseFont.createFont("STSong-Light" ,"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, size, Font.NORMAL);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }
}
