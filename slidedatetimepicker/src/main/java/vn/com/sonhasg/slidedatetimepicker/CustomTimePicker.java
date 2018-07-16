package vn.com.sonhasg.slidedatetimepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

public class CustomTimePicker extends TimePicker
{
    private static final String TAG = "CustomTimePicker";

    public CustomTimePicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Class<?> idClass = null;
        Class<?> numberPickerClass = null;
        Field selectionDividerField = null;
        Field hourField = null;
        Field minuteField = null;
        Field amPmField = null;
        NumberPicker hourNumberPicker = null;
        NumberPicker minuteNumberPicker = null;
        NumberPicker amPmNumberPicker = null;

        try
        {
            idClass = Class.forName("com.android.internal.R$id");

            hourField = idClass.getField("hour");
            minuteField = idClass.getField("minute");
            amPmField = idClass.getField("amPm");

            hourNumberPicker = (NumberPicker) findViewById(hourField.getInt(null));
            minuteNumberPicker = (NumberPicker) findViewById(minuteField.getInt(null));
            amPmNumberPicker = (NumberPicker) findViewById(amPmField.getInt(null));

            numberPickerClass = Class.forName("android.widget.NumberPicker");

            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(hourNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(minuteNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(amPmNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
        }
        catch (ClassNotFoundException e)
        {
            Log.e(TAG, "ClassNotFoundException in CustomTimePicker", e);
        }
        catch (NoSuchFieldException e)
        {
            Log.e(TAG, "NoSuchFieldException in CustomTimePicker", e);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "IllegalAccessException in CustomTimePicker", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "IllegalArgumentException in CustomTimePicker", e);
        }
    }
}
