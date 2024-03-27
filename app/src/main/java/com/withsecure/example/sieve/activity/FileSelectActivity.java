package com.withsecure.example.sieve.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.withsecure.example.sieve.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSelectActivity extends Activity implements AdapterView.OnItemClickListener {
    public static final String FILE = "com.withsecure.example.sieve.FILE";
    private TextView currentPath;
    private List<String> item;
    private List<String> path;
    private ListView pathList;
    private Intent resultIntent;
    private static final String root = "/";

    public FileSelectActivity() {
        this.item = null;
        this.path = null;
    }

    private void cancel() {
        this.resultIntent = new Intent(this, SettingsActivity.class);
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
        this.setContentView(R.layout.activity_file_select);
        String startingPath = this.getExternalFilesDir(null).getPath();
        this.currentPath = this.findViewById(R.id.fileselect_textview_path);
        this.pathList = this.findViewById(R.id.fileselect_list_path);

        this.processDir(startingPath);
    }

    @Override  // android.widget.AdapterView$OnItemClickListener
    public void onItemClick(AdapterView adapterView0, View arg1, int pos, long id) {
        File file = new File((this.path.get(pos)));
        if(file.isDirectory()) {
            if(file.canRead()) {
                this.processDir((this.path.get(pos)));
                return;
            }
            new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("[" + file.getName() + "] folder can't be read!").setPositiveButton("OK", (dialog, i) -> {
            }).show();
            return;
        }

        if((file.toString().endsWith(".xml")) || (file.toString().endsWith(".XML"))) {
            this.returnFile(file.getAbsolutePath());
        }
    }

    private void processDir(String dir) {
        this.currentPath.setText("Current path: " + dir);
        this.item = new ArrayList<>();
        this.path = new ArrayList<>();
        File f = new File(dir);
        File[] arr_file = f.listFiles();
        if(!dir.equals(root)) {
            this.item.add(root);
            this.path.add(root);
            this.item.add("../");
            this.path.add(f.getParent());
        }

        for (File file : arr_file) {
            this.path.add(file.getPath());
            if (file.isDirectory()) {
                this.item.add(file.getName() + root);
            } else {
                this.item.add(file.getName());
            }
        }

        ArrayAdapter<String> fileList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, this.item);
        this.pathList.setAdapter(fileList);
        this.pathList.setOnItemClickListener(this);
    }

    private void returnFile(String out) {
        this.resultIntent = new Intent(this, SettingsActivity.class);
        this.resultIntent.putExtra(FILE, out);
        this.setResult(-1, this.resultIntent);
        this.finish();
    }
}

