package com.cslending.zc.collapp.home;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.cslending.zc.collapp.databinding.FragmentHomeBinding;
import com.cslending.zc.collapp.ui.pay.SetPayment;
import com.cslending.zc.collapp.ui.pay.SetPayment_No;
import com.cslending.zc.collapp.ui.pay.SetPayment_Yes;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Objects;

public class HomeFragment extends Fragment {

    SQLiteDB db;

    private FragmentHomeBinding binding;
    DecoratedBarcodeView barcodeView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = new SQLiteDB(getActivity());

        barcodeView = root.findViewById(R.id.barcode_view);
        barcodeView.decodeContinuous(result -> {
            barcodeView.pauseAndWait();

            String code = result.getText();

            db.read();
            Cursor result2 = db.getLoanfromQR(code);

            if (result2.getCount() == 0) {
                Toast.makeText(getActivity(), "No Loans Found!", Toast.LENGTH_SHORT).show();
            } else {
                if (result2.moveToFirst()) {
                    String loancode = result2.getString(0);
                    String paid = result2.getString(1);
                    String rem = result2.getString(2);

                    if (Objects.equals(paid, "-")) {
                        Intent intent = new Intent(getActivity(), SetPayment.class);
                        intent.putExtra("scode", loancode);
                        startActivity(intent);
                    } else if (Objects.equals(paid, "Yes")) {
                        if (Objects.equals(rem, "")) {
                            String message = "Loan #" + loancode + " payment is set to 'PAY' but not yet processed. Proceed?";
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                            builder1.setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog12, id12) -> {
                                        dialog12.dismiss();

                                        Intent intent = new Intent(getActivity(), SetPayment_Yes.class);
                                        intent.putExtra("scode", loancode);
                                        startActivity(intent);
                                    });
                            AlertDialog alert1 = builder1.create();
                            alert1.show();
                        } else {
                            String message = "Loan #" + loancode + " payment was already set to 'PAY'. Do you want to print receipt only?";
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                            builder1.setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("No", (dialog12, id12) -> dialog12.dismiss())
                                    .setNegativeButton("Yes", (dialog12, id12) -> {
                                        dialog12.dismiss();

                                        Intent intent = new Intent(getActivity(), SetPayment_Yes.class);
                                        intent.putExtra("scode", loancode);
                                        startActivity(intent);
                                    });
                            AlertDialog alert1 = builder1.create();
                            alert1.show();
                        }
                    } else if (Objects.equals(paid, "No")) {
                        if (Objects.equals(rem, "")) {
                            String message = "Loan #" + loancode + " payment is set to 'NO PAY' but not yet processed. Proceed?";
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                            builder1.setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog12, id12) -> {
                                        dialog12.dismiss();

                                        Intent intent = new Intent(getActivity(), SetPayment_No.class);
                                        intent.putExtra("scode", loancode);
                                        startActivity(intent);
                                    });
                            AlertDialog alert1 = builder1.create();
                            alert1.show();
                        } else {
                            String message = "Loan #" + loancode + " payment was already set to 'NO PAY'.";
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                            builder1.setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog12, id12) -> dialog12.dismiss());
                            AlertDialog alert1 = builder1.create();
                            alert1.show();
                        }
                    }
                }
            }
            result2.close();
            db.close();
        });

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pauseAndWait();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
        db.read();
        ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null) { supportActionBar.hide(); }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        barcodeView.pauseAndWait();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}