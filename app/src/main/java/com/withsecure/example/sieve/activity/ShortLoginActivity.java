package com.withsecure.example.sieve.activity;

import static com.withsecure.example.sieve.activity.WelcomeActivity.PERMISSIONS_CODE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.withsecure.example.sieve.R;
import com.withsecure.example.sieve.service.AuthService;
import com.withsecure.example.sieve.service.AuthServiceConnector;
import com.withsecure.example.sieve.service.CryptoService;

public class ShortLoginActivity extends Activity implements AuthServiceConnector.ResponseListener {
    public static final String TAG = "m_ShortLogin";
    private AlertDialog.Builder connectionError;
    private String mainKey;
    private TextView prompt;
    private EditText pwEntry;
    private AuthServiceConnector serviceConnection;
    private Button submitButton;
    private String workingPIN;

    public ShortLoginActivity() {
        this.workingPIN = null;
        this.mainKey = null;
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void checkKeyResult(boolean status) {
        Log.wtf(TAG, "called checkKeyResult?");
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void checkPinResult(boolean status) {
        if(status) {
            this.loginSuccessful();
            return;
        }

        this.loginFailed();
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void connected() {
        this.submitButton.setEnabled(true);
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void firstLaunchResult(int status) {
        Log.wtf(TAG, "called firstLaunchResult?");
    }

    public void loginFailed() {
        this.prompt.setText(R.string.error_incorrect_password);  // string:error_incorrect_password "This password is incorrect"
        this.submitButton.setEnabled(true);
    }

    public void loginSuccessful() {
        this.submitButton.setEnabled(true);
        Intent intent = new Intent(this, PWList.class);
        intent.putExtra(CryptoService.KEY, this.mainKey);
        this.startActivity(intent);
    }

    @Override  // android.app.Activity
    public void onBackPressed() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        this.startActivity(intent);
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_short_login);
        this.serviceConnection = new AuthServiceConnector(this);
        this.mainKey = this.getIntent().getStringExtra(AuthService.PASSWORD);
        this.pwEntry = this.findViewById(R.id.shortlogin_edittext_entry);
        this.prompt = this.findViewById(R.id.shortlogin_textview_prompt);
        this.submitButton = this.findViewById(R.id.shortlogin_button_submit);
        this.connectionError = new AlertDialog.Builder(this);
        this.connectionError.setMessage(R.string.service_error_cantconnect).setTitle("Error");
        this.connectionError.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.connectionError.create();
    }

    @Override  // android.app.Activity
    public void onPause() {
        super.onPause();
        this.unbind();
    }

    @Override  // android.app.Activity
    public void onResume() {
        super.onResume();
        this.bindService(new Intent(this, AuthService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void sendFailed() {
        this.connectionError.show();
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void setKeyResult(boolean status) {
        Log.wtf(TAG, "called setKeyResult?");
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void setPinResult(boolean status) {
        Log.wtf(TAG, "called setPinResult?");
    }

    public void submit(View view) {
        this.workingPIN = this.pwEntry.getText().toString();
        Log.d(TAG, "user has entered a pin: " + this.workingPIN);
        this.serviceConnection.checkPin(this.workingPIN);
        this.submitButton.setEnabled(false);
    }

    private void unbind() {
        this.unbindService(this.serviceConnection);
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

