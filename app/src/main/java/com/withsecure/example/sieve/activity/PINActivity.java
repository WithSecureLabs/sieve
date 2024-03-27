package com.withsecure.example.sieve.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.withsecure.example.sieve.R;
import com.withsecure.example.sieve.activity.MainLoginActivity;
import com.withsecure.example.sieve.service.AuthService;

import java.util.regex.Pattern;

public class PINActivity extends Activity {
    public static final String REQUEST = "com.withsecure.example.sieve.REQUEST";
    public static final int REQUEST_ADD = 0x69B5B;
    public static final int REQUEST_EDIT = 92635;
    private String currentPIN;
    private EditText entryOne;
    private EditText entryThree;
    private EditText entryTwo;
    private TextView prompt;
    private int requestCode;
    Intent resultIntent;

    public PINActivity() {
        this.currentPIN = null;
    }

    private void cancel() {
        this.resultIntent = new Intent(this, MainLoginActivity.class);
        this.setResult(0, this.resultIntent);
        this.finish();
    }

    @Override  // android.app.Activity
    public void onBackPressed() {
        this.cancel();
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_pin);  // layout:activity_pin
        this.entryOne = this.findViewById(R.id.pinentry_edittext_pin);
        this.entryTwo = this.findViewById(R.id.pinentry_edittext_pinagain);
        this.entryThree = this.findViewById(R.id.pinentry_edittext_pinold);
        this.prompt = this.findViewById(R.id.pinentry_textview_prompt);
        Intent intent0 = this.getIntent();
        this.requestCode = intent0.getIntExtra(REQUEST, 0);
        switch(this.requestCode) {
            case REQUEST_EDIT: {
                this.currentPIN = intent0.getStringExtra(AuthService.PIN);
                break;
            }
            case REQUEST_ADD: {
                (this.findViewById(R.id.text_view_pin_password_old)).setVisibility(View.INVISIBLE);
                (this.findViewById(R.id.pinentry_edittext_pinold)).setVisibility(View.INVISIBLE);
            }
        }

    }

    public void submit(View view) {
        switch(this.requestCode) {
            case REQUEST_EDIT: {
                this.submitEdit();
                return;
            }
            case REQUEST_ADD: {
                this.submitEntry();
                return;
            }
            default: {
            }
        }
    }

    private void submitEdit() {
        String s = this.entryOne.getText().toString();
        String s1 = this.entryTwo.getText().toString();
        String s2 = this.entryThree.getText().toString();
        boolean z = Pattern.compile("[^0-9]").matcher(s).find();
        if(s2.equals(this.currentPIN)) {
            if(s.length() == 4) {
                if(!z) {
                    if(s.equals(s1)) {
                        this.resultIntent = new Intent(this, MainLoginActivity.class);
                        this.resultIntent.putExtra(AuthService.PIN, s);
                        this.setResult(-1, this.resultIntent);
                        this.finish();
                        return;
                    }

                    this.prompt.setText(R.string.text_view_pin_dontmatch);
                    return;
                }

                this.prompt.setText(R.string.text_view_pin_notstandard);
                return;
            }

            this.prompt.setText(R.string.text_view_pin_tooshort);
            return;
        }

        this.prompt.setText(R.string.text_view_pin_notold);
    }

    private void submitEntry() {
        String s = this.entryOne.getText().toString();
        String s1 = this.entryTwo.getText().toString();
        boolean z = Pattern.compile("[^0-9]").matcher(s).find();
        if(s.length() == 4) {
            if(!z) {
                if(s.equals(s1)) {
                    this.resultIntent = new Intent(this, MainLoginActivity.class);
                    this.resultIntent.putExtra(AuthService.PIN, s);
                    this.setResult(-1, this.resultIntent);
                    this.finish();
                    return;
                }

                this.prompt.setText(R.string.text_view_pin_dontmatch);
                return;
            }

            this.prompt.setText(R.string.text_view_pin_notstandard);
            return;
        }

        this.prompt.setText(R.string.text_view_pin_tooshort);
    }
}

