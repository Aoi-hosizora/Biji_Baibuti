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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.auth.AuthService;
import com.baibuti.biji.R;
import com.baibuti.biji.common.auth.dto.AuthRespDTO;
import com.baibuti.biji.ui.activity.AuthActivity;
import com.baibuti.biji.ui.IContextHelper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends Fragment implements IContextHelper {

    public View view;

    @BindView(R.id.loginFrag_layout_username)
    TextInputLayout m_LoginLayout;

    @BindView(R.id.loginFrag_edt_username)
    TextInputEditText m_LoginEditText;

    @BindView(R.id.loginFrag_layout_password)
    TextInputLayout m_PasswordLayout;

    @BindView(R.id.loginFrag_edt_password)
    TextInputEditText m_PasswordEditText;

    @BindView(R.id.loginFrag_spinner_ex)
    Spinner m_ExSpinner;

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
            ButterKnife.bind(this, view);

            initView();
        }

        return view;
    }

    /**
     * 初始化界面，标题
     */
    private void initView() {
        if (AuthManager.getInstance().isLogin())
            m_LoginEditText.setText(AuthManager.getInstance().getUsername());
        AuthActivity activity = (AuthActivity) getActivity();
        if (activity != null) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(activity, R.layout.layout_common_spinner, exTexts);
            m_ExSpinner.setAdapter(spinnerAdapter);
            m_ExSpinner.setSelection(Arrays.asList(exTexts).indexOf("30天"), true);
        }
    }

    private Map<String, Integer> exTextNumbers = new LinkedHashMap<String, Integer>() {{
        put("1天", 1);
        put("2天", 2);
        put("3天", 3);
        put("7天", 7);
        put("15天", 15);
        put("30天", 30);
        put("45天", 45);
        put("60天", 60);
    }};

    private String[] exTexts = exTextNumbers.keySet().toArray(new String[0]);

    /**
     * 登录按钮
     */
    @OnClick(R.id.loginFrag_btn_login)
    void LoginButton_Clicked() {
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

        int exp = exTextNumbers.get(exTexts[m_ExSpinner.getSelectedItemPosition()]);
        exp *= 24 * 3600;

        ProgressDialog progressDialog =
            showProgress(getContext(),
                String.format(Locale.CHINA, "用户 \"%s\" 登录中，请稍后...", username),
                true, null);

        try {
            AuthRespDTO auth = AuthService.login(username, password, exp);

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
    @OnClick(R.id.loginFrag_btn_clear)
    void ClearButton_Clicked() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }

    /**
     * 去注册按钮
     */
    @OnClick(R.id.loginFrag_btn_toRegister)
    void ToRegisterButton_Clicked() {
        AuthActivity activity = (AuthActivity) getActivity();
        if (activity != null)
            activity.openRegister();
    }
}
