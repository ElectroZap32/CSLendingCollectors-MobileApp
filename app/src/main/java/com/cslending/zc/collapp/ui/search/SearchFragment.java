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
import androidx.fragment.app.Fragment;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.cslending.zc.collapp.databinding.FragmentSearchBinding;
import com.cslending.zc.collapp.ui.pay.SetPayment;

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
            Intent intent = new Intent(getActivity(), SetPayment.class);

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
                        input_sc.setText("");

                        intent.putExtra("scode", loancode);
                        startActivity(intent);
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