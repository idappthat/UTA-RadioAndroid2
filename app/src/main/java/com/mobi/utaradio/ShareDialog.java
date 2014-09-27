package com.mobi.utaradio;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Cameron on 9/26/2014.
 */
public class ShareDialog extends Dialog {
    public ShareDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTitle("Select an app to share with ");
    }
}
