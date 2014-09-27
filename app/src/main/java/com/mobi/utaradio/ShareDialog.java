package com.mobi.utaradio;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Cameron on 9/26/2014.
 */
public class ShareDialog extends Dialog {

    private TableLayout layout;
    private TextView tv;
    private List<ResolveInfo> info;

    public ShareDialog(Context context, List<ResolveInfo> info) {
        super(context);
        this.info = filterInfoList(
                getContext().getResources().getStringArray(R.array.share_applications), info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new TableLayout(getContext());

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        tv = new TextView(getContext());
        tv.setText("Hello");

        layout.setLayoutParams(tableParams);
        TableRow row = new TableRow(getContext());
        for(int i = 0; i < info.size(); i++) {
            if(i % 3 == 0) {
                layout.addView(row);
                row = new TableRow(getContext());
                row.setLayoutParams(rowParams);
            }
            ImageView image = new ImageView(getContext());
            image.setImageDrawable(info.get(i).loadIcon(getContext().getPackageManager()));
            row.addView(image);
        }

        setContentView(layout);
        this.setTitle("Select an app to share with");
    }

    private List<ResolveInfo> filterInfoList(String[] packageNames, List<ResolveInfo> info) {
        Iterator<ResolveInfo> i = info.iterator();

        while(i.hasNext()) {
            boolean inList = false;
            Log.e("Share", Boolean.toString(inList));
            for(int p = 0; p < packageNames.length; p++) {
                //Log.e("Share", packageNames[p]);
                if(i.next().activityInfo.packageName.toLowerCase().startsWith(packageNames[p])) {
                    //Log.e("Share", packageNames[p]);
                    inList = true;
                    break;
                }
                //Log.e("Share", Boolean.toString(inList));
                if(!inList) {
                    i.remove();
                }
            }
        }
        return info;
    }
}
