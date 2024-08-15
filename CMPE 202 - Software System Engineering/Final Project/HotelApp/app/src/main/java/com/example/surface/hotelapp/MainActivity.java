package com.example.surface.hotelapp;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static FragmentManager fragmentManager;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        // If saved instance state is null, replace login fragment
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frame, new LogInFragment(),
                            Utils.LogInFragment).commit();
        }

    }

    //animation
    protected void loginFragment() {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.frame, new LogInFragment(), Utils.LogInFragment).commit();
    }

    @Override
    public void  onBackPressed() {
        Fragment SignUpFragment = fragmentManager.findFragmentByTag(Utils.SignUpFragment);

        if (SignUpFragment != null) {
            loginFragment();
        }
        else {
            super.onBackPressed();
        }

    }
}
