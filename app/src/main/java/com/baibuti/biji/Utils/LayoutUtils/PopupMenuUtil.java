package com.baibuti.biji.Utils.LayoutUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.baibuti.biji.R;

public class PopupMenuUtil {

    /**
     * 初始化弹出菜单
     * @param context
     * @param PopupMenu
     * @param LayoutResource
     * @return
     */
    public static LinearLayout initPopupMenu(Context context, Dialog PopupMenu, int LayoutResource) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(LayoutResource, null);

        // 初始化视图

        PopupMenu.setContentView(root);
        Window dialogWindow = PopupMenu.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) context.getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度

        dialogWindow.setAttributes(lp);

        return root;
    }
}
