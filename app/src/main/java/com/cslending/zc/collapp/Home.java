package com.cslending.zc.collapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.cslending.zc.collapp.databinding.ActivityHomeBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Home extends AppCompatActivity {

    SQLiteDB db;
    SQLiteDB sqlite_obj_setcol;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    List<String> list3 = new ArrayList<>();
    List<String> list4 = new ArrayList<>();
    List<String> list5 = new ArrayList<>();
    List<String> list6 = new ArrayList<>();
    List<String> list7 = new ArrayList<>();

    String line = null;
    String result = null;
    JSONObject jo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        db = new SQLiteDB(getApplicationContext());
        sqlite_obj_setcol = new SQLiteDB(getApplicationContext());
        super.onCreate(savedInstanceState);

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_worksheet, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        db.read();
        Cursor result = db.queryOfflineData();
        if (result.getCount() == 0) {
            Cursor result2 = db.getUnsyncedData();
            if (result2.getCount() == 0) {
                downloadOfflineData();
            } else {
                syncOffline2Cloud();
                db.regenDB();
            }
        }
        db.close();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("No", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Yes", (dialog, id) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(this.getSupportActionBar()).hide();
        syncOffline2Cloud();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void downloadOfflineData() {
        AlertDialog.Builder csbuilder = new AlertDialog.Builder(Home.this);
        csbuilder.setMessage("Updating Database...")
                .setCancelable(false);
        final AlertDialog csalert = csbuilder.create();
        csalert.show();

        @SuppressWarnings("deprecation")
        @SuppressLint("StaticFieldLeak")
        class GetJSON extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(Void... voids) {
                SQLiteDB sqlite_obj_download = new SQLiteDB(getApplicationContext());

                final String KEY_col = "set_col";

                try {
                    SharedPreferences ret = getApplicationContext().getSharedPreferences("ColInfo", Context.MODE_PRIVATE);
                    final String send_col_id = ret.getString(getResources().getString(R.string.collector),"");

                    URL url = new URL(BuildConfig.SERVER_URL + "json-collections.php");

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setReadTimeout(300000);
                    con.setConnectTimeout(300000);
                    con.setRequestMethod("POST");
                    con.setDoInput(true);
                    con.setDoOutput(true);

                    HashMap<String,String> send_col = new HashMap<>();
                    send_col.put(KEY_col, send_col_id);

                    OutputStream os = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(getPostDataString(send_col));
                    writer.flush();
                    writer.close();
                    os.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    result = sb.toString();

                    JSONArray ja = new JSONArray(result);

                    for (int i=0; i<ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        list1.add(jo.getString("id"));
                        list2.add(jo.getString("lastname"));
                        list3.add(jo.getString("cn"));
                        list4.add(jo.getString("amort"));
                        list5.add(jo.getString("amt"));
                        list6.add(jo.getString("paid"));
                        list7.add(jo.getString("gcode"));
                    }

                    sqlite_obj_download.open();
                    for (int i=0; i<list1.size(); i++) {
                        sqlite_obj_download.SyncOfflineDB(list1.get(i), list2.get(i), list3.get(i), list4.get(i), list5.get(i), list6.get(i), list7.get(i));
                    }
                    sqlite_obj_download.close();

                    runOnUiThread(() -> Toast.makeText(Home.this, "Offline Data Downloaded!", Toast.LENGTH_SHORT).show());

                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for(Map.Entry<String, String> entry : params.entrySet()){
                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                return result.toString();
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();

        csalert.dismiss();
    }

    public void syncOffline2Cloud() {
        AlertDialog.Builder stsbuilder = new AlertDialog.Builder(Home.this);
        stsbuilder.setMessage("Syncing...")
                .setCancelable(false);
        AlertDialog stsalert = stsbuilder.create();
        stsalert.show();

        db.read();
        Cursor result = db.getUnsyncedData();
        while (result.moveToNext()) {
            doSync(result.getString(0), result.getString(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5));
        }
        result.close();
        db.close();

        stsalert.dismiss();
    }

    public void doSync(String loan, String amt, String paid, String dt, String rem, String img) {
        SharedPreferences ret2 = Home.this.getSharedPreferences("ColInfo", Context.MODE_PRIVATE);
        final String collector = ret2.getString(getResources().getString(R.string.collector),"");

        final String coll_loan = String.valueOf(loan);
        final String coll_amt = String.valueOf(amt);
        final String coll_paid = String.valueOf(paid);
        final String coll_dt = String.valueOf(dt);
        final String coll_rem = String.valueOf(rem);
        final String coll_img = String.valueOf(img);

        StringRequest request = new StringRequest(Request.Method.POST, BuildConfig.SERVER_URL + "post-collections.php", response -> {
            if (response.contains("Success!")) {
                sqlite_obj_setcol.open();
                sqlite_obj_setcol.setPaySent(coll_loan);
                sqlite_obj_setcol.close();
            }
        }, error -> Toast.makeText(Home.this, "Could not sync data. Weak or no connection!", Toast.LENGTH_SHORT).show()){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String,String> param = new HashMap<>();
                param.put("id", coll_loan);
                param.put("amt", coll_amt);
                param.put("paid", coll_paid);
                param.put("coll", collector);
                param.put("rem", coll_rem);
                param.put("img", coll_img);
                param.put("updated", coll_dt);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(8000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getmInstance(Home.this).addToRequestQueue(request);
    }
}