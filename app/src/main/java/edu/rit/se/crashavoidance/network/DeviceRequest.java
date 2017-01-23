package edu.rit.se.crashavoidance.network;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by domotica3 on 12/31/16.
 */

public class DeviceRequest {
    public Device reqDevice;
    public String srcMAC = "";
    public String reqDate = "";
    public String done = "False";
    public List<Device> route = new ArrayList<>();

    public DeviceRequest () {}

    public DeviceRequest(Device reqDevice, String srcMAC){
        this.reqDevice = reqDevice;
        this.srcMAC = srcMAC;
        this.reqDate = getCurDate();
    }

//    public DeviceRequest(Device reqDevice, String srcMAC, String reqDate){
//        this.reqDevice = reqDevice;
//        this.srcMAC = srcMAC;
//        this.reqDate = reqDate;
//    }

    public String getCurDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    public Boolean inRequest(String deviceAddress) {
        for(Device d : route) {
            if (d.myMAC.equals(deviceAddress)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

//    public String toString() {
//        return ("{{0}};{{1}},{{2}},{{3}}")
//                .replace("{{0}}",reqDevice.toString())
//                .replace("{{1}}",srcMAC)
//                .replace("{{2}}",reqDate)
//                .replace("{{3}}",done);
//    }
//
//    public static DeviceRequest strDeviceReq(String strDeviceReq) {
//        String[] arr0 = strDeviceReq.split(";");
//        String[] arr1 = arr0[0].split(",");
//        return new DeviceRequest(Device.strDevice(arr0[0]),arr1[0],arr1[1]);
//    }
}
