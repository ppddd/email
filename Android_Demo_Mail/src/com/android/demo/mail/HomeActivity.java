
package com.android.demo.mail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android_demo_mail.R;

public class HomeActivity extends Activity {

    private ListView lv_home;
    private String[][] boxs = {
            {
                    "收件箱", "发邮件"
            },
            {
                    "INBOX", "OUTBOX"
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mailhome);

        lv_home = (ListView) findViewById(R.id.lv_home);
        lv_home.setAdapter(new ArrayAdapter<String>(this, R.layout.mailhome_item, boxs[0]));
        lv_home.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (boxs[1][position].equals("INBOX")) {
                    startActivity(new Intent(HomeActivity.this, MailBoxActivity.class).putExtra("TYPE", boxs[1][position]));
                } else {
                    startActivity(new Intent(HomeActivity.this, EditMailActivity.class).putExtra("TYPE", 0));
                }
            }
        });
    }
}
