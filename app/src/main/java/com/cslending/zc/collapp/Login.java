package com.cslending.zc.collapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Login extends AppCompatActivity {

    SQLiteDB db;

    SharedPreferences loginSP;
    SharedPreferences collSP;
    SharedPreferences dateSP;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginSP = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        initializeDB();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("No", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Yes", (dialog, id) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void initializeDB() {
        db = new SQLiteDB(getApplicationContext());
        db.read();

        Cursor data = db.getUnsyncedData();
        if (data.getCount() == 0) {
            db.regenDB();
        }
        db.close();

        LoginProcess();
    }

    public void LoginProcess() {
        TextInputLayout ueditbox = findViewById(R.id.til_username);
        TextInputLayout peditbox = findViewById(R.id.til_password);
        EditText username = findViewById(R.id.ti_username);
        EditText password = findViewById(R.id.ti_password);

        Button signin = findViewById(R.id.button_signin);

        username.setOnFocusChangeListener((v, hasFocus) -> ueditbox.setError(""));
        password.setOnFocusChangeListener((v, hasFocus) -> peditbox.setError(""));

        signin.setOnClickListener(v -> {
            ueditbox.setError("");
            peditbox.setError("");
            String user = username.getText().toString();
            String pass = password.getText().toString();

            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                if (TextUtils.isEmpty(user)) {
                    ueditbox.setError("Username Field is empty!");
                }
                if (TextUtils.isEmpty(pass)) {
                    peditbox.setError("Password Field is empty!");
                }

                Toast.makeText(getApplicationContext(),"Please check your user credentials and try again!",Toast.LENGTH_LONG).show();
            } else {
                Login2Server(user, pass);
            }
        });
    }

    public void Login2Server(String username, String password) {
        AlertDialog.Builder libuilder = new AlertDialog.Builder(this);
        libuilder.setMessage("Please wait...")
                .setCancelable(false);
        final AlertDialog lialert = libuilder.create();
        lialert.show();

        StringRequest request = new StringRequest(Request.Method.POST, BuildConfig.SERVER_URL + "post-login.php", response -> {
            lialert.dismiss();

            if (response.contains("Success")) {
                String cid = response.replaceAll("\\D+", "");

                SharedPreferences.Editor editor = loginSP.edit();
                editor.putString(getResources().getString(R.string.prefStatus), "loggedin");
                editor.apply();

                collSP = getSharedPreferences("ColInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = collSP.edit();
                editor2.putString(getResources().getString(R.string.collector), cid);
                editor2.apply();

                Date cal = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy", Locale.getDefault());
                String datelogin = df.format(cal);

                dateSP = getSharedPreferences("LoginDate", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor3 = dateSP.edit();
                editor3.putString(getResources().getString(R.string.ldate), datelogin);
                editor3.apply();

                startActivity(new Intent(this, Home.class));
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(response)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            }
        }, error -> {
            lialert.dismiss();

            AlertDialog.Builder lerbuilder = new AlertDialog.Builder(Login.this);
            lerbuilder.setMessage("LOGIN FAILED!\n\nDetails:\n" + error)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog leralert = lerbuilder.create();
            leralert.show();
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String,String> param = new HashMap<>();
                param.put("login_u",username);
                param.put("login_p",password);
                param.put("login_version", BuildConfig.VERSION_NAME);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getmInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
