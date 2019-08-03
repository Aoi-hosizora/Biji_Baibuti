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

public class RegisterFragment extends Fragment implements View.OnClickListener {

    public View view;
    private Button m_RegisterButton;
    private Button m_ClearButton;
    private Button m_ToLoginButton;
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

    }

    private void ClearButton_Click() {
        m_LoginEditText.setText("");
        m_PasswordEditText.setText("");
    }
}
