package com.example.surface.hotelapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

//class that extends Custom View from TextView for hiding and showing password on icon pressed down and up
public class EditTextPasswords extends AppCompatEditText {

    //member variable
    Drawable pwDisplayButton;

    //three required constructors
    public EditTextPasswords(Context context) {
        super(context);
        init();
    }

    public EditTextPasswords(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextPasswords(Context context,
                             AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        //initialize Drawable member variable.
        pwDisplayButton =
                ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_visibility_black_24dp, null);

        setOnTouchListener((v, event) -> {
            //checks to display the icon/button and where to display it
            if ((getCompoundDrawablesRelative()[2] != null)) {
                float buttonStart;
                boolean isDisplayButtonClicked = false;
                buttonStart = (getWidth() - getPaddingEnd()
                        - pwDisplayButton.getIntrinsicWidth());

                if (event.getX() > buttonStart) {
                    isDisplayButtonClicked = true;
                }

                //checks for actions if the icon is tapped.
                if (isDisplayButtonClicked) {
                    //checks for ACTION_DOWN (always occurs before ACTION_UP) to show the password
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        pwDisplayButton =
                                ResourcesCompat.getDrawable(getResources(),
                                        R.drawable.ic_visibility_black_24dp, null);

                        EditTextPasswords.this.setTransformationMethod(HideReturnsTransformationMethod
                                .getInstance());
                        showButton();
                    }
                    //checks for ACTION_UP to hide the password
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        pwDisplayButton =
                                ResourcesCompat.getDrawable(getResources(),
                                        R.drawable.ic_visibility_off_black_24dp, null);
                        EditTextPasswords.this.setTransformationMethod(PasswordTransformationMethod
                                .getInstance());
                        showButton();

                        return true;
                    }

                } else {
                    return false;
                }
            }
            return false;
        });
        //check for the change in the text to determine when to show the icon/button
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {
                showButton();
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                showButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
                showButton();
            }
        });
    }
    //shows the icon/button
    private void showButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds
                (null,                      // Start of text.
                        null,               // Top of text.
                        pwDisplayButton,  // End of text.
                        null);              // Below text.
    }

}
