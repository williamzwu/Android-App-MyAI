package com.dynaxtech.william.myai;

/**
 * Created by william on 12/10/2017.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by william on 8/5/2017.
 */

public
class AppParcelable implements Parcelable {
    private String mData;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mData);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<AppParcelable> CREATOR
            = new Parcelable.Creator<AppParcelable>() {
        public AppParcelable createFromParcel(Parcel in) {
            return new AppParcelable(in);
        }

        public AppParcelable[] newArray(int size) {
            return new AppParcelable[size];
        }
    };

    private AppParcelable(Parcel in) {
        mData = in.readString();
    }

    public String getString()
    {
        return mData;
    }
}
