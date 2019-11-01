package com.baibuti.biji.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;
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
}
