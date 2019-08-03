package com.baibuti.biji.UI.Fragment;

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

import com.baibuti.biji.R;
import com.baibuti.biji.UI.Activity.RegLogActivity;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public View view;
    private Button m_LoginButton;
    private Button m_ClearButton;
    private Button m_ToRegisterButton;
    private TextInputLayout m_LoginLayout;
    private TextInputEditText m_LoginEditText;
    private TextInputLayout m_PasswordLayout;
    private TextInputEditText m_PasswordEditText;

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
        m_LoginLayout.setError("Error");
        m_PasswordLayout.setError("Error");
    }

    private void ClearButton_Click() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }
}
