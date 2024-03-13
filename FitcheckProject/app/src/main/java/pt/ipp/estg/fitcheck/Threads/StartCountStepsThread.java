package pt.ipp.estg.fitcheck.Threads;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.BuildConfig;
import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;

public class StartCountStepsThread extends Thread {

    private Context context;


    public StartCountStepsThread(Context context) {
        this.context = context;
    }


    @Override
    public void run() {
        boolean done = false;

        while (!done) {
            try {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(context, CountStepsForegroundService.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(intent);
                    } else {
                        Toast.makeText(context, "Erro ao correr o serviços foreground.", Toast.LENGTH_LONG).show();
                    }

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(((MenuActivity) context),
                                new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION},
                                1);

                    }
                }

                done = true;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

    }


}
