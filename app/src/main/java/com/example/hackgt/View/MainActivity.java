package com.example.hackgt.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hackgt.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.FitnessLocal;
import com.google.android.gms.fitness.LocalRecordingClient;
import com.google.android.gms.fitness.data.LocalDataType;
import com.google.android.gms.fitness.request.LocalDataReadRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static final int ACTIVITY_REQUEST = 1;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        int hasMinPlayServices = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (hasMinPlayServices != ConnectionResult.SUCCESS) {
            // Prompt user to update their device's Google Play services app and return
            Toast.makeText(this, "Update device Google Play services", Toast.LENGTH_SHORT).show();
        } else {
            LocalRecordingClient fitnessClient = FitnessLocal.getLocalRecordingClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                String[] permissions = {Manifest.permission.ACTIVITY_RECOGNITION};
                ActivityCompat.requestPermissions(this, permissions, ACTIVITY_REQUEST);
                return;
            }
            else{
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
                fitnessClient.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener(b -> {
                            Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(b -> {
                            Toast.makeText(this, "Failed to subscribe!", Toast.LENGTH_SHORT).show();
                        });

                ZonedDateTime endTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
                ZonedDateTime startTime = endTime.minusWeeks(1);
                LocalDataReadRequest readRequest = new LocalDataReadRequest.Builder()
                        .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                        .build();

                fitnessClient.readData(readRequest)
                        .addOnSuccessListener(r -> {
                            for(int x : r.getBuckets().stream().flatMap()){

                            }
                        })
                        .addOnFailureListener(r -> {
                            Toast.makeText(this, "Failed to read data!", Toast.LENGTH_SHORT).show();
                        });

            }
        }

    }
}