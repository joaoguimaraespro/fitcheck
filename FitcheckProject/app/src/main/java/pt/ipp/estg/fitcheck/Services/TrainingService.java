package pt.ipp.estg.fitcheck.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.Broadcasts.LowBatteryReceiver;
import pt.ipp.estg.fitcheck.DataBases.TrainingDB;
import pt.ipp.estg.fitcheck.Models.Enums.TipoTreinoEnum;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.Models.TrainingResponse;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.Models.UserTrainings;
import pt.ipp.estg.fitcheck.R;

public class TrainingService extends LifecycleService implements SensorEventListener {

    private AsyncTask asyncTask;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;

    private LocationCallback locationCallback;
    private FusedLocationProviderClient mLocationClient;
    private TipoTreinoEnum tipoTreinoEnum;
    private PolylineOptions polylineOptions;
    private float initialStepCount = -1;
    private float finalStepCount;
    private long startTime;
    private LatLng latLng;
    private Sensor stepCounterSensor;
    private SensorManager sensorManager;

    private FirebaseUser currentUser;
    private FirebaseAuth auth;

    private String activityType, transitionType;
    private int at, tt;
    public boolean battery = false;

    private TrainingDB trainingDB;

    static Context context;

    private LowBatteryReceiver mBatteryReceiver = new LowBatteryReceiver();

    public TrainingService() {
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

        startTime = System.currentTimeMillis();

        context = getApplicationContext();

        trainingDB = TrainingDB.getInstance(this);

        //Regist Receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(mBatteryReceiver, filter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isChargingorCharged = status == BatteryManager.BATTERY_STATUS_CHARGING;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float) scale;

        battery = (batteryPct >= 40) || isChargingorCharged;


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        notificationBuilder = new NotificationCompat.Builder(this, channelId);

        if (battery) {
            mLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Log.d("TAG", "Lat is: " + locationResult.getLastLocation().getLatitude() + ", "
                            + "Lng is: " + locationResult.getLastLocation().getLongitude());
                    if (latLng != null) {
                        Location new_loc = new Location(locationResult.getLastLocation());
                        Location old_loc = new Location(LocationManager.GPS_PROVIDER);
                        old_loc.setLatitude(latLng.latitude);
                        old_loc.setLongitude(latLng.longitude);
                        if (new_loc.distanceTo(old_loc) > 10) {
                            latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            polylineOptions.add(latLng);
                        }
                    } else {
                        latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        polylineOptions.add(latLng);
                    }
                }
            };
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "Fitcheck ChanelID1";
        String channelName = "Training Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder notification1 = notificationBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.ic_gym)
                .setContentTitle("Treino concluído");


        at = (int) intent.getExtras().get("Activity type");
        tt = (int) intent.getExtras().get("Transition type");

        switch (at) {
            case 1:
                activityType = "de bicicleta";
                tipoTreinoEnum = TipoTreinoEnum.bicicleta;
                break;
            case 3:
                activityType = "parado";
                break;
            case 7:
                activityType = "a caminhar";
                tipoTreinoEnum = TipoTreinoEnum.caminhada;
                break;
            case 8:
                activityType = "a correr";
                tipoTreinoEnum = TipoTreinoEnum.corrida;
                break;
        }

        if (tt == 0) {
            transitionType = "Está";
        }


        if (battery) {
            if (at != 3) {
                ++initialStepCount;
                polylineOptions = new PolylineOptions();
                requestLocation();
                startForegroundWithNotification();
            } else {
                mLocationClient.removeLocationUpdates(locationCallback);
                asyncTask = new TrainingService.DatabaseAsync();
                finalStepCount = initialStepCount;
                initialStepCount = -1;
                new DatabaseAsync().execute();
                notificationManager.notify(3, notificationBuilder.build());
                stopSelf();


            }
        } else {
            if (at != 3) {
                startForegroundWithNotification();
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MenuActivity.class);

                Training treino = new Training();
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dateOnly = dateFormat.format(currentDate);
                treino.user_id = currentUser.getUid();
                treino.tipo = tipoTreinoEnum;
                treino.duracao = (int) (System.currentTimeMillis() - startTime);
                treino.data = dateOnly;

                resultIntent.putExtra("treino", treino);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
                notification1.addAction(R.drawable.ic_gym, "Confirmar", resultPendingIntent);
                notificationManager.notify(4, notification1.build());
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    private void startForegroundWithNotification() {
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle("" + transitionType + " " + activityType)
                .build();


        startForeground(2, notification);
    }

    private void requestLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent == null) {
            return;
        }
        ++initialStepCount;
        float it = sensorEvent.values[sensorEvent.values.length - 1];
        Log.d("TAG", "Steps count: " + it);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @SuppressLint("StaticFieldLeak")
    private class DatabaseAsync extends AsyncTask<Object, Void, Void> {

        Training treino = new Training();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateOnly = dateFormat.format(currentDate);
            treino.user_id = currentUser.getUid();
            treino.distancia = SphericalUtil.computeLength(polylineOptions.getPoints());
            treino.n_passos = finalStepCount;
            treino.tipo = tipoTreinoEnum;
            treino.duracao = (int) (System.currentTimeMillis() - startTime);
            treino.data = dateOnly;
            ArrayList<pt.ipp.estg.fitcheck.Models.LatLng> list = new ArrayList<>();
            for(LatLng l : polylineOptions.getPoints()){
                pt.ipp.estg.fitcheck.Models.LatLng latLng = new pt.ipp.estg.fitcheck.Models.LatLng();
                latLng.setLongitude(l.longitude);
                latLng.setLatitude(l.latitude);
                list.add(latLng);
            }
            treino.percurso =  list;
        }

        @Override
        protected Void doInBackground(Object... voids) {

            if (treino != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Trainings").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(documentSnapshot.get("trainingList") != null){
                            UserTrainings trainings = new UserTrainings(new ArrayList((Collection) Objects.requireNonNull(documentSnapshot.get("trainingList"))));
                            Map<String, Object> trainingsMap = new HashMap<>();
                            TrainingResponse trainingResponse = new TrainingResponse();
                            trainingResponse.data = treino.data;
                            trainingResponse.distancia = treino.distancia;
                            trainingResponse.tipo = treino.tipo;
                            trainingResponse.duracao = treino.duracao;
                            trainingResponse.n_passos = treino.n_passos;
                            trainingResponse.treino_id = treino.treino_id;
                            trainingResponse.user_id = treino.user_id;
                            trainingResponse.v_max =  treino.v_max;
                            List<pt.ipp.estg.fitcheck.Models.LatLng> list = new ArrayList<>();
                            if(treino.percurso != null){
                                for(pt.ipp.estg.fitcheck.Models.LatLng latLng: treino.percurso){
                                    pt.ipp.estg.fitcheck.Models.LatLng latLng1 = new pt.ipp.estg.fitcheck.Models.LatLng();
                                    latLng1.setLatitude(latLng.latitude);
                                    latLng1.setLongitude(latLng.longitude);
                                    list.add(latLng1);
                                }
                            }

                            trainingResponse.percurso = list;

                            trainings.trainingList.add(trainingResponse);
                            trainingsMap.put("trainingList", trainings.trainingList);
                            db.collection("Trainings").document(currentUser.getUid()).update( trainingsMap);
                        }else{
                            TrainingResponse trainingResponse = new TrainingResponse();
                            trainingResponse.data = treino.data;
                            trainingResponse.distancia = treino.distancia;
                            trainingResponse.tipo = treino.tipo;
                            trainingResponse.duracao = treino.duracao;
                            trainingResponse.n_passos = treino.n_passos;
                            trainingResponse.treino_id = treino.treino_id;
                            trainingResponse.user_id = treino.user_id;
                            trainingResponse.v_max =  treino.v_max;
                            List<pt.ipp.estg.fitcheck.Models.LatLng> list = new ArrayList<>();
                            if(treino.percurso != null){
                                for(pt.ipp.estg.fitcheck.Models.LatLng latLng: treino.percurso){
                                    pt.ipp.estg.fitcheck.Models.LatLng latLng1 = new pt.ipp.estg.fitcheck.Models.LatLng();
                                    latLng1.setLatitude(latLng.latitude);
                                    latLng1.setLongitude(latLng.longitude);
                                    list.add(latLng1);
                                }
                            }

                            trainingResponse.percurso = list;
                            ArrayList<TrainingResponse> lt = new ArrayList<>();
                            lt.add(trainingResponse);

                            UserTrainings ts = new UserTrainings(lt);
                            db.collection("Trainings").document(currentUser.getUid()).set(ts);
                        }

                        db.collection("Users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user1 = new User(documentSnapshot.getString("username"),
                                        documentSnapshot.getString("email"),
                                        documentSnapshot.getString("gender"),
                                        documentSnapshot.getString("birthDate"),
                                        documentSnapshot.getLong("height").intValue(),
                                        documentSnapshot.getLong("weight").intValue(),
                                        documentSnapshot.getLong("dailyObjective").intValue(),
                                        documentSnapshot.getDouble("totalDistance").intValue());

                                user1.totalDistance += treino.distancia;

                                Map<String, Object> userMap = new HashMap<>();

                                userMap.put("totalDistance", user1.totalDistance);

                                db.collection("Users").document(currentUser.getUid()).update(userMap);
                            }
                        });
                        trainingDB.daoAccess().insertTreino(treino);
                    }
                });

            }


            return null;
        }

    }


    @Override
    public void onDestroy() {
        unregisterReceiver(mBatteryReceiver);
        Toast.makeText(this, "Treino Acabado", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }
}