package pt.ipp.estg.fitcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;

public class RebootReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, CountStepsForegroundService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}