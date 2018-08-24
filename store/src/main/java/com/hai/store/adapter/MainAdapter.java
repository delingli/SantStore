package com.hai.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hai.store.R;
import com.hai.store.activity.MoreListActivity;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainHolder> {

    private List<List<String>> list;
    private Context context;

    public MainAdapter(Context context, List<List<String>> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MainAdapter.MainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MainHolder(LayoutInflater.from(context).inflate(R.layout.item_main_list, parent, false));
    }

    @Override
    public void onBindViewHolder(MainAdapter.MainHolder holder, int position) {
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(new CardViewAdapter(context, list.get(position)));
        holder.recyclerView.setNestedScrollingEnabled(false); //滑动冲突
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, MoreListActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }

    class MainHolder extends RecyclerView.ViewHolder {

        TextView textView, more;
        RecyclerView recyclerView;

        MainHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.card_view_type);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.card_view_rl);
            more = (TextView) itemView.findViewById(R.id.more);
        }
    }
}
