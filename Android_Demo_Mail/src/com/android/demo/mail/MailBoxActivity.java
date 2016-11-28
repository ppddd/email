
package com.android.demo.mail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.demo.MailApplication;
import com.android.demo.mail.entries.Email;
import com.android.demo.mail.utils.MailHelper;
import com.android.demo.mail.utils.MailReceiver;
import com.example.android_demo_mail.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class MailBoxActivity extends Activity {

    private ArrayList<Email> mailslist = new ArrayList<Email>();
    private ArrayList<ArrayList<InputStream>> attachmentsInputStreamsList = new ArrayList<ArrayList<InputStream>>();
    private Context context;
    private String type;
    private MyAdapter myAdapter;
    private ListView lv_box;
    private Handler handler;
    private List<MailReceiver> mailReceivers;
    private ProgressBar pb_box;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        type = getIntent().getStringExtra("TYPE");
        setContentView(R.layout.mailbox);

        initView();

        handler = new MyHandler(this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mailReceivers = MailHelper.getInstance(context).getAllMail(type);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                getMailsList(mailReceivers);
                handler.obtainMessage(1).sendToTarget();
            }
        }).start();

    }

    private void initView() {
        pb_box = (ProgressBar) findViewById(R.id.pb_box);
        lv_box = (ListView) findViewById(R.id.lv_box);
        myAdapter = new MyAdapter();
        lv_box.setAdapter(myAdapter);
    }

    private void getMailsList(List<MailReceiver> mails) {
        for (MailReceiver mailReceiver : mails) {
            Email email = new Email();
            try {
                email.setMessageID(mailReceiver.getMessageID());
                email.setFrom(mailReceiver.getFrom());
                email.setTo(mailReceiver.getMailAddress("TO"));
                email.setCc(mailReceiver.getMailAddress("CC"));
                email.setBcc(mailReceiver.getMailAddress("BCC"));
                email.setSubject(mailReceiver.getSubject());
                email.setSentdata(mailReceiver.getSentData());
                email.setContent(mailReceiver.getMailContent());
                email.setReplysign(mailReceiver.getReplySign());
                email.setHtml(mailReceiver.isHtml());
                email.setNews(mailReceiver.isNew());
                email.setAttachments(mailReceiver.getAttachments());
                email.setCharset(mailReceiver.getCharset());
                attachmentsInputStreamsList.add(0,mailReceiver.getAttachmentsInputStreams());
                mailslist.add(0, email);
                handler.obtainMessage(0).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mailslist.size();
        }

        @Override
        public Object getItem(int position) {
            return mailslist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mailbox_item, null);
            TextView tv_from = (TextView) convertView.findViewById(R.id.tv_from);
            tv_from.setText(mailslist.get(position).getFrom());
            TextView tv_sentdate = (TextView) convertView.findViewById(R.id.tv_sentdate);
            tv_sentdate.setText(mailslist.get(position).getSentdata());
            if (mailslist.get(position).isNews()) {
                TextView tv_new = (TextView) convertView.findViewById(R.id.tv_new);
                tv_new.setVisibility(View.VISIBLE);
            }
            TextView tv_subject = (TextView) convertView.findViewById(R.id.tv_subject);
            tv_subject.setText(mailslist.get(position).getSubject());
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((MailApplication)getApplication()).setAttachmentsInputStreams(attachmentsInputStreamsList.get(position));
                    final Intent intent = new Intent(context, MailContentActivity.class).putExtra("EMAIL", mailslist.get(position));
                    startActivity(intent);
                }
            });
            return convertView;
        }

    }

    private static class MyHandler extends Handler {

        private WeakReference<MailBoxActivity> wrActivity;

        public MyHandler(MailBoxActivity activity) {
            this.wrActivity = new WeakReference<MailBoxActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final MailBoxActivity activity = wrActivity.get();
            switch (msg.what) {
                case 0:
                    activity.myAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    activity.pb_box.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        };
    };

}
