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

public class MainLoginActivity extends Activity implements AuthServiceConnector.ResponseListener {
    private static final int IS_AUTHENTICATED = 0x44FDAB;
    public static final int MAIN_PIN = 2;
    public static final int MAIN_SETTINGS = 3;
    public static final int MAIN_WELCOME = 1;
    private static final int NOT_AUTHENTICATED = 0x9FE8B;
    private static final int NOT_INITALISED = 0xE1750;
    private static final String TAG = "m_MainLogin";
    EditText entry;
    Button login_button;
    TextView prompt;
    private AuthServiceConnector serviceConnection;
    private int state;
    private Intent workingIntent;
    private String workingPassword;

    public MainLoginActivity() {
        this.state = NOT_INITALISED;
        this.workingPassword = null;
        this.workingIntent = null;
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void checkKeyResult(boolean status) {
        if(status) {
            this.loginSuccessful();
            return;
        }

        this.loginFailed();
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void checkPinResult(boolean status) {
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void connected() {
        this.serviceConnection.checkFirstLaunch();
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void firstLaunchResult(int status) {
        switch(status) {
            case AuthServiceConnector.TYPE_HAS_KEY_HAS_PIN: {
                this.initaliseActivity();
                return;
            }
            case AuthServiceConnector.TYPE_HAS_KEY_NO_PIN: {
                this.setPin();
                return;
            }
            case AuthServiceConnector.TYPE_NO_KEY_NO_PIN: {
                this.welcomeUser();
                return;
            }
            default: {
            }
        }
    }

    private void initaliseActivity() {
        this.setContentView(R.layout.activity_main_login);
        this.entry = this.findViewById(R.id.mainlogin_edittext_entry);
        this.login_button = this.findViewById(R.id.mainlogin_button_login);
        this.prompt = this.findViewById(R.id.mainlogin_textview_prompt);
    }

    public void login(View view) {
        this.workingPassword = this.entry.getText().toString();
        Log.d(TAG, "String enetered: " + this.workingPassword);
        this.serviceConnection.checkKey(this.workingPassword);
        this.login_button.setEnabled(false);
    }

    public void settings_login(View view) {
        openSettings();
    }

    private void loginFailed() {
        this.prompt.setText(R.string.error_incorrect_password);
        this.login_button.setEnabled(true);
    }

    private void loginSuccessful() {
        this.login_button.setEnabled(true);
        Intent intent = new Intent(this, PWList.class);
        intent.putExtra(CryptoService.KEY, this.workingPassword);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.state = IS_AUTHENTICATED;
        this.startActivity(intent);
    }

    @Override  // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case MAIN_WELCOME:
            case MAIN_PIN: {
                if(resultCode == -1) {
                    this.workingIntent = data;
                    return;
                }

                return;
            }
            default: {
            }
        }
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
        //checkPermissions();
        this.serviceConnection = new AuthServiceConnector(this);
        this.state = NOT_AUTHENTICATED;
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
        switch(this.state) {
            case NOT_AUTHENTICATED:
            case NOT_INITALISED: {
                if(this.workingIntent != null) {
                    if(this.workingIntent.getStringExtra(WelcomeActivity.PASS) != null) {
                        this.serviceConnection.setKey(this.workingIntent.getStringExtra(WelcomeActivity.PASS));
                    }
                    else if(this.workingIntent.getStringExtra(AuthService.PIN) != null) {
                        this.serviceConnection.setPin(this.workingIntent.getStringExtra(AuthService.PIN));
                    }

                    this.workingIntent = null;
                    return;
                }

                return;
            }
            case IS_AUTHENTICATED: {
                this.startActivity(new Intent(this, ShortLoginActivity.class).putExtra(AuthService.PASSWORD, this.workingPassword));
                return;
            }
            default: {
            }
        }
    }

    @Override  // android.app.Activity
    protected void onStart() {
        super.onStart();
        this.startService(new Intent(this, AuthService.class));
    }

    private void openSettings() {
        this.startActivityForResult(new Intent(this, SettingsActivity.class), MAIN_SETTINGS);
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void sendFailed() {
        new AlertDialog.Builder(this).setMessage(R.string.service_error_cantconnect).setTitle("Error").setPositiveButton("OK", (dialog, which) -> {
        }).create().show();
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void setKeyResult(boolean status) {
        if(!status) {
            this.sendFailed();
        }
    }

    private void setPin() {
        Intent intent = new Intent(this, PINActivity.class);
        intent.putExtra(AddEntryActivity.REQUEST, PINActivity.REQUEST_ADD);
        this.startActivityForResult(intent, 2);
    }

    @Override  // com.withsecure.example.sieve.service.AuthServiceConnector$ResponseListener
    public void setPinResult(boolean status) {
        if(!status) {
            this.sendFailed();
        }
    }

    private void unbind() {
        this.unbindService(this.serviceConnection);
    }

    private void welcomeUser() {
        this.startActivityForResult(new Intent(this, WelcomeActivity.class), 1);
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

