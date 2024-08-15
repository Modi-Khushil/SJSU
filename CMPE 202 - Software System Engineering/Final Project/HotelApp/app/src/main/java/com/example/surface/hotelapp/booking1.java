package com.example.surface.hotelapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

public class booking1 extends AppCompatActivity {

    Button calendar;
    Button book;
    String CHANNEL_ID="Book";
    String booked;
    String nameValue;
    EditText etQuantityOfGuests;

    String activityName;
    String personQuantity;


    // The following variables/objects allows us to get the current user
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

    // Variables / objects which will be used to add data to the database
    private DatabaseReference mDatabase;
    private DatabaseReference dbRefActivities;

    private String date;

    // Calendar for Date Picker
    Calendar c = Calendar.getInstance();
    DateFormat fmtDate = DateFormat.getInstance();
    DatePickerDialog datePickerDialog;

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            c.set(Calendar.YEAR, i);
            c.set(Calendar.MONTH, i1);
            c.set(Calendar.DAY_OF_MONTH, i2);
            date = fmtDate.format(c.getTime());
        }
    };

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Blocks orientation change
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_booking1);

        createNotificationChannel();

        // Gets the extras from the previous page
        booked = "Yes";
        Bundle extras = getIntent().getExtras();
        nameValue = extras.getString("userName");
        final String activity = extras.getString("activity");
        final int individualPrice = extras.getInt("price");

        // Connecting to database to get data which will be send to notification
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dbRefActivities = mFirebaseDatabase.getReference("activities");

        etQuantityOfGuests = (EditText) findViewById(R.id.etQtyPersonsActivity);

        this.setFinishOnTouchOutside(false);

        calendar = (Button) findViewById(R.id.calendar);
        book = (Button) findViewById(R.id.btnBook);

        // Pick the date
        calendar.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(booking1.this,
                    (view1, year, monthOfYear, dayOfMonth) -> date = dayOfMonth+"/"+(monthOfYear+1)+"/"+year, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1);
            datePickerDialog.show();
        });

        // Event handler for when the user attempts to book his/her activity
        book.setOnClickListener(view -> {

            if (etQuantityOfGuests.getText().toString().isEmpty())  {
                Toast.makeText(booking1.this, "Please type in the amount of guests", Toast.LENGTH_LONG).show();
            }
            else    {
                int  quantity = Integer.parseInt(etQuantityOfGuests.getText().toString());

                // This variable is the final price of the booking based on quantity of people
                int price = quantity * individualPrice;

                // Try catch for handling errors
                try
                {
                    //Create new object for firebase
                    final Activities newActivity = new Activities(uid, activity,quantity, price, date );

                    // Push new object to firebase activities entity
                    mDatabase.child("activities").child(uid).push().setValue(newActivity);

                    // Listen for data change events
                    dbRefActivities.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // Foreach loop for searching the data in activities table related to user by uid
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren())
                            {
                                for (DataSnapshot postSnapshot1: postSnapshot.getChildren())
                                {
                                    // If activity is found take the name of it and quantity of persons booked
                                    if (Objects.requireNonNull(postSnapshot1.child("userID").getValue()).toString().equals(uid))
                                    {
                                        activityName =  Objects.requireNonNull(postSnapshot1.child("activity").getValue()).toString();
                                        personQuantity = Objects.requireNonNull(postSnapshot1.child("quantity").getValue()).toString();
                                    }
                                }
                            }

                            // Create notification every time data was changed
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(booking1.this, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.hotel)
                                    .setContentTitle("Booking Confirmation")//Notification content taken from DB
                                    .setContentText("You just booked "+activityName+" with us for "+personQuantity+" people")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(booking1.this);

                            int notificationId=0;
                            notificationManager.notify(notificationId, mBuilder.build());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // Get back to activities page after booking was made
                    Intent i = new Intent(booking1.this, ActivitiesActivity.class);

                    // Send Confirmation message trigger if booking was made
                    i.putExtra("booked?", booked);

                    // Send user name for navBar
                    i.putExtra("userName", nameValue);
                    startActivity(i);

                }
                catch (Exception ex)   {
                    Log.d("ActivityBookingError", ex.getMessage());
                }
            }
        });
    }

    // Create notification channel with checking SDK version
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}


