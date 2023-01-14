package com.cslending.zc.collapp.account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.cslending.zc.collapp.BuildConfig;
import com.cslending.zc.collapp.Login;
import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.Singleton;
import com.cslending.zc.collapp.collections.Worksheet;
import com.cslending.zc.collapp.databinding.FragmentAccountBinding;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText ex_name = root.findViewById(R.id.expenses_name);
        EditText ex_amt = root.findViewById(R.id.expenses_amt);
        Button ex_btn = root.findViewById(R.id.expenses_submit);

        Button ws_btn = root.findViewById(R.id.button_worksheet);
        Button about_btn = root.findViewById(R.id.button_about);
        Button logout_btn = root.findViewById(R.id.button_logout);

        SharedPreferences ret = this.requireActivity().getSharedPreferences("ColInfo", Context.MODE_PRIVATE);
        final String ex_col = ret.getString(getResources().getString(R.string.collector),"");

        ex_btn.setOnClickListener(v -> {
            String en = ex_name.getText().toString();
            String ea = ex_amt.getText().toString();

            if (en.equals("") || ea.equals("")) {
                AlertDialog.Builder embuilder = new AlertDialog.Builder(getContext());
                embuilder.setMessage("Please input all values!")
                        .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
                AlertDialog emalert = embuilder.create();
                emalert.show();
            } else {
                ex_name.setText("");
                ex_amt.setText("");
                send_expense(en, ea, ex_col);
            }
        });

        ws_btn.setOnClickListener(v -> startActivity(new Intent(getContext(), Worksheet.class)));

        about_btn.setOnClickListener(v -> {
            AlertDialog.Builder about_ad = new AlertDialog.Builder(requireContext());
            about_ad.setMessage("Chardikala Sandhu Lending Corp.\n\nMobile App Version:\n" + BuildConfig.VERSION_NAME)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
            AlertDialog about = about_ad.create();
            about.show();
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            final SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            public void onClick(View v) {
                AlertDialog.Builder confirmbuilder1 = new AlertDialog.Builder(requireContext());
                confirmbuilder1.setMessage("Do you really want to log-out?")
                        .setCancelable(false)
                        .setPositiveButton("No", (dialog, id) -> dialog.dismiss())
                        .setNegativeButton("Yes", (dialog, id) -> {
                            dialog.dismiss();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getResources().getString(R.string.prefStatus),"logout");
                            editor.apply();

                            Toast.makeText(getContext(), "Successfully Logged Out!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getContext(), Login.class));
                            requireActivity().finish();
                        });
                AlertDialog yesalert = confirmbuilder1.create();
                yesalert.show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void send_expense(final String ex_name, final String ex_amt, final String ex_col) {
        AlertDialog.Builder sebuilder = new AlertDialog.Builder(getActivity());
        sebuilder.setMessage("Sending Expenses...")
                .setCancelable(false);
        final AlertDialog sealert = sebuilder.create();
        sealert.show();

        StringRequest request = new StringRequest(Request.Method.POST, BuildConfig.SERVER_URL + "post-expenses.php", response -> {
            sealert.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            if (response.equals("Success!")){
                builder.setMessage("Expenses received!")
                        .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                builder.setMessage("Server Error!\n\n" + response)
                        .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }
        }, error -> {
            sealert.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Cannot send expenses! Please try again later! \n\nError Details: " + error)
                    .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String,String> param = new HashMap<>();
                param.put("name", ex_name);
                param.put("amt", ex_amt);
                param.put("coll",ex_col);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(8000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getmInstance(getActivity()).addToRequestQueue(request);
    }
}