package com.example.hsu.bt_4_mega2560;

/**
 * Created by hsu on 2017/2/7.
 */
public class blutoothdata {
    String devicename,deviceaddress,deviceuuid;

    public String getDevicename() {
        return devicename;
    }

    public String getDeviceuuid() {
        return deviceuuid;
    }

    public void setDeviceuuid(String deviceuuid) {
        this.deviceuuid = deviceuuid;
    }

    public String getDeviceaddress() {

        return deviceaddress;
    }

    public void setDeviceaddress(String deviceaddress) {
        this.deviceaddress = deviceaddress;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }
}
