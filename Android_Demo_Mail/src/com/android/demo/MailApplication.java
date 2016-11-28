
package com.android.demo;

import android.app.Application;

import java.io.InputStream;
import java.util.ArrayList;

import javax.mail.Store;

public class MailApplication extends Application {

    private Store store;
    private ArrayList<InputStream> attachmentsInputStreams;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public ArrayList<InputStream> getAttachmentsInputStreams() {
        return attachmentsInputStreams;
    }

    public void setAttachmentsInputStreams(ArrayList<InputStream> attachmentsInputStreams) {
        this.attachmentsInputStreams = attachmentsInputStreams;
    }

    

}
