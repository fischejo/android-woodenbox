package de.pecheur.colorbox.editor;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.ContentColumn;
import de.pecheur.colorbox.models.Pronunciation;
import de.pecheur.colorbox.models.Word;
import de.pecheur.dictionary.Dictionary;
import de.pecheur.dictionary.DictionaryCallback;

import java.util.ArrayList;


public class SideFragment extends Fragment implements View.OnFocusChangeListener, DictionaryCallback {
    private static final String ARGS_WORD = "word";
    private static final String ARGS_SIDE = "side";



    private Context mContext;
    private ContentResolver contentResolver;
    private Dictionary mDictionary;

    private boolean bSide;
    private boolean bScrollable = true;

    // text
    private AutoCompleteTextView editText;
    private ArrayAdapter<String> editAdapter;

    // exmaples
    private AutoCompleteTextView exampleText;
    private ArrayAdapter<String> exampleAdapter;

    // pronunciation
    private PronunciationView pronunciationView;




    public static SideFragment newInstance(Word word, boolean side) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_WORD, word);
        args.putBoolean(ARGS_SIDE, side);

        SideFragment fragment = new SideFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (bScrollable) {
            ScrollView scrollView = new ScrollView(container.getContext());
            inflater.inflate(R.layout.editor_side_fragment, scrollView);
            return scrollView;
        } else {
            return inflater.inflate(R.layout.editor_side_fragment, container, false);
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText = (AutoCompleteTextView) view.findViewById(R.id.edit);
        editText.setOnFocusChangeListener(this);
        // TODO: editText.setAdapter(editAdapter);


        exampleText = (AutoCompleteTextView) view.findViewById(R.id.example);
        exampleAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item);
        exampleText.setAdapter(exampleAdapter);

        pronunciationView = (PronunciationView) view.findViewById(R.id.pronunciations);


        if(savedInstanceState == null) {
            // todo cases unhandled
            Bundle args = getArguments();
            if (args != null) {
                bSide = args.getBoolean(ARGS_SIDE);
                onLoadWord((Word) args.getParcelable(ARGS_WORD));
            }
        }



    }




    public void onLoadWord(Word word) {
        if(word != null && word.hasId()) {
            Cursor c;
            String[] projection = new String[] {ContentColumn._ID, ContentColumn.DATA};
            String selection = ContentColumn.WORD + " = ? AND "+ ContentColumn.SIDE + " = ?";
            String[] args = new String[] {String.valueOf(word.getId()), String.valueOf(bSide)};

            // load text
            c = contentResolver.query(VocabularyProvider.TEXT_URI, projection, selection, args, null);
            if(c.moveToFirst()) editText.setText(c.getString(1));
            c.close();

            // load example
            c = contentResolver.query(VocabularyProvider.EXAMPLE_URI, projection, selection, args, null);
            if(c.moveToFirst()) editText.setText(c.getString(1));
            c.close();

            // load pronunciations
            c = contentResolver.query(VocabularyProvider.AUDIO_URI, projection, selection, args, null);
            for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                if(!c.isNull(1)) {
                    Pronunciation pronunciation = new Pronunciation(c.getLong(0));
                    pronunciation.setUri( Uri.parse( c.getString(1)));
                    pronunciation.setContributor( c.getString(2));
                    pronunciationView.add(pronunciation);
                };
            }
            c.close();


        }
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void onSaveWord(Word word) {
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();
/*
        // update text
        cpo.add(ContentProviderOperation.newUpdate(VocabularyProvider.CONTENT_URI)
                //.withValue(AttachmentColumns.MIME, a.getMimeType())
                .withValue(AttachmentColumns.DATA, data)
                .build());
*/
    }



    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SideFragment);
        bSide = a.getBoolean(R.styleable.SideFragment_side, bSide);
        bScrollable = a.getBoolean(R.styleable.SideFragment_scrollable, bScrollable);
        a.recycle();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mContext = (Context) activity;
            contentResolver = activity.getContentResolver();
            mDictionary = ((EditorActivity) activity).getDictionary();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be EditorActivity.");
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId() == R.id.edit && !hasFocus) {
            Log.d("p1", "EditText lost focus -> update");
            onSuggest(editText.getText().toString());
        }
    }



    private void onSuggest(String cs) {
        exampleAdapter.clear();
        pronunciationView.removeAll();

        mDictionary.query((bSide ? 0 : 1), cs);
    }

    @Override
    public void onCompilation(int id, Bundle bundle) {
        if(bSide == (id==0)) {
            exampleAdapter.addAll(bundle.getStringArrayList("example"));

            //pronunciationView.add();
        }
    }

    @Override
    public void onError(int id, int code) {
        if(bSide == (id==0)) {

        }
    }
}
