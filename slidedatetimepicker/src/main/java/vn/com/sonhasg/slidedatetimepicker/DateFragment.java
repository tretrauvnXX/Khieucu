package vn.com.sonhasg.slidedatetimepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import java.util.Date;

public class DateFragment extends Fragment
{
    public interface DateChangedListener
    {
        void onDateChanged(int year, int month, int day);
    }

    private DateChangedListener mCallback;
    private CustomDatePicker mDatePicker;

    public DateFragment()
    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            mCallback = (DateChangedListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Calling fragment must implement DateFragment.DateChangedListener interface");
        }
    }

    public static final DateFragment newInstance(int theme, int year, int month,
                                                 int day, Date minDate, Date maxDate)
    {
        DateFragment f = new DateFragment();

        Bundle b = new Bundle();
        b.putInt("theme", theme);
        b.putInt("year", year);
        b.putInt("month", month);
        b.putInt("day", day);
        b.putSerializable("minDate", minDate);
        b.putSerializable("maxDate", maxDate);
        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        int theme = getArguments().getInt("theme");
        int initialYear = getArguments().getInt("year");
        int initialMonth = getArguments().getInt("month");
        int initialDay = getArguments().getInt("day");
        Date minDate = (Date) getArguments().getSerializable("minDate");
        Date maxDate = (Date) getArguments().getSerializable("maxDate");

        Context contextThemeWrapper = new ContextThemeWrapper(
                getActivity(),
                theme == SlideDateTimePicker.HOLO_DARK ?
                         android.R.style.Theme_Holo :
                         android.R.style.Theme_Holo_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View v = localInflater.inflate(R.layout.fragment_date, container, false);

        mDatePicker = (CustomDatePicker) v.findViewById(R.id.datePicker);

        mDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mDatePicker.init(
            initialYear,
            initialMonth,
            initialDay,
            new OnDateChangedListener() {

                @Override
                public void onDateChanged(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth)
                {
                    mCallback.onDateChanged(year, monthOfYear, dayOfMonth);
                }
            });

        if (minDate != null)
            mDatePicker.setMinDate(minDate.getTime());

        if (maxDate != null)
            mDatePicker.setMaxDate(maxDate.getTime());

        return v;
    }
}
