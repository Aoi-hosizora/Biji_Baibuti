package com.baibuti.biji.util.otherUtil;

import android.graphics.Color;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateColorUtil {

    /**
     * Date -> String
     * @param date nullable for now
     */
    public static String Date2Str(Date date) {
        if (date == null)
            date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(date);
    }

    /**
     * String -> Date
     * @param dateStr yyyy-MM-dd HH:mm:ss
     */
    public static Date Str2Date(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            return sdf.parse(dateStr);
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 十进制color -> 十六进制
     */
    public static String ColorInt_HexEncoding(int color) {
        String R, G, B;
        StringBuilder sb = new StringBuilder();

        R = Integer.toHexString(Color.red(color));
        G = Integer.toHexString(Color.green(color));
        B = Integer.toHexString(Color.blue(color));

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        sb.append("#");
        sb.append(R);
        sb.append(G);
        sb.append(B);

        return sb.toString();
    }

    /**
     * 十六进制color -> 十进制
     */
    public static int ColorHex_IntEncoding(String color) {
        return Color.parseColor(color);
    }
}
