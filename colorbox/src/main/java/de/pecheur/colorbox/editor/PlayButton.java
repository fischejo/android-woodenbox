package de.pecheur.colorbox.editor;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by fischejo on 08.11.14.
 */
public class PlayButton extends ImageView{
    private MediaPlayer mediaPlayer;

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PlayButton(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mediaPlayer = new MediaPlayer();

    }

    @Override
    public boolean performClick() {

        mediaPlayer.start();
        return true;
    }

    public void setDataSource(Uri uri) {
        /*try {
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
