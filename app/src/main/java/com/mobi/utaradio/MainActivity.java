package com.mobi.utaradio;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.parse.Parse;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        if(action.equals(MusicService.OPEN_FORGROUND_ACTIVITY) ){
            Log.d("DEBUG", "I RAQ");
            finish();
        } else {
            Log.d("DEBUG", "I RAN");
        }


        setContentView(R.layout.activity_main);
        //initialize parse
        Parse.initialize(this, "5qlRd6jeJWmm6mnixs64t4RWPG6cX6z9wt2heHpS", "ncvLb5cHHjH8Iev1cckMq5hYzc7WFhnHDZJSQBIc");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

}
