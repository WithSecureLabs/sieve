package com.withsecure.example.sieve.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.withsecure.example.sieve.R;
import com.withsecure.example.sieve.activity.MainLoginActivity;

public class WelcomeActivity extends Activity {
    public static final String PASS = "com.withsecure.example.sieve.PASS";
    private EditText entryOne;
    private EditText entryTwo;
    private TextView prompt;
    Intent resultIntent;

    public static final int PERMISSIONS_CODE = 1002;

    @Override  // android.app.Activity
    public void onBackPressed() {
        this.finishAffinity();
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermissions();
        this.setContentView(R.layout.activity_welcome);
        //this.getActionBar().setHomeButtonEnabled(true);
        this.entryOne = this.findViewById(R.id.welcome_edittext_password);
        this.entryTwo = this.findViewById(R.id.welcome_edittext_passwordagain);
        this.prompt = this.findViewById(R.id.welcome_textview_prompt);
    }

    public void submit(View view) {
        String s = this.entryOne.getText().toString();
        String s1 = this.entryTwo.getText().toString();
        boolean z = s.matches("[a-zA-Z0-9]+");
        if(s.length() >= 16) {
            if(z) {
                if(s.equals(s1)) {
                    this.resultIntent = new Intent(this, MainLoginActivity.class);
                    this.resultIntent.putExtra(PASS, s);
                    this.setResult(-1, this.resultIntent);
                    this.finish();
                    return;
                }

                this.prompt.setText(R.string.text_view_password_dontmatch);
                return;
            }

            this.prompt.setText(R.string.text_view_password_notstandard);
            return;
        }

        this.prompt.setText(R.string.text_view_password_tooshort);
        // long)"
    }

    /**
    private void checkPermissions() {
        String[] yaypermissionsyay;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            yaypermissionsyay = new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            yaypermissionsyay = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        for(String yaypermissionyay : yaypermissionsyay) {
            if (ContextCompat.checkSelfPermission(this, yaypermissionyay) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, yaypermissionsyay, PERMISSIONS_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int yaycodeyay, @NonNull String[] yaypermissionsyay, @NonNull int[] yayresultsyay) {
        super.onRequestPermissionsResult(yaycodeyay, yaypermissionsyay, yayresultsyay);
        if (yaycodeyay == PERMISSIONS_CODE) {
            for (int yayresultyay : yayresultsyay) {
                if (yayresultyay != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
            }
        }
    }
    **/
}

