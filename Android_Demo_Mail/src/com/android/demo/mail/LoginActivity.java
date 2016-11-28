
package com.android.demo.mail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.demo.MailApplication;
import com.android.demo.mail.utils.ConnUtil;
import com.example.android_demo_mail.R;
import com.max.toolbox.utils.PreferencesUtil;

import java.lang.ref.WeakReference;

import javax.mail.Store;

public class LoginActivity extends Activity {

    private EditText et_un, et_pw;
    private Button btn_login;
    private CheckBox cb_saveuser, cb_autologin;
    private Context context;
    private Handler handler;
    
    private static final String POP3HOST = "pop3host";
    private static final String SMTPHOST = "smtphost";
    private static final String SAVEUSER = "saveuser";
    private static final String AUTOLOGIN = "autologin";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        handler = new MyHandler(this);
        if (PreferencesUtil.getSharedBooleanData(context, AUTOLOGIN)) {
            login();
        } else {
            setContentView(R.layout.login);
            initView();
            setListener();
            if (PreferencesUtil.getSharedBooleanData(context, SAVEUSER)) {
                cb_saveuser.setChecked(PreferencesUtil.getSharedBooleanData(context, SAVEUSER));
                et_un.setText(PreferencesUtil.getSharedStringData(context, USERNAME));
                et_pw.setText(PreferencesUtil.getSharedStringData(context, PASSWORD));
            }
        }
    }

    private void setListener() {
        cb_autologin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cb_saveuser.setEnabled(!isChecked);
                if (isChecked) {
                    cb_saveuser.setChecked(isChecked);
                }
            }
        });

        btn_login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_un.getText()) || TextUtils.isEmpty(et_pw.getText())) {
                    Toast.makeText(getApplicationContext(), "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                PreferencesUtil.setSharedBooleanData(context, SAVEUSER, cb_saveuser.isChecked());
                PreferencesUtil.setSharedBooleanData(context, AUTOLOGIN, cb_autologin.isChecked());
                PreferencesUtil.setSharedStringData(context, USERNAME, et_un.getText().toString());
                PreferencesUtil.setSharedStringData(context, PASSWORD, et_pw.getText().toString());
                PreferencesUtil.setSharedStringData(context, POP3HOST, ConnUtil.getPOP3Host(et_un.getText().toString()));
                PreferencesUtil.setSharedStringData(context, SMTPHOST, ConnUtil.getSMTPHost(et_un.getText().toString()));
                login();
            }
        });
    }

    private void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Store store = ConnUtil.login(PreferencesUtil.getSharedStringData(context, POP3HOST), PreferencesUtil.getSharedStringData(context, USERNAME),
                        PreferencesUtil.getSharedStringData(context, PASSWORD));
                if (store != null) {
                    ((MailApplication) getApplication()).setStore(store);
                    startActivity(new Intent(context, HomeActivity.class));
                    finish();
                } else {
                    handler.obtainMessage(0).sendToTarget();
                }
            }
        }).start();
    }

    private void initView() {
        et_un = (EditText) findViewById(R.id.et_un);
        et_pw = (EditText) findViewById(R.id.et_pw);
        btn_login = (Button) findViewById(R.id.btn_login);
        cb_saveuser = (CheckBox) findViewById(R.id.cb_savepw);
        cb_autologin = (CheckBox) findViewById(R.id.cb_autologin);
    }

    private static class MyHandler extends Handler {

        private WeakReference<LoginActivity> wrActivity;

        public MyHandler(LoginActivity activity) {
            this.wrActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final LoginActivity activity = wrActivity.get();
            switch (msg.what) {
                case 0:
                    Toast.makeText(activity.getApplicationContext(), "用户名或密码不正确", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    };

}
