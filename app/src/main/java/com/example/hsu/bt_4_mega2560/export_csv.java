package com.example.hsu.bt_4_mega2560;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class export_csv {
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = this.fileforder+filename;
    }

    private String filename;
    private boolean state = false;
    private final String fileforder = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"bike";

    public export_csv(String filename) {
        this.filename = this.fileforder+filename;
    }
    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
    public void write_csv(String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.filename),true));
        writer.write(data);
        writer.write("\n");
        writer.flush();
        writer.close();
    }




}
