package edu.rit.se.crashavoidance.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import edu.rit.se.crashavoidance.R;
import edu.rit.se.wifibuddy.DnsSdService;
import edu.rit.se.wifibuddy.DnsSdTxtRecord;

/**
 *
 */
class AvailableServicesListViewAdapter extends BaseAdapter {

    private List<DnsSdService> serviceList;
    private final MainActivity context;

    public AvailableServicesListViewAdapter(MainActivity context, List<DnsSdService> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DnsSdService service = getItem(position);

        // Inflates the template view inside each ListView item
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.service_item, parent, false);
        }

        TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceName);
        TextView deviceInfoTextView = (TextView) convertView.findViewById(R.id.deviceInfo);
        TextView connectTextView = (TextView) convertView.findViewById(R.id.connect);
        connectTextView.setText("Connect");

        String sourceDeviceName = service.getSrcDevice().deviceName;
        if (sourceDeviceName.equals("")) {
            sourceDeviceName = "Android Device";
        }
        deviceNameTextView.setText(sourceDeviceName);

        Map<String, String> mapTxtRecord;
        String strTxtRecord = "";
        if (context.getWifiHandler() != null) {
            DnsSdTxtRecord txtRecord = context.getWifiHandler().getDnsSdTxtRecordMap().get(service.getSrcDevice().deviceAddress);
            if (txtRecord != null) {
                mapTxtRecord = txtRecord.getRecord();
                for (Map.Entry<String, String> record : mapTxtRecord.entrySet()) {
                    strTxtRecord += record.getKey() + ": " + record.getValue() + "\n";
                }
            }
        }
        String status = context.getWifiHandler().deviceStatusToString(context.getWifiHandler().getThisDevice().status);
        String strDeviceInfo = status + "\n" + strTxtRecord;
        deviceInfoTextView.setText(strDeviceInfo);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.onServiceClick(service);
            }
        });

        return convertView;
    }

//    private boolean isMaster(DnsSdService service) {
//        Map<String, String> mapTxtRecord;
//        String strTxtRecord = "";
//        if (context.getWifiHandler() != null) {
//            DnsSdTxtRecord txtRecord = context.getWifiHandler().getDnsSdTxtRecordMap().get(service.getSrcDevice().deviceAddress);
//            if (txtRecord != null) {
//                mapTxtRecord = txtRecord.getRecord();
//                for (Map.Entry<String, String> record : mapTxtRecord.entrySet()) {
//                    if (record.getKey().equals("master")) {
//                        return record.getValue().equals("True");
//                    }
//                }
//            }
//        }
//        return false;
//    }

    public boolean isRecordProperty(DnsSdService service, String propName) {
        Map<String, String> mapTxtRecord;
        if (context.getWifiHandler() != null) {
            DnsSdTxtRecord txtRecord = context.getWifiHandler().getDnsSdTxtRecordMap().get(service.getSrcDevice().deviceAddress);
            if (txtRecord != null) {
                mapTxtRecord = txtRecord.getRecord();
                for (Map.Entry<String, String> record : mapTxtRecord.entrySet()) {
                    if (record.getKey().equals(propName)) {
                        return record.getValue().equals("True");
                    }
                }
            }
        }
        return false;
    }

    /**
     * Add service to the Services list if it has not already been added
     * @param service Service to be added to list
     * @return false if item was already in the list
     */
    public Boolean addUnique(DnsSdService service) {
        if (serviceList.contains(service)) {
            return false;
        } else if (isRecordProperty(service, "master") &&
                (!service.getSrcDevice().deviceName.equals("Android Device") ||
                        !service.getSrcDevice().deviceName.equals(""))) {
            serviceList.add(service);
            this.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getCount() {
        return serviceList.size();
    }

    @Override
    public DnsSdService getItem(int position) {
        return serviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
