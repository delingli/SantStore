package com.hai.appstore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hai.store.fragment.MoreListFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MoreListFragment aa=new MoreListFragment();
        aa.addOnMovieListScrollListener(new MoreListFragment.OnMovieListScrollListener() {
            @Override
            public void onMovieListScrolled(int i, int i1) {

            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.flcontinerss, new MoreListFragment()).commit();
//        Intent actionIntent = getIntent();
//        String down_list = actionIntent.getStringExtra("DOWN_LIST");
//        Intent intent = new Intent(this, TestActivity.class);
//     if (null != down_list) {
//            Log.e("start_store", "down_list " + down_list);
//            intent.putExtra("DOWN_LIST", down_list);
//            startActivity(intent);
//        } else {
//            if (isRoot()) {
//                finish();
//                return;
//            }
//            startActivity(intent);
//        }
//        finish();
    }

    private boolean isRoot() {
        return !isTaskRoot() || (getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0;
    }
}
