package com.cslending.zc.collapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

public class Init extends AppCompatActivity {

    SharedPreferences loginSP;
    String raw_permissions;

    boolean all_perm = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Check device permissions first!
        do {
            CheckPerm();
        } while (!all_perm);

        loginSP = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String loginStatus = loginSP.getString(getResources().getString(R.string.prefStatus), "");

        Date cal = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy", Locale.getDefault());
        String nowdate = df.format(cal);

        SharedPreferences getloginDate = getApplicationContext().getSharedPreferences("LoginDate", Context.MODE_PRIVATE);
        String datelogin = getloginDate.getString(getResources().getString(R.string.ldate),"");

        if (loginStatus.equals("loggedin")) {
            // Check if current date is not equal to the date last logged in.
            // Makes sure that the collector is always logged out after every end of the day.
            if (!nowdate.equals(datelogin)) {
                final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getResources().getString(R.string.prefStatus),"logout");
                editor.apply();

                Toast.makeText(this, "Auto Logged-Out", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, Login.class));
                finish();
            } else {
                startActivity(new Intent(this, Home.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    public void CheckPerm() {
        all_perm = true;
        int permissionsCode = 1;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            all_perm = false;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                all_perm = false;
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                all_perm = false;
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
            all_perm = false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            all_perm = false;
        }

        // Check Bluetooth (Android API >= API 31) Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                all_perm = false;
            }
        }

        if (!all_perm) {
            StringJoiner perms = new StringJoiner("," ,"", "");

            raw_permissions = perms.add(Manifest.permission.CAMERA)
                    .add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .add(Manifest.permission.BLUETOOTH_ADMIN)
                    .add(Manifest.permission.BLUETOOTH)
                    .toString();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                raw_permissions = perms.add(Manifest.permission.BLUETOOTH_CONNECT)
                        .add(Manifest.permission.BLUETOOTH_SCAN)
                        .toString();
            }

            String[] permissions = raw_permissions.split(",");
            ActivityCompat.requestPermissions(this, permissions, permissionsCode);
        }
    }
}
