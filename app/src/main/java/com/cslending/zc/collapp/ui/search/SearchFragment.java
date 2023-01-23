package com.cslending.zc.collapp.ui.search;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.cslending.zc.collapp.databinding.FragmentSearchBinding;
import com.cslending.zc.collapp.ui.pay.SetPayment;
import com.cslending.zc.collapp.ui.pay.SetPayment_No;
import com.cslending.zc.collapp.ui.pay.SetPayment_Yes;

import java.util.Objects;

public class SearchFragment extends Fragment {

    SQLiteDB db;

    private FragmentSearchBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText input_sc = root.findViewById(R.id.edittext_sc);
        db = new SQLiteDB(getActivity());

        Button sc_btn = root.findViewById(R.id.search_btn);

        sc_btn.setOnClickListener(v -> {
            String sc_code = input_sc.getText().toString().toUpperCase();

            if (sc_code.equals("")) {
                Toast.makeText(getActivity(), "Please input the code!", Toast.LENGTH_SHORT).show();
            } else {
                db.read();
                Cursor result = db.getLoanfromSC(sc_code);

                if (result.getCount() == 0) {
                    Toast.makeText(getActivity(), "No Loans Found!", Toast.LENGTH_SHORT).show();
                } else {
                    if (result.moveToFirst()) {
                        String loancode = result.getString(0);
                        String paid = result.getString(1);
                        String rem = result.getString(2);
                        input_sc.setText("");

                        if (Objects.equals(paid, "-")) {
                            Intent intent = new Intent(getActivity(), SetPayment.class);
                            intent.putExtra("scode", loancode);
                            startActivity(intent);
                        } else if (Objects.equals(paid, "Yes")) {
                            if (Objects.equals(rem, "")) {
                                String message = "Loan #" + loancode + " payment is set to 'PAY' but not yet processed. \n\nProceed?";
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
                                String message = "Loan #" + loancode + " payment was already set to 'PAY'.\n\nDo you want to print receipt only?";
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
                                String message = "Loan #" + loancode + " payment is set to 'NO PAY' but not yet processed.\n\nProceed?";
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
                result.close();
                db.close();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}