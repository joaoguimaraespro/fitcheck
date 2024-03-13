package pt.ipp.estg.fitcheck.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;
import pt.ipp.estg.fitcheck.Services.TrainingService;

public class LowBatteryReceiver extends BroadcastReceiver {

    private Context mContext;
    private Intent mIntent;
    public boolean isOver;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        this.mIntent = intent;
        this.isOver = true;

        String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            Log.d("Log_Paco", "onReceiveBroadcast: ");
        }

    }

}
