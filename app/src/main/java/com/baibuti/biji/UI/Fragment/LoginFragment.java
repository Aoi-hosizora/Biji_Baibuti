package com.baibuti.biji.UI.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Auth.AuthUtil;
import com.baibuti.biji.Net.Models.RespObj.AuthStatus;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Activity.RegLogActivity;

import java.util.Locale;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public View view;
    private Button m_LoginButton;
    private Button m_ClearButton;
    private Button m_ToRegisterButton;
    private TextInputLayout m_LoginLayout;
    private TextInputEditText m_LoginEditText;
    private TextInputLayout m_PasswordLayout;
    private TextInputEditText m_PasswordEditText;

    private ProgressDialog m_logining;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_login, container, false);

            initView();
        }

        return view;
    }

    private void initView() {
        m_LoginButton = view.findViewById(R.id.id_LoginFrag_LoginButton);
        m_ToRegisterButton = view.findViewById(R.id.id_LoginFrag_ToRegisterButton);
        m_ClearButton = view.findViewById(R.id.id_LoginFrag_ClearButton);

        m_LoginLayout = view.findViewById(R.id.id_LoginFrag_LoginLayout);
        m_LoginEditText = view.findViewById(R.id.id_LoginFrag_LoginText);
        m_PasswordLayout = view.findViewById(R.id.id_LoginFrag_PasswordLayout);
        m_PasswordEditText = view.findViewById(R.id.id_LoginFrag_PasswordText);

        m_LoginButton.setOnClickListener(this);
        m_ToRegisterButton.setOnClickListener(this);
        m_ClearButton.setOnClickListener(this);

        m_logining = new ProgressDialog(getContext());
        m_logining.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_LoginFrag_LoginButton:
                LoginButton_Click();
            break;
            case R.id.id_LoginFrag_ClearButton:
                ClearButton_Click();
            break;
            case R.id.id_LoginFrag_ToRegisterButton:
                ((RegLogActivity) getActivity()).openRegister();
            break;
        }
    }

    private void LoginButton_Click() {

        String username = m_LoginEditText.getText().toString();
        String password = m_PasswordEditText.getText().toString();

        m_logining.setMessage(String.format(Locale.CHINA, "用户 \"%s\" 登录中，请稍后...", username));
        m_logining.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                AuthStatus status = AuthUtil.login(username, password);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_logining.cancel();
                        if (status.isSuccess()) {
                            Toast.makeText(getActivity(),
                                String.format(Locale.CHINA, "用户 \"%s\" 登录成功", status.getUsername()), Toast.LENGTH_SHORT).show();
                            AuthMgr.getInstance().setToken(status.getToken());
                            AuthMgr.getInstance().setUserName(status.getUsername());

                            Log.e("", "usr: " + AuthMgr.getInstance().getUserName() + ", token: " + AuthMgr.getInstance().getToken() );
                            // TODO finish
                        }
                        else
                            showErrorAlert(status.getErrorMsg());
                    }
                });

            }
        }).start();

    }

    private void ClearButton_Click() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }

    private void showErrorAlert(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("登录错误")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .create().show();
    }
}
