package pt.ipp.estg.fitcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import java.util.Objects;

import pt.ipp.estg.fitcheck.Services.TrainingService;

public class TransitionsReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "pt.ipp.estg.fitcheck.ACTION_PROCESS_ACTIVITY_TRANSITIONS";
    public boolean service = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : Objects.requireNonNull(result).getTransitionEvents()) {
                Intent it = new Intent(context, TrainingService.class);
                it.putExtra("Activity type", event.getActivityType());
                it.putExtra("Transition type", event.getTransitionType());
                context.startForegroundService(it);
            }
        }
    }
}