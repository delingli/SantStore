package com.hai.store.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseViewHolder<ViewModel> extends RecyclerView.ViewHolder {

    protected Context mContext;

    public BaseViewHolder(View view) {
        super(view);
        mContext = view.getContext();
    }

    public abstract void onBind(ViewModel viewModel, int position);
}
