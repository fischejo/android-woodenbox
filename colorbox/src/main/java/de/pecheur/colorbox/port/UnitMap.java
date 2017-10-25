package de.pecheur.colorbox.port;

import java.util.ArrayList;
import java.util.List;

import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.models.Word;

/**
 * Created by fischejo on 26.07.14.
 */
class UnitMap extends Unit {
    private List<Word> words = new ArrayList<Word>();

    public void add(Word word) {
        words.add(word);
    }

    public void remove(Word word) {
        words.remove(word);
    }

    public List<Word> getContent() {
        return words;
    }
}
