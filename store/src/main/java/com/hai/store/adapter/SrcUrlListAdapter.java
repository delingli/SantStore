package com.hai.store.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hai.store.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SrcUrlListAdapter extends RecyclerView.Adapter<SrcUrlListAdapter.SrcListHolder> {

    private List<String> list;
    private Context context;

    public SrcUrlListAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public SrcUrlListAdapter.SrcListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SrcListHolder(LayoutInflater.from(context).inflate(R.layout.item_app_scre, parent, false));
    }

    @Override
    public void onBindViewHolder(SrcUrlListAdapter.SrcListHolder holder, int position) {
        Picasso.with(context).load(list.get(position)).into(holder.src);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SrcListHolder extends RecyclerView.ViewHolder {

        ImageView src;

        SrcListHolder(View itemView) {
            super(itemView);
            src = (ImageView) itemView.findViewById(R.id.iv_src);
        }

    }
}
