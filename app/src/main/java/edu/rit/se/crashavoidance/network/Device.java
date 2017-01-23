package edu.rit.se.crashavoidance.network;

/**
 * Created by domotica3 on 12/31/16.
 */

public class Device {
    public String nameID;
    public String curCoordinates = "";
    public String curDateTime = "";
    public String myMAC = "";

    public Device() {}

    public Device (String nameID, String curCoordinates, String curDateTime) {
        this.nameID = nameID;
        this.curCoordinates = curCoordinates;
        this.curDateTime = curDateTime;
    }

//    public String toString() {
//        return ("{{0}},{{1}},{{2}}")
//                .replace("{{0}}",nameID)
//                .replace("{{1}}",curCoordinates)
//                .replace("{{2}}",curDateTime);
//    }
//
//    public static Device strDevice(String strDevice) {
//        String[] arr = strDevice.split(",");
//        return new Device(arr[0],arr[1],arr[2]);
//    }
}
