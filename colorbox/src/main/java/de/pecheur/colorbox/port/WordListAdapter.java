package de.pecheur.colorbox.port;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.models.Word;


public class WordListAdapter extends ArrayAdapter<Word> {
    int mLayout;
    public WordListAdapter(Context context, int layout) {
        super(context, 0);
        mLayout = layout;
    }

    private class WordViewHolder {
        TextView frontText;
        TextView backText;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Word word = getItem(position);

        if (view == null) {
            // inflate
            view = LayoutInflater.from(getContext()).inflate(
                    mLayout, parent, false);

            // view holder
            WordViewHolder holder = new WordViewHolder();
            holder.frontText = (TextView) view.findViewById(R.id.text1);
            holder.backText = (TextView) view.findViewById(R.id.text2);
            view.setTag(holder);
        }

        WordViewHolder holder = (WordViewHolder) view.getTag();

        // text
        //holder.frontText.setText(word.getText(true));
        //holder.backText.setText(word.getText(false));


        return view;

    }
}