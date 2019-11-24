package com.baibuti.biji.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.baibuti.biji.R;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.auth.AuthService;
import com.baibuti.biji.common.auth.dto.AuthRespDTO;
import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.ui.IContextHelper;

import rx_activity_result2.RxActivityResult;

public class LauncherActivity extends AppCompatActivity implements IContextHelper {

    private static final int MIN_WAIT_TIME = 500;  // 0.5s

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        String token = AuthManager.getInstance().getSpToken(this);
        // Log.i("", "onCreate: " + token);
        if (token.isEmpty())
            continueToActivity();
        else {
            AuthManager.getInstance().setToken(token);

            // Try Login
            ProgressHandler.process(this, "登录中...", false,
                AuthService.currentAuth(), new InteractInterface<AuthRespDTO>() {
                    @Override
                    public void onSuccess(AuthRespDTO data) {
                        AuthManager.getInstance().login(data.getUsername(), token);
                        continueToActivity();
                    }

                    @Override
                    public void onError(String message) {
                        showToast(LauncherActivity.this, message);
                        AuthManager.getInstance().setSpToken(LauncherActivity.this, "");
                        continueToActivity();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        showToast(LauncherActivity.this, "网络错误：" + throwable.getMessage());
                        continueToActivity();
                    }
                }
            );
        }
    }

    /**
     * 跳转到主界面
     */
    private void continueToActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            RxActivityResult.on(LauncherActivity.this).startIntent(intent);
            new Handler().postDelayed(this::finish, MIN_WAIT_TIME);
        }, MIN_WAIT_TIME);
    }
}
