package vn.com.sonhasg.slidedatetimepicker;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TimePicker;

public class TimeFragment extends Fragment
{
    public interface TimeChangedListener
    {
        void onTimeChanged(int hour, int minute);
    }

    private TimeChangedListener mCallback;
    private TimePicker mTimePicker;

    public TimeFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            mCallback = (TimeChangedListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Calling fragment must implement TimeFragment.TimeChangedListener interface");
        }
    }

    public static final TimeFragment newInstance(int theme, int hour, int minute,
        boolean isClientSpecified24HourTime, boolean is24HourTime)
    {
        TimeFragment f = new TimeFragment();

        Bundle b = new Bundle();
        b.putInt("theme", theme);
        b.putInt("hour", hour);
        b.putInt("minute", minute);
        b.putBoolean("isClientSpecified24HourTime", isClientSpecified24HourTime);
        b.putBoolean("is24HourTime", is24HourTime);
        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        int theme = getArguments().getInt("theme");
        int initialHour = getArguments().getInt("hour");
        int initialMinute = getArguments().getInt("minute");
        boolean isClientSpecified24HourTime = getArguments().getBoolean("isClientSpecified24HourTime");
        boolean is24HourTime = getArguments().getBoolean("is24HourTime");

        Context contextThemeWrapper = new ContextThemeWrapper(
                getActivity(),
                theme == SlideDateTimePicker.HOLO_DARK ?
                         android.R.style.Theme_Holo :
                         android.R.style.Theme_Holo_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View v = localInflater.inflate(R.layout.fragment_time, container, false);

        mTimePicker = (TimePicker) v.findViewById(R.id.timePicker);

        mTimePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
            {
                mCallback.onTimeChanged(hourOfDay, minute);
            }
        });

        if (isClientSpecified24HourTime)
        {
            mTimePicker.setIs24HourView(is24HourTime);
        }
        else
        {
            mTimePicker.setIs24HourView(DateFormat.is24HourFormat(
                getTargetFragment().getActivity()));
        }

        mTimePicker.setCurrentHour(initialHour);
        mTimePicker.setCurrentMinute(initialMinute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        {
            fixTimePickerBug18982();
        }

        return v;
    }

    private void fixTimePickerBug18982()
    {
        View amPmView = ((ViewGroup) mTimePicker.getChildAt(0)).getChildAt(3);

        if (amPmView instanceof NumberPicker)
        {
            ((NumberPicker) amPmView).setOnValueChangedListener(new OnValueChangeListener() {

                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal)
                {
                    if (picker.getValue() == 1)  // PM
                    {
                        if (mTimePicker.getCurrentHour() < 12)
                            mTimePicker.setCurrentHour(mTimePicker.getCurrentHour() + 12);
                    }
                    else  // AM
                    {
                        if (mTimePicker.getCurrentHour() >= 12)
                            mTimePicker.setCurrentHour(mTimePicker.getCurrentHour() - 12);
                    }

                    mCallback.onTimeChanged(
                        mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute());
                }
            });
        }
    }
}
