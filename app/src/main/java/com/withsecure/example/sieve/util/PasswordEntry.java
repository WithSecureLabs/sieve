package com.withsecure.example.sieve.util;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordEntry implements Parcelable {
    public static Creator<PasswordEntry> CREATOR = null;
    public String email;
    public String password;
    public String service;
    public String username;

    static {
        PasswordEntry.CREATOR = new Creator<PasswordEntry>() {
            public PasswordEntry createFromParcel(Parcel in) {
                return new PasswordEntry(in);
            }

            public PasswordEntry[] newArray(int size) {
                return new PasswordEntry[size];
            }
        };
    }

    public PasswordEntry(Parcel in) {
        String[] input = new String[4];
        in.readStringArray(input);
        this.service = input[0];
        this.username = input[1];
        this.email = input[2];
        this.password = input[3];
    }

    public PasswordEntry(String s, String u, String e, String p) {
        this.service = s;
        this.username = u;
        this.password = p;
        this.email = e;
    }

    public static Map<String, String> MapList(List<PasswordEntry> list0) {
        Map<String, String> m = new HashMap<>();
        for(int i = 0; i < list0.size(); ++i) {
            m.put((list0.get(i)).service, (list0.get(i)).username);
        }

        return m;
    }

    @Override  // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return this.service + "\n" + this.username;
    }

    @Override  // android.os.Parcelable
    public void writeToParcel(Parcel out, int arg1) {
        out.writeStringArray(new String[]{this.service, this.username, this.email, this.password});
    }
}

