package com.example.hsu.bt_4_mega2560;

public class ble_receiver_data {

    public ble_receiver_data(long time, String rece_string) {
        this.time = time;
        this.rece_string = rece_string;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private long time;

    public String getRece_string() {
        return rece_string;
    }

    public void setRece_string(String rece_string) {
        this.rece_string = rece_string;
    }

    private String rece_string;

}
