package com.baibuti.biji.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

public interface IContextHelper {

    /**
     * Toast: 信息
     */
    default void showToast(Context context,
                           CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * AlertDialog: 标题 + 信息
     */
    default void showAlert(Context context,
                           CharSequence title, CharSequence message) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show();
    }

    /**
     * AlertDialog: 标题 + 信息 + Pos
     */
    default void showAlert(Context context,
                           CharSequence title, CharSequence message,
                           CharSequence posText, DialogInterface.OnClickListener posListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posText, posListener)
            .show();
    }

    /**
     * AlertDialog: 标题 + 信息 + Pos + Neg
     */
    default void showAlert(Context context,
                           CharSequence title, CharSequence message,
                           CharSequence posText, DialogInterface.OnClickListener posListener,
                           CharSequence negText, DialogInterface.OnClickListener negListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .show();
    }

    /**
     * AlertDialog: 标题 + 信息 + Pos + Neg + Neu
     */
    default void showAlert(Context context,
                           CharSequence title, CharSequence message,
                           CharSequence posText, DialogInterface.OnClickListener posListener,
                           CharSequence negText, DialogInterface.OnClickListener negListener,
                           CharSequence neuText, DialogInterface.OnClickListener neuListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .setNeutralButton(neuText, neuListener)
            .show();
    }

    /**
     * showInputDialog 提交文本
     */
    interface OnInputDialogClickListener {
        void onClick(DialogInterface dialog, int which, String content);
    }

    /**
     * AlertDialog: 标题 + View + Pos
     */
    default void showAlert(Context context,
                           CharSequence title, View view,
                           CharSequence posText, DialogInterface.OnClickListener posListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(posText, posListener)
            .create().show();
    }

    /**
     * AlertDialog: 标题 + View + Pos + Neg
     */
    default void showAlert(Context context,
                           CharSequence title, View view,
                           CharSequence posText, DialogInterface.OnClickListener posListener,
                           CharSequence negText, DialogInterface.OnClickListener negListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .create().show();
    }

    /**
     * AlertDialog: 标题 + EditText + Pos + Neg
     */
    default void showInputDialog(Context context,
                                 CharSequence title, CharSequence text, CharSequence hint, int maxLines,
                                 CharSequence posText, OnInputDialogClickListener posListener,
                                 CharSequence negText, DialogInterface.OnClickListener negListener) {
        EditText edt = new EditText(context);
        edt.setSingleLine(true);
        edt.setMaxLines(maxLines);
        edt.setHorizontallyScrolling(true);

        edt.setHint(hint);
        edt.setText(text);

        showAlert(context,
            title, edt,
            posText, (d, w) -> posListener.onClick(d, w, edt.getText().toString()),
            negText, negListener
        );
    }

    /**
     * AlertDialog: 标题 + 列表
     */
    default void showAlert(Context context,
                           CharSequence title,
                           CharSequence[] list, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(list, listener)
            .show();
    }

    /**
     * AlertDialog: 标题 + 列表 + 适配器
     */
    default void showAlert(Context context,
                           CharSequence title,
                           ListAdapter adapter, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(adapter, 0, listener)
            .show();
    }

    /**
     * ProgressDialog: 信息 + cancelable + onCancelListener
     */
    default ProgressDialog showProgress(Context context,
                                        CharSequence message,
                                        boolean cancelable, DialogInterface.OnCancelListener onCancelListener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.show();
        return progressDialog;
    }

    /**
     * ProgressDialog: 标题 + 信息 + cancelable + onCancelListener
     */
    default ProgressDialog showProgress(Context context,
                                        CharSequence title, CharSequence message,
                                        boolean cancelable, DialogInterface.OnCancelListener onCancelListener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.show();
        return progressDialog;
    }

    /**
     * Snackbar: 信息
     */
    default void showSnackBar(View view,
                              CharSequence message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Snackbar: 信息 + Action
     */
    default void showSnackbar(View view,
                              CharSequence message,
                              CharSequence action, View.OnClickListener onClickListener) {
        Snackbar
            .make(view, message, Snackbar.LENGTH_SHORT)
            .setAction(action, onClickListener)
            .show();
    }

    /**
     * Intent.ACTION_VIEW: 打开浏览器
     */
    default void showBrowser(Context context, String[] links) {
        for (String link : links) {
            try {
                Uri uri = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // @FunctionalInterface
    // interface ThrowingConsumer<T, E extends Exception> {
    //     void accept(T t) throws E;
    // }
    //
    // default <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
    //     return i -> {
    //         try {
    //             throwingConsumer.accept(i);
    //         } catch (Exception ex) {
    //             throw new RuntimeException(ex);
    //         }
    //     };
    // }
}
