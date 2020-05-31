package com.example.hsu.bt_4_mega2560;

import android.os.Environment;

import java.io.File;

public class export_csv {
    private String filename;

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    private boolean state;
    private final String fileforder = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"bike";

    public export_csv(String filename) {
        this.filename = this.fileforder+filename;
    }

}
