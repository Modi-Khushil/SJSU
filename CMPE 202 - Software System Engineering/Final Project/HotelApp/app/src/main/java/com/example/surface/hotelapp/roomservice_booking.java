package com.example.surface.hotelapp;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class roomservice_booking extends AppCompatActivity {

    String nameValue;
    public Boolean fragmentShown = false;
    String booked;
    Button book;

    private TableLayout table; // TheLayout on Stephen's

    // We will use the following to get the uid from the current user
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    // Variables / objects which will be used to add data to the database
    private DatabaseReference mDatabase;


    // Inner class used to hold the controllers

    public static class MyComponents {
        //public TableRow tr;
        public TextView tvItem;
        public TextView tvPrice;
        public TextView tvQty;
        public Button btnLess;
        public Button btnMore;
    }

    @SuppressLint({"SourceLockedOrientationActivity", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Blocks orientation change
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_roomservice_booking);

        // Instantiates the Firebase Database object
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle extrasName = getIntent().getExtras();
        nameValue = extrasName.getString("userName");

        Bundle extras = getIntent().getExtras();
        booked = extras.getString("booked?");
        booked = "Yes";

        // Instantiates the arrayList which holds the components
        // Parameters for the dynamic table
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") ArrayList<MyComponents> mComponents = new ArrayList<>();

        // Resets the table when the activity is loaded
        table = (TableLayout) findViewById(R.id.tableOrders);
        table.removeAllViews();

        // Width and Height of the table which will be generated when itens are added

        // Title TextView
        final TextView txtTitle = (TextView) findViewById(R.id.txtRSOrder);

        String [] itens = null;
        String [] prices = null;

        Bundle myExtras = getIntent().getExtras();

        switch (myExtras.get("meal").toString()){
            case "breakfast":
                itens = getResources().getStringArray(R.array.breakfastitens);
                prices = getResources().getStringArray(R.array.breakfastprices);
                txtTitle.setText("Breakfast Menu");
                break;
            case "lunch":
                itens = getResources().getStringArray(R.array.lunchitens);
                prices = getResources().getStringArray(R.array.lunchprices);
                txtTitle.setText("Lunch & Dinner Menu");
                break;
            case "dessert":
                itens = getResources().getStringArray(R.array.dessertitens);
                prices = getResources().getStringArray(R.array.dessertprices);
                txtTitle.setText("Dessert Menu");
                break;
        }

        TableRow tr;
        for (int rows = 0; rows < Objects.requireNonNull(itens).length; rows++)    {

            final MyComponents m = new MyComponents();

            tr = new TableRow(roomservice_booking.this);
            tr.setBackgroundColor(Color.WHITE);
            tr.setPadding(0,4,0,0);

            // Creates Item TextView
            m.tvItem = new TextView(roomservice_booking.this);
            m.tvItem.setPadding(4,0,0,0);
            m.tvItem.setText(itens[rows]);

            // Creates Price TextView
            m.tvPrice = new TextView(roomservice_booking.this);
            m.tvPrice.setPadding(4,0,0,0);
            m.tvPrice.setText(Objects.requireNonNull(prices)[rows]);

            // Creates Qty TextView
            //final TextView tvQty;
            m.tvQty = new TextView(roomservice_booking.this);
            m.tvQty.setText("0");

            // Creates Decrease Button
            m.btnLess = new Button(roomservice_booking.this);
            m.btnLess.setText("-");
            // onClick Listener for the Decrease Button
            m.btnLess.setOnClickListener(view -> {
                int quantity = Integer.parseInt(m.tvQty.getText().toString());
                if (quantity > 0) {
                    quantity-=1;
                    m.tvQty.setText(""+quantity);
                }
            });

            // Creates Increase Button
            m.btnMore = new Button(roomservice_booking.this);
            m.btnMore.setText("+");
            // onClick Listener for the Increase Button
            m.btnMore.setOnClickListener(v -> {
                int quantity = Integer.parseInt(m.tvQty.getText().toString());
                quantity+=1;
                m.tvQty.setText(""+quantity);
            });

            // Adds all controllers to the tableRow
            tr.addView(m.tvItem);
            tr.addView(m.tvPrice);
            tr.addView(m.btnLess);
            tr.addView(m.tvQty);
            tr.addView(m.btnMore);

            // Adds the component to the ArrayList
            mComponents.add(m);
            // Adds the row to the table
            table.addView(tr);

        }

        book = (Button) findViewById(R.id.btnServiceBooking);
        book.setOnClickListener(v -> {
            Orders newOrder;
            for (int rows =0; rows < table.getChildCount();rows++) {

                Date today = new Date();
                String outputPattern = "dd/MM/yyyy";
                @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                String strToday;

                strToday = outputFormat.format(today);

                TableRow tr1 = (TableRow) table.getChildAt(rows);
                TextView tvItem = (TextView) tr1.getChildAt(0);
                TextView tvPrice = (TextView) tr1.getChildAt(1);
                TextView tvQty = (TextView) tr1.getChildAt(3);
                String item = tvItem.getText().toString();

                double price = Double.parseDouble(tvPrice.getText().toString());
                int qty = Integer.parseInt(tvQty.getText().toString());
                if (qty > 0) {
                    newOrder = new Orders(item, price, qty, strToday);

                    try {
                        mDatabase.child("orders").child(uid).push().setValue(newOrder);
                    } catch (Exception ex) {
                        Log.d("ErrorRecordingOrder", ex.getMessage());
                    }
                }

            }

            Intent i = new Intent(roomservice_booking.this, RoomServiceActivity.class);
            i.putExtra("booked?", booked);
            i.putExtra("userName", nameValue);
            startActivity(i);
        });

    }



    public void displayFragment()
    {
        ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance(3);

        FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container3, confirmationFragment).addToBackStack(null).commit();
    }

    public void closeFragment()
    {
        FragmentManager fragmentManager = getFragmentManager();
        ConfirmationFragment confirmationFragment = (ConfirmationFragment) fragmentManager
                .findFragmentById(R.id.fragment_container3);
        if(confirmationFragment != null)
        {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(confirmationFragment).commit();
        }

        fragmentShown = false;
    }

    public void showConfirmation()
    {
        displayFragment();

        new Handler().postDelayed(this::closeFragment, 2000);
    }



}
