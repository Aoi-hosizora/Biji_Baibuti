package com.baibuti.biji.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.auth.AuthService;
import com.baibuti.biji.R;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.ui.activity.AuthActivity;
import com.baibuti.biji.ui.IContextHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginFragment extends Fragment implements IContextHelper {

    public View view;

    @BindView(R.id.id_LoginFrag_UsernameLayout)     private TextInputLayout m_LoginLayout;
    @BindView(R.id.id_LoginFrag_UsernameText)       private TextInputEditText m_LoginEditText;
    @BindView(R.id.id_LoginFrag_PasswordLayout)     private TextInputLayout m_PasswordLayout;
    @BindView(R.id.id_LoginFrag_PasswordText)       private TextInputEditText m_PasswordEditText;

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

    /**
     * 初始化界面，标题
     */
    private void initView() {
        if (AuthManager.getInstance().isLogin())
            m_LoginEditText.setText(AuthManager.getInstance().getUserName());
    }

    /**
     * 登录按钮
     */
    @OnClick(R.id.id_LoginFrag_LoginButton)
    private void LoginButton_Clicked() {
        String username = m_LoginEditText.getText().toString();
        String password = m_PasswordEditText.getText().toString();

        if (username.length() == 0) {
            m_LoginLayout.setError("用户名为空");
            return;
        }

        if (password.length() == 0) {
            m_PasswordLayout.setError("密码为空");
            return;
        }

        ProgressDialog progressDialog =
            showProgress(getContext(),
                String.format(Locale.CHINA, "用户 \"%s\" 登录中，请稍后...", username),
                true, null);

        try {
            AuthRespDTO auth = AuthService.login(username, password);

            progressDialog.dismiss();
            showToast(getContext(), String.format(Locale.CHINA, "用户 \"%s\" 登录成功", auth.getUsername()));
            AuthManager.getInstance().login(auth.getUsername(), auth.getToken());

            AuthActivity activity = (AuthActivity) getActivity();
            if (activity != null) {
                activity.setResult(Activity.RESULT_OK, new Intent());
                activity.finish();
            }
        } catch (ServerException ex) {
            progressDialog.dismiss();
            showAlert(getContext(), "登录失败", ex.getMessage());
        }
    }

    /**
     * 清空按钮
     */
    @OnClick(R.id.id_LoginFrag_ClearButton)
    private void ClearButton_Clicked() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }

    /**
     * 去注册按钮
     */
    @OnClick(R.id.id_LoginFrag_ToRegisterButton)
    private void ToRegisterButton_Clicked() {
        AuthActivity activity = (AuthActivity) getActivity();
        if (activity != null)
            activity.openRegister();
    }
}
