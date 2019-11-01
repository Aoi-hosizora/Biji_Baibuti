package com.baibuti.biji.ui.fragment;

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

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.AuthService;
import com.baibuti.biji.R;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.ui.activity.AuthActivity;
import com.baibuti.biji.ui.IContextHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterFragment extends Fragment implements IContextHelper {

    public View view;

    @BindView(R.id.id_RegisterFrag_UsernameLayout)  private TextInputLayout m_RegisterLayout;
    @BindView(R.id.id_RegisterFrag_UsernameText)    private TextInputEditText m_RegisterEditText;
    @BindView(R.id.id_RegisterFrag_PasswordLayout)  private TextInputLayout m_PasswordLayout;
    @BindView(R.id.id_RegisterFrag_PasswordText)    private TextInputEditText m_PasswordEditText;

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

    /**
     * 初始化界面，无
     */
    private void initView() { }

    /**
     * 注册按钮
     */
    @OnClick(R.id.id_RegisterFrag_RegisterButton)
    private void RegisterButton_Clicked() {
        String username = m_RegisterEditText.getText().toString();
        String password = m_PasswordEditText.getText().toString();

        if (username.length() < 5 || username.length() >= 30) {
            m_RegisterLayout.setError("用户名长度应在 5-30 之间");
            return;
        }

        if (password.length() < 8 || password.length() >= 20) {
            m_PasswordLayout.setError("密码长度应在 8-20 之间");
            return;
        }

        ProgressDialog progressDialog =
            showProgress(getContext(),
                String.format(Locale.CHINA, "用户 \"%s\" 注册中，请稍后...", username),
                true, null);

        try {
            progressDialog.dismiss();
            AuthRespDTO auth = AuthService.register(username, password);
            showToast(getContext(), String.format(Locale.CHINA, "用户 \"%s\" 注册成功，请登录。", auth.getUsername()));
        } catch (ServerException ex) {
            progressDialog.dismiss();
            showAlert(getContext(), "注册失败", ex.getMessage());
        }
    }

    /**
     * 清除按钮
     */
    @OnClick(R.id.id_RegisterFrag_ClearButton)
    private void ClearButton_Clicked() {
        m_RegisterEditText.setText("");
        m_PasswordEditText.setText("");
    }

    /**
     * 去登录按钮
     */
    @OnClick(R.id.id_RegisterFrag_ToLoginButton)
    private void ToLoginButton_Clicked() {
        AuthActivity activity = (AuthActivity) getActivity();
        if (activity != null)
            activity.openLogin();
    }
}
