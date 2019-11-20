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
     * Toast: message
     */
    default void showToast(Context context,
                           CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    ////////////////////////////////

    /**
     * AlertDialog: title + message
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
     * AlertDialog: title + message + Pos
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
     * AlertDialog: title + message + Pos + Neg
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
     * AlertDialog: title + message + Pos + Neg + Neu
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

    ////////////////////////////////

    /**
     * AlertDialog: title + View + Pos
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
     * AlertDialog: title + View + Pos + Neg
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

    ////////////////////////////////

    /**
     * AlertDialog: title + list + Pos
     */
    default void showAlert(Context context,
                           CharSequence title,
                           CharSequence[] list, DialogInterface.OnClickListener listener,
                           CharSequence posText, DialogInterface.OnClickListener posListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(list, listener)
            .setPositiveButton(posText, posListener)
            .show();
    }

    /**
     * AlertDialog: title + list + adapter + pos
     */
    default void showAlert(Context context,
                           CharSequence title,
                           ListAdapter adapter, DialogInterface.OnClickListener listener,
                           CharSequence posText, DialogInterface.OnClickListener posListener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(adapter, 0, listener)
            .setPositiveButton(posText, posListener)
            .show();
    }

    ////////////////////////////////

    /**
     * showInputDialog
     */
    interface OnInputDialogClickListener {
        void onClick(DialogInterface dialog, int which, String content);
    }

    /**
     * AlertDialog: title + EditText + Pos + Neg
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

    ////////////////////////////////

    /**
     * ProgressDialog: message + cancelable + onCancelListener
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
     * ProgressDialog: title + message + cancelable + onCancelListener
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

    ////////////////////////////////

    /**
     * Snackbar: message
     */
    default void showSnackBar(View view,
                              CharSequence message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Snackbar: message + action
     */
    default void showSnackBar(View view,
                              CharSequence message,
                              CharSequence action, View.OnClickListener onClickListener) {
        Snackbar
            .make(view, message, Snackbar.LENGTH_SHORT)
            .setAction(action, onClickListener)
            .show();
    }

    ////////////////////////////////

    /**
     * Intent.ACTION_VIEW: browser
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
