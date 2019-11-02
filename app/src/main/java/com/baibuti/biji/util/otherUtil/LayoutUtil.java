package com.baibuti.biji.util.otherUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

public class LayoutUtil {

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    /**
     * 初始化弹出菜单
     * Usage:
     *      m_PopupMenu = new Dialog(getContext(), R.style.BottomDialog);
     *      LinearLayout root = LayoutUtil.initPopupMenu(getContext(), m_PopupMenu, R.layout.xxx);
     *      root.findViewById(xxx).setOnClickListener(xxx);
     *
     * @param LayoutResource R.layout.xxx
     */
    public static LinearLayout initPopupMenu(Context context, Dialog PopupMenu, int LayoutResource) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(LayoutResource, null);

        // 初始化视图
        PopupMenu.setContentView(root);
        Window dialogWindow = PopupMenu.getWindow();
        if (dialogWindow == null)
            throw new NullPointerException();

        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = context.getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度

        dialogWindow.setAttributes(lp);

        return root;
    }

    /**
     * 为 NavigationView 通过 DisplayMetrics 设置宽度
     * @param activity 当前活动，防止 getActivity() 为 null
     * @param rate 占页面宽度的百分比
     */
    public static void setNavigationViewWidth(@Nullable Activity activity, @Nonnull NavigationView navigationView, double rate) {
        if (activity == null) return;

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = (int) (metrics.widthPixels * rate);
        navigationView.setLayoutParams(params);
    }
}
