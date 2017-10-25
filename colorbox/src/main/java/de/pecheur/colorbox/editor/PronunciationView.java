package de.pecheur.colorbox.editor;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.models.Pronunciation;

import java.util.ArrayList;

/**
 * Created by fischejo on 21.11.14.
 */
public class PronunciationView extends LinearLayout {
    private ArrayList<View> recycleBin;
    private LayoutInflater layoutInflater;


    public PronunciationView(Context context) {
        this(context, null);
    }

    public PronunciationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PronunciationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        recycleBin = new ArrayList<View>();
        layoutInflater = LayoutInflater.from(context);
    }


    public void add(Pronunciation pronunciation) {
        add(pronunciation, false);
    }



    public void add(Pronunciation pronunciation, boolean checked) {
        // do some recycling
        View child = recycleBin.isEmpty() ? layoutInflater.inflate(R.layout.editor_audio_item_checked, this, false) :
                recycleBin.remove(0);

        child.setTag(pronunciation);

        PlayButton playButton = (PlayButton) child.findViewById(R.id.playButton);
        playButton.setDataSource(pronunciation.getUri());

        TextView text = (TextView) child.findViewById(R.id.textView);
        text.setText(pronunciation.getContributor());

        // checkable handling
        child.setOnClickListener(mToggleListener);
        ((Checkable) child).setChecked(checked);

        addView(child);
    }

    public void removeAll() {
        int size = getChildCount();
        for(int i = 0; i < size; i++) {
            recycleBin.add(getChildAt(i));
        }
        removeAllViews();
    }


    public ArrayList<Pronunciation> getChecked() {
        ArrayList<Pronunciation> checked = new ArrayList<Pronunciation>();
        int size = getChildCount();
        for(int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if(((CheckBox)child).isChecked()) {
                checked.add((Pronunciation) child.getTag());
            }
        }
        return checked;
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState();

        int size = getChildCount();
        Pronunciation[] pronunciations = new Pronunciation[size];
        boolean[] checked = new boolean[size];
        for(int i = 0; i < size; i++) {
            View child = getChildAt(i);
            pronunciations[i] = (Pronunciation) child.getTag();
            checked[i] = ((CheckBox)child).isChecked();
        }
        savedState.pronunciations = pronunciations;
        savedState.checked = checked;
        return savedState;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable savedInstanceState) {
        SavedState savedState = (SavedState) savedInstanceState;
        Pronunciation[] pronunciations = savedState.pronunciations;
        boolean[] checked = savedState.checked;

        for(int i = 0; i < pronunciations.length; i++) {
            add( pronunciations[i], checked[i]);
        }
    }


    private class SavedState implements Parcelable {
        public boolean[] checked;
        public Pronunciation[] pronunciations;

        public SavedState() {};

        public SavedState(Parcel in) {
            in.readBooleanArray(checked);
            pronunciations = (Pronunciation[]) in.readParcelableArray(Parcelable.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeBooleanArray(checked);
            dest.writeParcelableArray(pronunciations, flags);
        }
    }


    private final View.OnClickListener mToggleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Checkable checkable = (Checkable) v;
            checkable.toggle();
        }
    };
}
