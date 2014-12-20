package com.mobi.utaradio;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import com.parse.Parse;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize parse
        Parse.initialize(this, "5qlRd6jeJWmm6mnixs64t4RWPG6cX6z9wt2heHpS", "ncvLb5cHHjH8Iev1cckMq5hYzc7WFhnHDZJSQBIc");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
