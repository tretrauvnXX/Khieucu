package vn.com.sonhasg.slidedatetimepicker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.Date;

public class SlideDateTimePicker
{
    public static final int HOLO_DARK = 1;
    public static final int HOLO_LIGHT = 2;

    private FragmentManager mFragmentManager;
    private SlideDateTimeListener mListener;
    private Date mInitialDate;
    private Date mMinDate;
    private Date mMaxDate;
    private boolean mIsClientSpecified24HourTime;
    private boolean mIs24HourTime;
    private int mTheme;
    private int mIndicatorColor;

    public SlideDateTimePicker(FragmentManager fm)
    {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(SlideDateTimeDialogFragment.TAG_SLIDE_DATE_TIME_DIALOG_FRAGMENT);

        if (prev != null)
        {
            ft.remove(prev);
            ft.commit();
        }

        mFragmentManager = fm;
    }

    public void setListener(SlideDateTimeListener listener)
    {
        mListener = listener;
    }

    public void setInitialDate(Date initialDate)
    {
        mInitialDate = initialDate;
    }

    public void setMinDate(Date minDate)
    {
        mMinDate = minDate;
    }

    public void setMaxDate(Date maxDate)
    {
        mMaxDate = maxDate;
    }

    private void setIsClientSpecified24HourTime(boolean isClientSpecified24HourTime)
    {
        mIsClientSpecified24HourTime = isClientSpecified24HourTime;
    }

    public void setIs24HourTime(boolean is24HourTime)
    {
        setIsClientSpecified24HourTime(true);
        mIs24HourTime = is24HourTime;
    }

    public void setTheme(int theme)
    {
        mTheme = theme;
    }

    public void setIndicatorColor(int indicatorColor)
    {
        mIndicatorColor = indicatorColor;
    }

    public void show()
    {
        if (mListener == null)
        {
            throw new NullPointerException("Attempting to bind null listener to SlideDateTimePicker");
        }

        if (mInitialDate == null)
        {
            setInitialDate(new Date());
        }

        SlideDateTimeDialogFragment dialogFragment =
                SlideDateTimeDialogFragment.newInstance(
                        mListener,
                        mInitialDate,
                        mMinDate,
                        mMaxDate,
                        mIsClientSpecified24HourTime,
                        mIs24HourTime,
                        mTheme,
                        mIndicatorColor);

        dialogFragment.show(mFragmentManager,
                SlideDateTimeDialogFragment.TAG_SLIDE_DATE_TIME_DIALOG_FRAGMENT);
    }

    public static class Builder
    {
        private FragmentManager fm;
        private SlideDateTimeListener listener;

        private Date initialDate;
        private Date minDate;
        private Date maxDate;
        private boolean isClientSpecified24HourTime;
        private boolean is24HourTime;
        private int theme;
        private int indicatorColor;

        public Builder(FragmentManager fm)
        {
            this.fm = fm;
        }

        public Builder setListener(SlideDateTimeListener listener)
        {
            this.listener = listener;
            return this;
        }

        public Builder setInitialDate(Date initialDate)
        {
            this.initialDate = initialDate;
            return this;
        }

        public Builder setMinDate(Date minDate)
        {
            this.minDate = minDate;
            return this;
        }

        public Builder setMaxDate(Date maxDate)
        {
            this.maxDate = maxDate;
            return this;
        }

        public Builder setIs24HourTime(boolean is24HourTime)
        {
            this.isClientSpecified24HourTime = true;
            this.is24HourTime = is24HourTime;
            return this;
        }

        public Builder setTheme(int theme)
        {
            this.theme = theme;
            return this;
        }

        public Builder setIndicatorColor(int indicatorColor)
        {
            this.indicatorColor = indicatorColor;
            return this;
        }

        public SlideDateTimePicker build()
        {
            SlideDateTimePicker picker = new SlideDateTimePicker(fm);
            picker.setListener(listener);
            picker.setInitialDate(initialDate);
            picker.setMinDate(minDate);
            picker.setMaxDate(maxDate);
            picker.setIsClientSpecified24HourTime(isClientSpecified24HourTime);
            picker.setIs24HourTime(is24HourTime);
            picker.setTheme(theme);
            picker.setIndicatorColor(indicatorColor);

            return picker;
        }
    }
}
