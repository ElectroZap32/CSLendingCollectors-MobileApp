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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.cslending.zc.collapp.databinding.FragmentHomeBinding;
import com.cslending.zc.collapp.ui.pay.SetPayment;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

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

            Intent intent = new Intent(getActivity(), SetPayment.class);
            String code = result.getText();

            db.read();
            Cursor result2 = db.getLoanfromQR(code);

            if (result2.getCount() == 0) {
                Toast.makeText(getContext(), "No Loans Found!", Toast.LENGTH_SHORT).show();
                barcodeView.resume();
            } else {
                intent.putExtra("scode", result.getText());
                startActivity(intent);
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