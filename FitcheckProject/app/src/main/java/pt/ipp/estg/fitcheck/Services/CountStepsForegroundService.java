package pt.ipp.estg.fitcheck.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.Broadcasts.LowBatteryReceiver;
import pt.ipp.estg.fitcheck.DataBases.DOHistoryDB;
import pt.ipp.estg.fitcheck.DataBases.DailyObjectiveDB;
import pt.ipp.estg.fitcheck.Fragments.HomeFragment;
import pt.ipp.estg.fitcheck.Models.DOHistory;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.TransitionsReceiver;

public class CountStepsForegroundService extends LifecycleService {

    private SensorManager sensorManager;
    SensorEventListener listen;

    private Sensor stepCounterSensor;

    public long initialStepCount = -1;

    private FirebaseAuth auth;

    private NotificationChannel notificationChannel;

    private NotificationManager notificationManager;

    private NotificationCompat.Builder notificationBuilder;

    private Notification notification;

    private DailyObjectiveDB dailyObjectiveDB;

    private DOHistoryDB doHistoryDB;

    private FirebaseUser currentUser;

    private String currentDate = null;

    private Long currentGoalDay;

    private int daily_history_id = -1;

    private boolean isReady = false;

    public CountStepsForegroundService() {
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
        currentUser = auth.getCurrentUser();
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate() {
        super.onCreate();


        dailyObjectiveDB = DailyObjectiveDB.getInstance(this.getApplicationContext());
        doHistoryDB = DOHistoryDB.getInstance(this.getApplicationContext());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        notificationBuilder = new NotificationCompat.Builder(this, channelId);

    }


    public void requestActivityUpdates() {
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        ActivityTransitionRequest activityTransitionRequest = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(getApplicationContext(), TransitionsReceiver.class);
        intent.setAction(TransitionsReceiver.INTENT_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Task<Void> task = ActivityRecognition.getClient(this.getApplicationContext())
                .requestActivityTransitionUpdates(activityTransitionRequest, pendingIntent);

        task.addOnSuccessListener(a -> Log.d("S", "Transitions API was successfully registered."))
                .addOnFailureListener(b -> Log.d("F", "Transitions API could not be registered: " + b));
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getSteps(SensorEvent event) {
        if (event == null) {
            return;
        }

        ++initialStepCount;
        float it = event.values[event.values.length - 1];


        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_gym)
                .setContentTitle(initialStepCount + " passos").setShowWhen(false);

        if (currentGoalDay == null) {
            notificationBuilder.setContentText("Ainda não tem nenhum objetivo diário");
        } else if (this.initialStepCount > currentGoalDay) {
            notificationBuilder.setContentText("Completou o objetivo diário de " + currentGoalDay + " passos");
        } else {
            notificationBuilder.setContentText("Objetivo diário: " + currentGoalDay);
        }

        Intent notificationIntent = new Intent(CountStepsForegroundService.this, MenuActivity.class);
        notificationIntent.putExtra("fragment", "HomeFragment");

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent itPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(itPendingIntent);

        notification = notificationBuilder.build();

        notificationManager.notify(1, notification);
        if (isReady) {
            new DatabaseAsync().execute();
        }
    }

    private void updateObjective() {
        dailyObjectiveDB.daoAccess().findDailyObjectiveByUser(currentUser.getUid()).observe(this, dailyObjective -> {
            if (dailyObjective != null) {
                currentGoalDay = dailyObjective.objective;

            } else {
                currentGoalDay = null;
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        doHistoryDB.daoAccess().findDOHistoryByUserByDate(currentUser.getUid(), dtf.format(now)).observe(this, doHistoryList -> {
            if (doHistoryList != null) {
                initialStepCount = doHistoryList.achieved;
                currentDate = doHistoryList.data;
                daily_history_id = doHistoryList.daily_history_id;

            }

            dailyObjectiveDB.daoAccess().findDailyObjectiveByUser(currentUser.getUid()).observe(this, dailyObjective -> {
                if (dailyObjective != null) {
                    currentGoalDay = dailyObjective.objective;
                } else {
                    currentGoalDay = null;
                }
                isReady = true;
                startForegroundWithNotification();
            });
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        listen = new SensorListen();

        sensorManager.registerListener(listen, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        requestActivityUpdates();
        return START_STICKY;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundWithNotification() {


        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_gym)
                .setContentTitle(initialStepCount + " passos").setShowWhen(false);

        if (currentGoalDay == null) {
            notificationBuilder.setContentText("Ainda não tem nenhum objetivo diário");
        } else if (this.initialStepCount > currentGoalDay) {
            notificationBuilder.setContentText("Completou o objetivo diário de " + currentGoalDay + " passos");
        } else {
            notificationBuilder.setContentText("Objetivo diário: " + currentGoalDay);
        }

        notification = notificationBuilder.build();


        startForeground(1, notification);

    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "Serviços interrompidos", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "Fitcheck ChanelID";
        String channelName = "Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }


    public class SensorListen implements SensorEventListener {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                updateObjective();
                getDailyID();
                getSteps(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getDailyID() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        if (currentDate != null && currentDate.equals(dtf.format(now))) {
            doHistoryDB.daoAccess().findDOHistoryByUserByDate(currentUser.getUid(), currentDate).observe((LifecycleOwner) this, doHistoryList -> {
                if (doHistoryList != null) {
                    daily_history_id = doHistoryList.daily_history_id;
                } else {
                    daily_history_id = -1;
                }
            });
        }
    }

    private class DatabaseAsync extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime now = LocalDateTime.now();


            DOHistory doHistory = new DOHistory();

            doHistory.user_id = currentUser.getUid();

            if (daily_history_id != -1) {
                doHistory.daily_history_id = daily_history_id;
            }

            if (currentGoalDay == null) {
                doHistory.objective = 0;
            } else {
                doHistory.objective = currentGoalDay;
            }

            if (doHistory.achieved >= doHistory.objective) {
                doHistory.completed = true;
            }


            if (currentDate == null || !currentDate.equals(dtf.format(now))) {
                if (currentDate != null) {
                    doHistory.achieved = initialStepCount;

                    doHistory.data = currentDate;
                    doHistoryDB.daoAccess().updateDOHistory(doHistory);
                }
                initialStepCount = 0;
                currentDate = dtf.format(now);
                doHistory.achieved = initialStepCount;

                doHistory.data = currentDate;
                doHistoryDB.daoAccess().insertDOHistory(doHistory);
            } else {
                doHistory.achieved = initialStepCount;
                doHistory.data = currentDate;
                doHistoryDB.daoAccess().updateDOHistory(doHistory);
            }

            return null;
        }
    }
}

