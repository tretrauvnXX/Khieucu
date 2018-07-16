package vn.com.sonhasg.dynamiccalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nguyenphuoc on 25-04-2017.
 */

public class DynamicCalendar extends LinearLayout {

    private Context context;
    private AttributeSet attrs;
    private View rootView;

    private RecyclerView recyclerView_dates;
    private TextView tv_month_year, tv_sun;
    public ImageView iv_previous, iv_next;

    private OnDateClickListener onDateClickListener;

    private ArrayList<DateModel> dateModelList;
    private DateListAdapter dateListAdapter;

    private SimpleDateFormat sdfMonthYear = new SimpleDateFormat("MMM - yyyy");

    private Calendar calendar = Calendar.getInstance();

    public DynamicCalendar(Context context) {
        super(context);

        this.context = context;

        if (!isInEditMode()) {
            init();
        }
    }

    public DynamicCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.attrs = attrs;

        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {

        /*TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCalendar, 0, 0);

        try {
            //strHeaderBackgroundColor = a.getString(R.styleable.MyCalendar_headerBackgroundColor);
            //strHeaderTextColor = a.getString(R.styleable.MyCalendar_headerTextColor);
        } finally {
            a.recycle();
        }*/

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.dynamic_calendar, this, true);

        recyclerView_dates = (RecyclerView) rootView.findViewById(R.id.recyclerView_dates);
        iv_previous = (ImageView) rootView.findViewById(R.id.iv_previous);
        iv_next = (ImageView) rootView.findViewById(R.id.iv_next);
        tv_month_year = (TextView) rootView.findViewById(R.id.tv_month_year);
        tv_sun = (TextView) rootView.findViewById(R.id.tv_sun);
    }

    public void isSundayOff(boolean b, String layoutcolor, String textcolor) {
        if (b) {
            if (!TextUtils.isEmpty(layoutcolor) && !TextUtils.isEmpty(textcolor)) {
                AppConstants.isSundayOff = true;
                AppConstants.strSundayOffBackgroundColor = layoutcolor;
                AppConstants.strSundayOffTextColor = textcolor;
                tv_sun.setTextColor(Color.parseColor(textcolor));
            }
        }
    }

    public void setExtraDatesOfMonthBackgroundColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            AppConstants.strExtraDatesBackgroundColor = color;
        }
    }

    public void setExtraDatesOfMonthTextColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            AppConstants.strExtraDatesTextColor = color;
        }
    }

    public void setCurrentDateBackgroundColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            AppConstants.strCurrentDateBackgroundColor = color;
        }
    }

    public void addEvent(String date, String gps, String gpsOk, String gpsError) {
        if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(gps) && !TextUtils.isEmpty(gpsOk) && !TextUtils.isEmpty(gpsError)) {
            String tmpDate = GlobalMethods.convertDate(date, AppConstants.sdfDate, AppConstants.sdfDate);
            AppConstants.eventList.add(new EventModel(tmpDate, gps, gpsOk, gpsError));
        }
    }

    public void deleteAllEvent() {
        AppConstants.eventList.clear();
    }

    public void setMonthView(String sign) {
        dateModelList = new ArrayList<>();
        dateListAdapter = new DateListAdapter(context, dateModelList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 7);
        recyclerView_dates.setLayoutManager(gridLayoutManager);

        recyclerView_dates.setAdapter(dateListAdapter);

        dateListAdapter.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(Date date) {
                if (onDateClickListener != null) {
                    onDateClickListener.onClick(date);
                }
            }

            @Override
            public void onLongClick(Date date) {
                if (onDateClickListener != null) {
                    onDateClickListener.onLongClick(date);
                }
            }
        });

        if (sign.equals("add")) {
            AppConstants.main_calendar.set(Calendar.MONTH, (AppConstants.main_calendar.get(Calendar.MONTH) + 1));
        } else if (sign.equals("sub")) {
            AppConstants.main_calendar.set(Calendar.MONTH, (AppConstants.main_calendar.get(Calendar.MONTH) - 1));
        }

        tv_month_year.setText(sdfMonthYear.format(AppConstants.main_calendar.getTime()));

        calendar.setTime(AppConstants.main_calendar.getTime());
        calendar.set((Calendar.DAY_OF_MONTH), 1);

        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 2;

        if (monthBeginningCell == -1) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else if (monthBeginningCell == 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);
        }

        dateModelList.clear();

        while (dateModelList.size() < 42) {
            DateModel model = new DateModel();
            model.setDates(calendar.getTime());
            dateModelList.add(model);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        dateListAdapter.notifyDataSetChanged();
    }

    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        this.onDateClickListener = onDateClickListener;
    }

    public void goToCurrentDate() {
        AppConstants.main_calendar = Calendar.getInstance();

        setMonthView("");
    }

    public Date getCurrentMonth() {
        return AppConstants.main_calendar.getTime();
    }

    public void refreshCalendar() {
        setMonthView("");
    }

}
