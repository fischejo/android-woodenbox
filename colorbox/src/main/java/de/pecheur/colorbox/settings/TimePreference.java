package de.pecheur.colorbox.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;


// TODO saving and restoring the preference's state
// TODO summery + getString(int, obj[]) format
// TODO simplify onSetInitialValue(...)

public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;
    private CharSequence summary;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        calendar = Calendar.getInstance();
        summary = super.getSummary();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setIs24HourView(true);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            // if time is in past then add a day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);    // tomorrow
            }

            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }

            setSummary(getSummary());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        Calendar temp = Calendar.getInstance();
        if (restoreValue) {
            if (defaultValue == null) {
                temp.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                temp.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                temp.setTimeInMillis(System.currentTimeMillis());
            } else {
                temp.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));

        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (summary != null && calendar != null) {
            Date date = new Date(calendar.getTimeInMillis());
            String time = DateFormat.getTimeFormat(getContext()).format(date);
            return String.format(summary.toString(), time);
        }
        return null;
    }
} 