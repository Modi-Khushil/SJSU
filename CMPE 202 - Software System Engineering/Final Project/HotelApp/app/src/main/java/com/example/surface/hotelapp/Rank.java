package com.example.surface.hotelapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Rank extends AppCompatActivity {

    //page elements
    String nameValue;
    private RatingBar ratingBar;
    EditText etReview;
    Button sendReview;
    DialView mCustomView;

    //to get the uid from the current user
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    // Database reference object
    private DatabaseReference mDatabase;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Blocks orientation change
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_rank);

        mCustomView = findViewById(R.id.dialView);
        //gets the username string for NavBar
        Bundle extrasName = getIntent().getExtras();
        nameValue = extrasName.getString("userName");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //home button
        FloatingActionButton fabHome = (FloatingActionButton) findViewById(R.id.floatingActionButtonRank);
        fabHome.setOnClickListener(view -> {
            Intent i = new Intent(Rank.this, HomeActivity.class);
            i.putExtra("userName", nameValue);
            startActivity(i);
        });
        //calling method that checks for the rating on the bar
        addListenerOnRatingBar();
        //xml page elements
        sendReview = (Button) findViewById(R.id.btnSendReview);
        etReview = (EditText) findViewById(R.id.editText);


        sendReview.setOnClickListener(view -> {
            try{
                //gets the rating for the RatingBar
                ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                float rank = ratingBar.getRating();
                String review = etReview.getText().toString();
                //writes "room_review" object in the database
                RoomRating roomRating = new RoomRating(uid, rank, review);
                try {
                    mDatabase.child("room_review").child(uid).setValue(roomRating);
                    Toast.makeText(Rank.this,
                            "Thank you for your review!",
                            Toast.LENGTH_LONG).show();
                }
                catch (Exception ex) {
                    Toast.makeText(Rank.this,
                            "An error occurred while sending a review." +
                                    " Please contact our team at vishnu@gmail.com",
                            Toast.LENGTH_LONG).show();
                }
            }

            catch (Exception ex) {
                Toast.makeText(Rank.this,
                        "Please write your review first",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    //method to check for the rating, display the Toast message and change the Smile face accordingly
    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            Toast.makeText(Rank.this, "Your rating is " + rating,
                    Toast.LENGTH_LONG).show();
            mCustomView.setRating(rating);
        });
    }

}
