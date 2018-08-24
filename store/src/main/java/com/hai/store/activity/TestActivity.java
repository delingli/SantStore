package com.hai.store.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.hai.store.R;
import com.hai.store.fragment.MoreListFragment;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        FrameLayout flcontiner = (FrameLayout) findViewById(R.id.flcontiner);
        getSupportFragmentManager().beginTransaction().replace(R.id.flcontiner, new MoreListFragment()).commit();
    }
}
