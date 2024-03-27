package com.withsecure.example.sieve.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.withsecure.example.sieve.util.PasswordEntry;
import com.withsecure.example.sieve.R;

import java.util.Objects;

public class AddEntryActivity extends Activity {
    public static final String ENTRY = "com.withsecure.example.sieve.ENTRY";
    public static final String REQUEST = "com.withsecure.example.sieve.REQUEST";
    public static final int REQUEST_ADD = 4365;
    public static final int REQUEST_EDIT = 2346;
    private static final String TAG = "m_AddEntry";
    AlertDialog.Builder ausDelete;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editPasswordAgain;
    private EditText editService;
    private EditText editUsername;
    private TextView prompt;
    private int requestCode;
    Intent resultIntent;

    public AddEntryActivity() {
        this.requestCode = 0;
    }

    private void cancel() {
        this.resultIntent = new Intent(this, PWList.class);
        this.setResult(0, this.resultIntent);
        this.finish();
    }

    private void fillData(PasswordEntry pe) {
        this.editService.setText(pe.service);
        this.editUsername.setText(pe.username);
        this.editEmail.setText(pe.email);
        this.editPassword.setText(pe.password);
        this.editPasswordAgain.setText(pe.password);
    }

    @Override  // android.app.Activity
    public void onBackPressed() {
        this.cancel();
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_entry);

        findViewById(R.id.addentry_button_save).setOnClickListener(v -> {
            save();
        });

        findViewById(R.id.addentry_button_delete).setOnClickListener(v -> {
            delete();
        });

        findViewById(R.id.addentry_button_cancel).setOnClickListener(v -> {
            cancel();
        });


        this.editService = this.findViewById(R.id.addentry_edittext_service);
        this.editUsername = this.findViewById(R.id.addentry_edittext_username);
        this.editEmail = this.findViewById(R.id.addentry_edittext_email);
        this.editPassword = this.findViewById(R.id.addentry_edittext_password);
        this.editPasswordAgain = this.findViewById(R.id.addentry_edittext_passwordagain);
        this.prompt = this.findViewById(R.id.addentry_textview_prompt);
        this.ausDelete = new AlertDialog.Builder(this);
        this.ausDelete.setMessage(R.string.addentry_confirm_delete).setTitle("Confirm");
        this.ausDelete.setPositiveButton("Yes", (arg0, arg1) -> AddEntryActivity.this.returnForDelete());
        this.ausDelete.setNegativeButton("No", (arg0, arg1) -> {
        });
        this.ausDelete.create();
        this.resultIntent = this.getIntent();
        if(this.resultIntent == null) {
            Log.e(TAG, "requestIntent = null");
        }

        this.requestCode = this.resultIntent.getIntExtra(REQUEST, 0);
        if(this.requestCode == 0x92A) {
            this.fillData((Objects.requireNonNull(this.resultIntent.getParcelableExtra(ENTRY))));
        }
    }

    private void returnForDelete() {
        this.resultIntent = new Intent(this, PWList.class);
        this.setResult(-1, this.resultIntent);
        this.finish();
    }

    private void returnToActivity() {
        String s = this.editService.getText().toString();
        String s1 = this.editUsername.getText().toString();
        String s2 = this.editEmail.getText().toString();
        String s3 = this.editPassword.getText().toString();
        String s4 = this.editPasswordAgain.getText().toString();
        if(s3.equals("")) {
            this.prompt.setText(R.string.error_incorrect_password);
            return;
        }

        if(!s3.equals(s4)) {
            this.prompt.setText(R.string.text_view_password_dontmatch);
            return;
        }

        if(s3.length() < 4) {
            this.prompt.setText(R.string.error_incorrect_password);
            return;
        }

        this.resultIntent.putExtra(ENTRY, new PasswordEntry(s, s1, s2, s3));
        this.setResult(-1, this.resultIntent);
        this.finish();
    }

    public void save() {
        this.returnToActivity();
    }

    public void delete() {
        this.ausDelete.show();
    }
}

