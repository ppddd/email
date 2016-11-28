
package com.android.demo.mail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.demo.FileExplorerActivity;
import com.android.demo.mail.entries.Email;
import com.android.demo.mail.utils.MailSenter;
import com.example.android_demo_mail.R;
import com.max.toolbox.utils.PreferencesUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class EditMailActivity extends Activity {

    private static final String SMTPHOST = "smtphost";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private EditText et_addr, et_mailsubject, et_mailcontent;
    private Button btn_cancel, btn_sent, btn_addattachment;
    private ListView lv_mailattachment;
    private int type;
    private Email email;
    private Handler handler;
    private ArrayList<String> attachments;
    private MyAdapter adapter;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.mailedit);
        getExtra();
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1 && data != null) {
            String filepath = data.getStringExtra("FILEPATH");
            if (filepath.length() > 0) {
                lv_mailattachment.setVisibility(View.VISIBLE);
                attachments.add(filepath);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void init() {
        et_addr = (EditText) findViewById(R.id.et_addr);
        et_mailsubject = (EditText) findViewById(R.id.et_mailsubject);
        et_mailcontent = (EditText) findViewById(R.id.et_mailcontent);
        lv_mailattachment = (ListView) findViewById(R.id.lv_mailattachment);
        btn_addattachment = (Button) findViewById(R.id.btn_addattachment);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_sent = (Button) findViewById(R.id.btn_sent);

        btn_addattachment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(EditMailActivity.this, FileExplorerActivity.class), 1);
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EditMailActivity.this.finish();
            }
        });

        btn_sent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Email email = new Email();
                            email.setTo(et_addr.getText().toString());
                            email.setSubject(et_mailsubject.getText().toString());
                            email.setContent(et_mailcontent.getText().toString());
                            email.setAttachments(attachments);

                            MailSenter mailSenter = new MailSenter(handler, PreferencesUtil.getSharedStringData(context, SMTPHOST), PreferencesUtil.getSharedStringData(context, USERNAME),
                                    PreferencesUtil.getSharedStringData(context, PASSWORD));
                            handler.obtainMessage(0).sendToTarget();
                            mailSenter.send(email);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        handler = new MyHandler(this);
        attachments = new ArrayList<String>();
        lv_mailattachment.setAdapter(adapter = new MyAdapter());

        if (type == 1) {
            et_addr.setText(email.getFrom());
            et_mailsubject.setText("回复：" + email.getSubject());
        } else if (type == 2) {
            et_mailsubject.setText("转发：" + email.getSubject());
            et_mailcontent.setText(email.getContent());
        }

        et_addr.setText("40834701@qq.com");
        et_mailsubject.setText("xxxxx");
        et_mailcontent.setText("awsdasfadsfgsdfs");

    }

    private void getExtra() {
        type = getIntent().getIntExtra("TYPE", 0);
        email = (Email) getIntent().getSerializableExtra("EMAIL");
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(EditMailActivity.this);
            tv.setText(attachments.get(position).substring(attachments.get(position).lastIndexOf("/") + 1));
            tv.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    attachments.remove(position);
                    handler.obtainMessage(3).sendToTarget();
                }
            });
            return tv;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return attachments.get(position);
        }

        @Override
        public int getCount() {
            return attachments.size();
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<EditMailActivity> wrActivity;
        private ProgressDialog pd;

        public MyHandler(EditMailActivity activity) {
            this.wrActivity = new WeakReference<EditMailActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final EditMailActivity activity = wrActivity.get();
            switch (msg.what) {
                case 0:
                    pd = ProgressDialog.show(activity.context, "发送邮件","正在发送....", true, false);
                    break;
                case 1:
                    pd.dismiss();
                    Toast.makeText(activity.getApplicationContext(), "发送成功！", Toast.LENGTH_LONG).show();
                    activity.finish();
                    break;
                case 3:
                    activity.adapter.notifyDataSetChanged();
                    break;
                default:
                    Toast.makeText(activity.context, "发送出现错误！", Toast.LENGTH_LONG).show();
                    break;
            }
        };
    };

}
