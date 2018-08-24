package com.hai.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hai.store.R;
import com.hai.store.activity.DetailActivity;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.CardViewHolder> {

    private Context context;
    private List<String> list;

    public CardViewAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public CardViewAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(CardViewAdapter.CardViewHolder holder, int position) {
        holder.appName.setText(list.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, DetailActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        TextView appName;

        CardViewHolder(View itemView) {
            super(itemView);
            appName = (TextView) itemView.findViewById(R.id.app_name);
        }
    }
}
