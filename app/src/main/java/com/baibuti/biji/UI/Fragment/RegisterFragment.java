package com.baibuti.biji.UI.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baibuti.biji.Net.Modules.Auth.AuthUtil;
import com.baibuti.biji.Net.Models.RespObj.AuthStatus;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Activity.RegLogActivity;

import java.util.Locale;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    public View view;
    private Button m_RegisterButton;
    private Button m_ClearButton;
    private Button m_ToLoginButton;
    private TextInputLayout m_LoginLayout;
    private TextInputEditText m_LoginEditText;
    private TextInputLayout m_PasswordLayout;
    private TextInputEditText m_PasswordEditText;

    private ProgressDialog m_reging;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_register, container, false);

            initView();
        }

        return view;
    }

    private void initView() {
        m_RegisterButton = view.findViewById(R.id.id_RegisterFrag_RegisterButton);
        m_ToLoginButton = view.findViewById(R.id.id_RegisterFrag_ToLoginButton);
        m_ClearButton = view.findViewById(R.id.id_RegisterFrag_ClearButton);

        m_LoginLayout = view.findViewById(R.id.id_RegisterFrag_LoginLayout);
        m_LoginEditText = view.findViewById(R.id.id_RegisterFrag_LoginText);
        m_PasswordLayout = view.findViewById(R.id.id_RegisterFrag_PasswordLayout);
        m_PasswordEditText = view.findViewById(R.id.id_RegisterFrag_PasswordText);

        m_RegisterButton.setOnClickListener(this);
        m_ToLoginButton.setOnClickListener(this);
        m_ClearButton.setOnClickListener(this);

        m_reging = new ProgressDialog(getContext());
        m_reging.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_RegisterFrag_RegisterButton:
                RegisterButton_Click();
            break;
            case R.id.id_RegisterFrag_ClearButton:
                ClearButton_Click();
                break;
            case R.id.id_RegisterFrag_ToLoginButton:
                ((RegLogActivity) getActivity()).openLogin();
            break;
        }
    }

    private void RegisterButton_Click() {

        String username = m_LoginEditText.getText().toString();
        String password = m_PasswordEditText.getText().toString();

        m_reging.setMessage(String.format(Locale.CHINA, "用户 \"%s\" 注册中，请稍后...", username));
        m_reging.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                AuthStatus status = AuthUtil.register(username, password);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_reging.cancel();
                        if (status.isSuccess())
                            showAlert("注册成功", String.format(Locale.CHINA, "用户 \"%s\" 注册成功，请登录。", status.getUsername()));
                        else
                            showAlert("注册失败", status.getErrorMsg());
                    }
                });

            }
        }).start();
    }

    private void ClearButton_Click() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .create().show();
    }
}
