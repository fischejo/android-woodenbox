package de.pecheur.dictionary;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fischejo on 21.11.14.
 */
public class Pronunciation implements Parcelable{

    private String contributor;
    private String ipa;
    private Uri uri;

    public Pronunciation() {};

    public Pronunciation(Parcel in) {
        contributor = in.readString();
        ipa = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getIpa() {
        return ipa;
    }

    public void setIpa(String ipa) {
        this.ipa = ipa;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contributor);
        dest.writeString(ipa);
        dest.writeParcelable(uri, flags);
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
