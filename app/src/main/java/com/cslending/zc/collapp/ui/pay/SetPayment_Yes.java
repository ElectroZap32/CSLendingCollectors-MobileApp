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
import android.view.View;
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
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.google.android.material.card.MaterialCardView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class SetPayment_Yes extends AppCompatActivity {
    SQLiteDB db;

    String currentPhotoPath;
    int REQUEST_IMAGE_CAPTURE = 100;

    String LoanQR = null;
    String lname;
    String amt;
    String pay;
    String rem;
    String remark;
    String amount;
    String image;

    private final Locale locale = new Locale("id", "PH");
    private final DateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", locale);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment_yes);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LoanQR = extras.getString("scode");

            db = new SQLiteDB(getApplicationContext());
            db.read();

            Cursor result = db.getLoanfromQR(LoanQR);

            TextView borrower_loan = findViewById(R.id.borrower_loan);
            TextView borrower_amt = findViewById(R.id.borrower_amt);
            TextView borrower_name = findViewById(R.id.borrower_name);

            EditText input_pay = findViewById(R.id.pay_amount);
            EditText input_rem = findViewById(R.id.remarks);

            ImageView img = findViewById(R.id.pay_image);

            MaterialCardView card_input = findViewById(R.id.card_borrower_option_input);
            MaterialCardView card_image = findViewById(R.id.card_borrower_option_image);
            MaterialCardView card_button = findViewById(R.id.card_borrower_option_pay);

            final Button btn_pay = findViewById(R.id.button_pay);
            final Button btn_image = findViewById(R.id.button_image);
            final Button btn_print = findViewById(R.id.button_print);
            final Button btn_save = findViewById(R.id.button_save);

            if (result.moveToFirst()) {
                lname = result.getString(0);
                amt = result.getString(1);
                remark = result.getString(3);
                amount = result.getString(4);
                image = result.getString(5);

                borrower_name.setText(lname);
                borrower_amt.setText("P " + amt);
            }
            result.close();
            db.close();

            borrower_loan.setText("Loan #" + LoanQR);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dTFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date cal = Calendar.getInstance().getTime();
            String dt = dTFormat.format(cal);

            if (Objects.equals(remark, "")) {
                card_input.setVisibility(View.VISIBLE);
                card_image.setVisibility(View.GONE);
                card_button.setVisibility(View.GONE);
            } else {
                input_pay.setFocusable(false);
                input_pay.setEnabled(false);
                input_pay.setCursorVisible(false);
                input_pay.setKeyListener(null);

                input_rem.setFocusable(false);
                input_rem.setEnabled(false);
                input_rem.setCursorVisible(false);
                input_rem.setKeyListener(null);

                if (Objects.equals(amount, "0")) {
                    card_input.setVisibility(View.GONE);
                    card_image.setVisibility(View.VISIBLE);
                    card_button.setVisibility(View.VISIBLE);

                    btn_print.setVisibility(View.VISIBLE);
                    btn_pay.setVisibility(View.VISIBLE);
                } else {
                    if (Objects.equals(image, "")) {
                        card_input.setVisibility(View.GONE);

                        btn_pay.setVisibility(View.VISIBLE);
                        btn_print.setVisibility(View.VISIBLE);
                    } else {
                        card_input.setVisibility(View.GONE);
                        card_image.setVisibility(View.GONE);

                        btn_pay.setVisibility(View.GONE);
                        btn_print.setVisibility(View.VISIBLE);
                    }
                }
            }

            btn_save.setOnClickListener(v -> {
                pay = input_pay.getText().toString();
                rem = input_rem.getText().toString();

                if (TextUtils.isEmpty(pay) || TextUtils.isEmpty(rem)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Please input an amount and remarks when clicking 'Pay'!")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog1, id1) -> dialog1.dismiss());
                    AlertDialog alert1 = builder1.create();
                    alert1.show();
                } else {
                    AlertDialog.Builder confirmbuilder1 = new AlertDialog.Builder(this);
                    confirmbuilder1.setMessage("Confirmation of Clicking 'Pay'?\nAmount: P" + pay)
                            .setCancelable(false)
                            .setPositiveButton("No", (dialog, id) -> dialog.dismiss())
                            .setNegativeButton("Yes", (dialog, id) -> {
                                db.open();
                                db.payYes(LoanQR, pay, dt, rem, "");
                                db.close();

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                builder1.setMessage("Payment Recorded.\n\nPrinting Receipt!")
                                        .setCancelable(false)
                                        .setPositiveButton("Finish", (dialog12, id12) -> {
                                            dialog12.dismiss();

                                            input_pay.setFocusable(false);
                                            input_pay.setEnabled(false);
                                            input_pay.setCursorVisible(false);
                                            input_pay.setKeyListener(null);

                                            input_rem.setFocusable(false);
                                            input_rem.setEnabled(false);
                                            input_rem.setCursorVisible(false);
                                            input_rem.setKeyListener(null);

                                            card_input.setVisibility(View.GONE);
                                            card_image.setVisibility(View.VISIBLE);
                                            card_button.setVisibility(View.VISIBLE);

                                            btn_pay.setVisibility(View.VISIBLE);
                                            btn_print.setVisibility(View.VISIBLE);

                                            AlertDialog.Builder printbuilder = new AlertDialog.Builder(this);
                                            printbuilder.setMessage("START PRINTING RECEIPT...\n\n(Make sure that BT PRINTER is ON!)")
                                                    .setCancelable(false)
                                                    .setNegativeButton("Print", (pdialog, pid) -> {
                                                        try {
                                                            BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                                                            if (connection != null) {
                                                                if (pay == null) { pay = amount; }
                                                                int payment = Integer.parseInt(pay);
                                                                DecimalFormat decformat = new DecimalFormat("0.00");
                                                                EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);
                                                                final String text = "[C]<b><font size='tall'>CS LENDING</font></b>\n" +
                                                                        "[C]For concerns, please contact us.\n" +
                                                                        "[C]09280446486/09976977844\n" +
                                                                        "[C]VAT REG TIN: 009-493-347-000\n" +
                                                                        "[L]\n" +
                                                                        "[C]" + df.format(new Date()) + "\n" +
                                                                        "[C]================================\n" +
                                                                        "[C]<b height='10'>" + lname + "</b>\n" +
                                                                        "[C]<b>Loan #" + LoanQR + "</b>\n" +
                                                                        "[C]--------------------------------\n" +
                                                                        "[C]<b height='15'><font size='tall'>P " + decformat.format(payment) + "</font></b>\n" +
                                                                        "[C]--------------------------------\n" +
                                                                        "[C]Payment Received!\n" +
                                                                        "[L]\n" +
                                                                        "[C]Complaints, text 09280446486.\n" +
                                                                        "[C]Thank You!\n";
                                                                printer.printFormattedText(text);
                                                                Thread.sleep(2000);
                                                                printer.disconnectPrinter();

                                                                AlertDialog.Builder builder11 = new AlertDialog.Builder(this);
                                                                builder11.setMessage("Do you want to print receipt again?")
                                                                        .setCancelable(false)
                                                                        .setPositiveButton("No", (dialog123, id123) -> {
                                                                            dialog123.dismiss();
                                                                            AlertDialog.Builder donebuilder = new AlertDialog.Builder(this);
                                                                            donebuilder.setMessage("Receipt Printed!")
                                                                                    .setCancelable(false)
                                                                                    .setPositiveButton("Confirm Receipt", (ddialog, did) -> ddialog.dismiss());
                                                                            AlertDialog dalert = donebuilder.create();
                                                                            dalert.show();
                                                                        })
                                                                        .setNegativeButton("Yes", (dialog123, id123) -> {
                                                                            try {
                                                                                EscPosPrinter printer2 = new EscPosPrinter(connection, 203, 48f, 32);
                                                                                printer2.printFormattedText(text);
                                                                                Thread.sleep(2000);
                                                                                printer2.disconnectPrinter();
                                                                                AlertDialog.Builder donebuilder = new AlertDialog.Builder(this);
                                                                                donebuilder.setMessage("Receipt Printed!")
                                                                                        .setCancelable(false)
                                                                                        .setPositiveButton("Finish", (ddialog, did) -> ddialog.dismiss());
                                                                                AlertDialog dalert = donebuilder.create();
                                                                                dalert.show();
                                                                            } catch (EscPosConnectionException | EscPosParserException | EscPosEncodingException | EscPosBarcodeException | InterruptedException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        });
                                                                AlertDialog alert1 = builder11.create();
                                                                alert1.show();
                                                            } else {
                                                                AlertDialog.Builder builder11 = new AlertDialog.Builder(this);
                                                                builder11.setMessage("Cannot print receipt!\n\nPlease check if BT Printer is ON or CONNECTED.")
                                                                        .setCancelable(false)
                                                                        .setPositiveButton("Finish", (dialog123, id123) -> dialog123.dismiss());
                                                                AlertDialog alert1 = builder11.create();
                                                                alert1.show();
                                                            }
                                                        } catch (Exception e) {
                                                            AlertDialog.Builder builder11 = new AlertDialog.Builder(this);
                                                            builder11.setMessage("There was a problem with printing the receipt!")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("OK", (dialog123, id123) -> dialog123.dismiss());
                                                            AlertDialog alert1 = builder11.create();
                                                            alert1.show();
                                                        }
                                                    });
                                            AlertDialog printalert = printbuilder.create();
                                            printalert.show();
                                        });
                                AlertDialog alert1 = builder1.create();
                                alert1.show();
                            });
                    AlertDialog yesalert = confirmbuilder1.create();
                    yesalert.show();
                }
            });

            btn_print.setOnClickListener(v -> {
                AlertDialog.Builder confirmbuilder1 = new AlertDialog.Builder(this);
                confirmbuilder1.setMessage("Confirm Printing of Receipt?")
                        .setCancelable(false)
                        .setPositiveButton("No", (dialog, id) -> dialog.dismiss())
                        .setNegativeButton("Yes", (dialog, id) -> {
                            AlertDialog.Builder printbuilder = new AlertDialog.Builder(this);
                            printbuilder.setMessage("START PRINTING RECEIPT...\n\n(Make sure that BT PRINTER is ON!)")
                                    .setCancelable(false)
                                    .setNegativeButton("Print", (pdialog, pid) -> {
                                        try {
                                            BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                                            if (connection != null) {
                                                if (pay == null) { pay = amount; }
                                                int payment = Integer.parseInt(pay);
                                                DecimalFormat decformat = new DecimalFormat("0.00");
                                                EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);
                                                final String text = "[C]<b><font size='tall'>CS LENDING</font></b>\n" +
                                                        "[C]For concerns, please contact us.\n" +
                                                        "[C]09280446486/09976977844\n" +
                                                        "[C]VAT REG TIN: 009-493-347-000\n" +
                                                        "[L]\n" +
                                                        "[C]" + df.format(new Date()) + "\n" +
                                                        "[C]================================\n" +
                                                        "[C]<b height='10'>" + lname + "</b>\n" +
                                                        "[C]<b>Loan #" + LoanQR + "</b>\n" +
                                                        "[C]--------------------------------\n" +
                                                        "[C]<b height='15'><font size='tall'>P " + decformat.format(payment) + "</font></b>\n" +
                                                        "[C]--------------------------------\n" +
                                                        "[C]Payment Received!\n" +
                                                        "[L]\n" +
                                                        "[C]Complaints, text 09280446486.\n" +
                                                        "[C]Thank You!\n";
                                                printer.printFormattedText(text);
                                                Thread.sleep(2000);
                                                printer.disconnectPrinter();

                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                                builder1.setMessage("Do you want to print receipt again?")
                                                        .setCancelable(false)
                                                        .setPositiveButton("No", (dialog12, id12) -> {
                                                            dialog12.dismiss();
                                                            AlertDialog.Builder donebuilder = new AlertDialog.Builder(this);
                                                            donebuilder.setMessage("Receipt Printed!")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("Confirm Receipt", (ddialog, did) -> ddialog.dismiss());
                                                            AlertDialog dalert = donebuilder.create();
                                                            dalert.show();
                                                        })
                                                        .setNegativeButton("Yes", (dialog12, id12) -> {
                                                            try {
                                                                EscPosPrinter printer2 = new EscPosPrinter(connection, 203, 48f, 32);
                                                                printer2.printFormattedText(text);
                                                                Thread.sleep(2000);
                                                                printer2.disconnectPrinter();
                                                                AlertDialog.Builder donebuilder = new AlertDialog.Builder(this);
                                                                donebuilder.setMessage("Receipt Printed!")
                                                                        .setCancelable(false)
                                                                        .setPositiveButton("Finish", (ddialog, did) -> ddialog.dismiss());
                                                                AlertDialog dalert = donebuilder.create();
                                                                dalert.show();
                                                            } catch (EscPosConnectionException | EscPosParserException | EscPosEncodingException | EscPosBarcodeException | InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        });
                                                AlertDialog alert1 = builder1.create();
                                                alert1.show();
                                            } else {
                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                                builder1.setMessage("Cannot print receipt!\n\nPlease check if BT Printer is ON or CONNECTED.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Finish", (dialog12, id12) -> dialog12.dismiss());
                                                AlertDialog alert1 = builder1.create();
                                                alert1.show();
                                            }
                                        } catch (Exception e) {
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                            builder1.setMessage("There was a problem with printing the receipt!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK", (dialog12, id12) -> dialog12.dismiss());
                                            AlertDialog alert1 = builder1.create();
                                            alert1.show();
                                        }
                                    });
                            AlertDialog printalert = printbuilder.create();
                            printalert.show();
                        });
                AlertDialog yesalert = confirmbuilder1.create();
                yesalert.show();
            });

            btn_pay.setOnClickListener(v -> {
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
                    confirmbuilder1.setMessage("Confirmation submission of Image / Receipt?")
                            .setCancelable(false)
                            .setPositiveButton("No", (dialog, id) -> dialog.dismiss())
                            .setNegativeButton("Yes", (dialog, id) -> {
                                db.open();
                                db.payYesImg(LoanQR, data);
                                db.close();

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                                builder1.setMessage("Image Recorded. Thank you!")
                                        .setCancelable(false)
                                        .setPositiveButton("Finish", (dialog12, id12) -> {
                                            dialog12.dismiss();
                                            finish();
                                        });
                                AlertDialog alert1 = builder1.create();
                                alert1.show();
                            });
                    AlertDialog yesalert = confirmbuilder1.create();
                    yesalert.show();
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