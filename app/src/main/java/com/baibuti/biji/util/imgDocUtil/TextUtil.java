package com.baibuti.biji.util.imgDocUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

/**
 * 读取写入 文本文件
 */
public class TextUtil {

    /**
     * 从文件读取数据
     * @return null for error
     */
    @Nullable
    public static String readFromFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists())
                throw new FileNotFoundException();

            FileInputStream fis = new FileInputStream(filename);
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[1024];

            int len;
            while ((len = fis.read(buf)) > 0) {
                sb.append(new String(buf, 0, len));
            }

            fis.close();

            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 将数据保存进文件
     * @return false for error
     */
    public static boolean writeIntoFile(String filename, String content) {
        try {
            File file = new File(filename);
            if (file.exists() && !file.delete())
                throw new IOException();
            if (!file.exists() && !file.createNewFile())
                throw new IOException();

            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
