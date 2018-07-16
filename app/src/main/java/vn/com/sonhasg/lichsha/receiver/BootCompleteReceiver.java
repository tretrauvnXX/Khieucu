package vn.com.sonhasg.lichsha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vn.com.sonhasg.lichsha.GPSTrackerService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GPSTrackerService.class));
    }
}
