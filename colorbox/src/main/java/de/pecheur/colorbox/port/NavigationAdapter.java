package de.pecheur.colorbox.port;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.R;


public class NavigationAdapter extends ArrayAdapter<UnitMap> {
    public NavigationAdapter(Context context,List<UnitMap> units ) {
        super(context, 0, units);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.import_spinner_item, parent, false);
        }

        Unit unit = getItem(position);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(unit.getTitle());

        TextView subtitle = (TextView) convertView.findViewById(R.id.subtitle);
        subtitle.setText(getContext().getString(
                R.string.import_spinner_subtitle,
                unit.getFrontLanguage(),
                unit.getBackLanguage()));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
