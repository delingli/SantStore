package com.hai.store.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.hai.store.R;

public class OneAppHolder extends RecyclerView.ViewHolder {

    public CheckBox appCheck;
    public ImageView appIcon;
    public TextView appName;

    public OneAppHolder(View itemView) {
        super(itemView);
        appCheck = (CheckBox) itemView.findViewById(R.id.app_check);
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
    }

    public void hideCheckbox() {
        appCheck.setVisibility(View.GONE);
    }
}
