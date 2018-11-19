//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.mildperate;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MildOperatorConfig implements Parcelable {
    public String market;

    protected MildOperatorConfig(Parcel in) {
        market = in.readString();
    }

    public MildOperatorConfig(String market) {
        this.market = market;
    }

    public MildOperatorConfig() {
    }

    public static final Creator<MildOperatorConfig> CREATOR = new Creator<MildOperatorConfig>() {
        @Override
        public MildOperatorConfig createFromParcel(Parcel in) {
            return new MildOperatorConfig(in);
        }

        @Override
        public MildOperatorConfig[] newArray(int size) {
            return new MildOperatorConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(market);
    }
}
