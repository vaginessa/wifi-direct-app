package edu.rit.se.crashavoidance.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by domotica3 on 1/1/17.
 */

public class DeviceResponse {
    public Device resDevice;
    public String srcMAC = "";
    public String resDate = "";
    public String done = "True";
    public List<Device> route = new ArrayList<>();

    public DeviceResponse() {}

//    public String toString() {
//        return ("{{0}}<>{{1}},{{2}},{{3}}")
//                .replace("{{0}}",resDevice.toString())
//                .replace("{{1}}",srcMAC)
//                .replace("{{2}}",resDate)
//                .replace("{{3}}",done);
//    }

//    public DeviceResponse(Device resDevice, String srcMAC, String resDate){
//        this.resDevice = resDevice;
//        this.srcMAC = srcMAC;
//        this.resDate = resDate;
//    }

//    public static DeviceResponse strDeviceRes(String strDeviceRes) {
//        String[] arr0 = strDeviceRes.split("<>");
//        String[] arr1 = arr0[0].split(",");
//        return new DeviceResponse(Device.strDevice(arr0[0]),arr1[0],arr1[1]);
//    }
}
