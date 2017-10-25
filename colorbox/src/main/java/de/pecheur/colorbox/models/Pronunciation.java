package de.pecheur.colorbox.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fischejo on 09.11.14.
 */
public class Pronunciation extends de.pecheur.dictionary.Pronunciation {
    private long id;


    public Pronunciation() {
        super();
    }

    public Pronunciation(long id) {
        super();
        this.id = id;
    }


    public Pronunciation(Parcel in) {
        super(in);
        id = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(id);
    }

    public static final Parcelable.Creator<Pronunciation> CREATOR = new Parcelable.Creator<Pronunciation>() {
        public Pronunciation createFromParcel(Parcel in) {
            return new Pronunciation(in);
        }

        public Pronunciation[] newArray(int size) {
            return new Pronunciation[size];
        }
    };
}
