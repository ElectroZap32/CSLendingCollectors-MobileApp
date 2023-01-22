package com.cslending.zc.collapp.ui.pay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("deprecation")
public class SetPayment_No extends AppCompatActivity {
    SQLiteDB db;

    String currentPhotoPath;
    int REQUEST_IMAGE_CAPTURE = 100;

    String LoanQR = null;
    String lname;
    String amt;
    String rem;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment_no);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LoanQR = extras.getString("scode");

            db = new SQLiteDB(getApplicationContext());
            db.read();

            Cursor result = db.getLoanfromQR(LoanQR);

            TextView borrower_loan = findViewById(R.id.borrower_loan);
            TextView borrower_amt = findViewById(R.id.borrower_amt);
            TextView borrower_name = findViewById(R.id.borrower_name);

            EditText input_rem = findViewById(R.id.remarks);

            ImageView img = findViewById(R.id.pay_image);

            final Button btn_nopay = findViewById(R.id.button_nopay);
            final Button btn_image = findViewById(R.id.button_image);

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

            btn_nopay.setOnClickListener(v -> {
                rem = input_rem.getText().toString();

                if (TextUtils.isEmpty(rem)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Please provide Comments / Remarks for No Pay!")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog1, id1) -> dialog1.dismiss());
                    AlertDialog alert1 = builder1.create();
                    alert1.show();
                } else {
                    if (img.getDrawable() == null) {
                        AlertDialog.Builder confirmbuilder1 = new AlertDialog.Builder(this);
                        confirmbuilder1.setMessage("Please 'Take Picture' first before submitting!")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
                        AlertDialog noalert = confirmbuilder1.create();
                        noalert.show();
                    } else {
                        Bitmap bm =((BitmapDrawable)img.getDrawable()).getBitmap();
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 25, bao);

                        byte[] rdata = bao.toByteArray();
                        String data = Base64.encodeToString(rdata, Base64.NO_WRAP);

                        AlertDialog.Builder confirmbuilder1 = new AlertDialog.Builder(this);
                        confirmbuilder1.setMessage("Confirmation of Clicking 'No Pay'?")
                                .setCancelable(false)
                                .setPositiveButton("No", (dialog, id) -> dialog.dismiss())
                                .setNegativeButton("Yes", (dialog, id) -> {
                                    db.open();
                                    db.payNo(LoanQR, dt, rem, data);
                                    db.close();

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                    builder1.setMessage("Action Recorded. Thank you!")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", (dialog13, id13) -> {
                                                dialog13.dismiss();
                                                finish();
                                            });
                                    AlertDialog alert1 = builder1.create();
                                    alert1.show();
                                });
                        AlertDialog noalert = confirmbuilder1.create();
                        noalert.show();
                    }
                }
            });

            btn_image.setOnClickListener(v -> captureImage());
        }
    }

    void captureImage() {
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView img = findViewById(R.id.pay_image);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            img.setImageURI(contentUri);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.cslending.zc.collapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}