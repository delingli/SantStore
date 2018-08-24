package com.hai.store.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.List;

public abstract class BaseListAdapter<ViewModel> extends RecyclerView.Adapter<BaseViewHolder<ViewModel>> {

    public Context mContext;
    public LayoutInflater mInflater;
    public List<ViewModel> mData;

    public BaseListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<ViewModel> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public List<ViewModel> getData() {
        return mData;
    }

    public void addData(List<ViewModel> data) {
        int position = mData.size();
        mData.addAll(data);
        notifyItemChanged(position, data.size());
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<ViewModel> holder, int position) {
        holder.onBind(mData.get(holder.getAdapterPosition()), position);
    }

    @Override
    public int getItemCount() {
        return null == mData ? 0 : mData.size();
    }
}
