package com.cslending.zc.collapp.collections;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cslending.zc.collapp.R;
import com.cslending.zc.collapp.SQLiteDB;
import com.cslending.zc.collapp.databinding.FragmentCollectionsBinding;

public class CollectionsFragment extends Fragment {

    private FragmentCollectionsBinding binding;

    SQLiteDB sqlite_obj_coll;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TableLayout tableLayout = root.findViewById(R.id.table_collections);
        TableRow rowHeader = new TableRow(getContext());
        rowHeader.setBackgroundColor(Color.parseColor("#5DADE2"));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText={"NAME / LOAN","PAID","AMOUNT"};
        for (String c:headerText) {
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(5, 5, 5, 5);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayout.addView(rowHeader);

        sqlite_obj_coll = new SQLiteDB(getActivity());
        sqlite_obj_coll.read();
        Cursor c = sqlite_obj_coll.queryTable();

        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                String loan = c.getString(1);
                String nameloan = c.getString(0) + " (#" + loan.substring(loan.length() - 5) + ")";
                String paid = c.getString(2);
                String pay_amt = "₱" + c.getString(3);

                TableRow row = new TableRow(getContext());
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                String[] colText={nameloan+"",paid,pay_amt};

                for(String text:colText) {
                    TextView tv = new TextView(getContext());
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(15);
                    tv.setPadding(5, 5, 5, 5);
                    tv.setText(text);
                    switch (paid) {
                        case "Yes":
                            tv.setTextColor(Color.parseColor("#006000"));
                            break;
                        case "No":
                            tv.setTextColor(Color.parseColor("#FF0000"));
                            break;
                        case "-":
                            tv.setTextColor(Color.parseColor("#757b85"));
                            break;
                    }
                    row.addView(tv);
                }
                tableLayout.addView(row);
            }
        }
        c.close();
        sqlite_obj_coll.close();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}