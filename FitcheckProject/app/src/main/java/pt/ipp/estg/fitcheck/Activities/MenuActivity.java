package pt.ipp.estg.fitcheck.Activities;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayDeque;
import java.util.Deque;

import pt.ipp.estg.fitcheck.DataBases.DailyObjectiveDB;
import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Fragments.HomeFragment;
import pt.ipp.estg.fitcheck.Fragments.ManualTrainingFragment;
import pt.ipp.estg.fitcheck.Fragments.ProfileFragment;
import pt.ipp.estg.fitcheck.Fragments.RankingFragment;
import pt.ipp.estg.fitcheck.Fragments.StatisticsFragment;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;
import pt.ipp.estg.fitcheck.Threads.StartCountStepsThread;


public class MenuActivity extends AppCompatActivity implements FragmentChange {

    private FirebaseAuth auth;

    private BottomNavigationView bottomNav;

    private DailyObjectiveDB dailyObjectiveDB;

    private FirebaseUser user;

    private FirebaseFirestore db;

    private Intent intentArgs;

    private Fragment atualFragment;

    Deque<Integer> integerDeque = new ArrayDeque<>(4);
    boolean flag = true;


    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");

        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        if (!isForegroundServiceRunning(CountStepsForegroundService.class)) {
            new StartCountStepsThread(MenuActivity.this).run();
        }

        bottomNav = findViewById(R.id.bottomNavView);


        db = FirebaseFirestore.getInstance();

        dailyObjectiveDB = Room.databaseBuilder(getApplicationContext(), DailyObjectiveDB.class, "dailyObjectiveDB").build();

        integerDeque.push(R.id.ic_home);
        bottomNav.setSelectedItemId(R.id.ic_home);
        exchangeFrag(new HomeFragment());

        String fragment = getIntent().getStringExtra("fragment");
        Training treino = null;

        if (getIntent().getExtras() != null) {
            treino = (Training) getIntent().getExtras().get("treino");
        }

        if (fragment != null) {
            if (fragment.equals("HomeFragment")) {
                intentArgs = null;
                bottomNav.setSelectedItemId(R.id.ic_home);
            }
        } else if (treino != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(4);
            intentArgs = getIntent();
            ManualTrainingFragment fr = new ManualTrainingFragment();
            Bundle bd = new Bundle();
            bd.putSerializable("treino", treino);
            fr.setArguments(bd);
            exchangeFrag(fr);
        } else {
            intentArgs = null;
        }

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (integerDeque.contains(id)) {
                    if (id == R.id.ic_home) {
                        if (integerDeque.size() != 1) {
                            if (flag) {
                                integerDeque.addFirst(R.id.ic_home);
                                flag = false;
                            }
                        }
                    }

                    integerDeque.remove(id);
                }

                integerDeque.push(id);

                exchangeFrag(getFragment(item.getItemId()));

                return true;
            }
        });
    }

    private Fragment getFragment(int itemId) {
        switch (itemId) {
            case R.id.ic_home:
                bottomNav.getMenu().getItem(0).setChecked(true);
                return new HomeFragment();
            case R.id.ic_ranking:
                bottomNav.getMenu().getItem(1).setChecked(true);
                return new RankingFragment();
            case R.id.ic_statistics:
                bottomNav.getMenu().getItem(2).setChecked(true);
                return new StatisticsFragment();
            case R.id.ic_profile:
                bottomNav.getMenu().getItem(3).setChecked(true);
                return new ProfileFragment(MenuActivity.this);
        }
        bottomNav.getMenu().getItem(0).setChecked(true);
        return new HomeFragment();
    }

    @Override
    public void exchangeFrag(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction tr = fragmentManager.beginTransaction();
        atualFragment = fragment;
        tr.replace(R.id.ContainerFragment, fragment);
        tr.addToBackStack(null);
        tr.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String fragment = intent.getStringExtra("fragment");
        Training treino = (Training) intent.getExtras().get("treino");

        if (fragment != null) {
            if (fragment.equals("HomeFragment")) {
                intentArgs = null;
                bottomNav.setSelectedItemId(R.id.ic_home);
            }
        } else if (treino != null) {

            intentArgs = intent;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(4);
            ManualTrainingFragment fr = new ManualTrainingFragment();
            Bundle bd = new Bundle();
            bd.putSerializable("treino", treino);
            fr.setArguments(bd);
            exchangeFrag(fr);
        } else {
            intentArgs = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, CountStepsForegroundService.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    startService(intent);
                } else {
                    Toast.makeText(this, "Erro ao correr o serviços foreground.", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Permissão recusada.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = auth.getCurrentUser();


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
            startActivity(intent);
            finish();
        } else {

            Training treino = null;
            if (intentArgs != null) {
                treino = (Training) intentArgs.getExtras().get("treino");
            }

            if (treino != null) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(4);
                ManualTrainingFragment fr = new ManualTrainingFragment();
                Bundle bd = new Bundle();
                bd.putSerializable("treino", treino);
                fr.setArguments(bd);
                exchangeFrag(fr);
                bottomNav.setVisibility(View.GONE);
                intentArgs = null;
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setVisibleBottomNav() {
        bottomNav.setVisibility(View.VISIBLE);
    }

    public void ItemId(int id) {
        bottomNav.setSelectedItemId(id);
    }


    @Override
    public void onBackPressed() {
        integerDeque.pop();

        if (!integerDeque.isEmpty()) {
            exchangeFrag(getFragment(integerDeque.peek()));
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isForegroundServiceRunning(Class<?> foregroundService) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (foregroundService.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
