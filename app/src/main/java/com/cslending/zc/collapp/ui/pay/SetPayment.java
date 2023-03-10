package com.cslending.zc.collapp.ui.pay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cslending.zc.collapp.Login;
import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetPayment extends AppCompatActivity {
    SQLiteDB db;

    String LoanQR = null;
    String lname;
    String amt;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LoanQR = extras.getString("scode");

            db = new SQLiteDB(getApplicationContext());
            db.read();

            Cursor result = db.getLoanfromQR(LoanQR);

            TextView borrower_loan = findViewById(R.id.borrower_loan);
            TextView borrower_amt = findViewById(R.id.borrower_amt);
            TextView borrower_name = findViewById(R.id.borrower_name);

            final Button btn_pay = findViewById(R.id.button_pay);
            final Button btn_nopay = findViewById(R.id.button_nopay);

            if (result.moveToFirst()) {
                lname = result.getString(0);
                amt = result.getString(1);

                borrower_name.setText(lname);
                borrower_amt.setText("P " + amt);
            }
            result.close();
            db.close();

            borrower_loan.setText("Loan #" + LoanQR);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dTFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date cal = Calendar.getInstance().getTime();
            String dt = dTFormat.format(cal);

            btn_pay.setOnClickListener(v -> {
                String message = "Are you sure with setting payment to 'PAY'?";
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("No", (dialog12, id) -> dialog12.dismiss())
                        .setNegativeButton("Yes", (dialog12, id) -> {
                            dialog12.dismiss();

                            db.open();
                            db.payYes(LoanQR, "0", dt, "", "");
                            db.close();

                            Intent intent = new Intent(this, SetPayment_Yes.class);
                            intent.putExtra("scode", LoanQR);
                            startActivity(intent);
                            finish();
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
            });

            btn_nopay.setOnClickListener(v -> {
                String message = "Are you sure with setting payment to 'NO PAY'?";
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("No", (dialog12, id) -> dialog12.dismiss())
                        .setNegativeButton("Yes", (dialog12, id) -> {
                            dialog12.dismiss();

                            db.open();
                            db.payNo(LoanQR, dt, "", "");
                            db.close();

                            Intent intent = new Intent(this, SetPayment_No.class);
                            intent.putExtra("scode", LoanQR);
                            startActivity(intent);
                            finish();
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
            });
        }
    }
}
