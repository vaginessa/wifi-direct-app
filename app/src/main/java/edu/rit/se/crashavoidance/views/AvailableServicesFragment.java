package edu.rit.se.crashavoidance.views;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rit.se.crashavoidance.R;
import edu.rit.se.crashavoidance.network.Device;
import edu.rit.se.crashavoidance.network.DeviceRequest;
import edu.rit.se.crashavoidance.network.DeviceResponse;
import edu.rit.se.crashavoidance.network.DeviceType;
import edu.rit.se.wifibuddy.DnsSdService;
import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static edu.rit.se.crashavoidance.network.DeviceType.*;

/**
 * ListFragment that shows a list of available discovered services
 */
public class AvailableServicesFragment extends Fragment{

    private WiFiDirectHandlerAccessor wifiDirectHandlerAccessor;
    private List<DnsSdService> services = new ArrayList<>();
    private MainActivity context = null;
    private AvailableServicesFragment fragment = null;
    private AvailableServicesListViewAdapter servicesListAdapter;
    private ListView deviceList;
    private Toolbar toolbar;
    private static final String TAG = WifiDirectHandler.TAG + "ServicesFragment";

    Gson json = new Gson();

    /**
     * Sets the Layout for the UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = (MainActivity) getActivity();
        fragment = this;

        View rootView = inflater.inflate(R.layout.fragment_available_services, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        deviceList = (ListView)rootView.findViewById(R.id.device_list);
        prepareResetButton(rootView);
        setServiceList();
        services.clear();
        servicesListAdapter.notifyDataSetChanged();
        Log.d("TIMING", "Discovering started " + (new Date()).getTime());
        registerLocalP2pReceiver();
        getHandler().continuouslyDiscoverServices();
        return rootView;
    }

    private void prepareResetButton(View view){
        Button resetButton = (Button)view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetServiceDiscovery();

            }
        });

        /*
        * TEST purposes
        * */
        Button resetService = (Button)view.findViewById(R.id.reset_service);
        resetService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Device reqDevice = new Device(
                        "Q",
                        "",
                        "");

                DeviceRequest deviceRequest = new DeviceRequest(reqDevice,
                        getHandler().getThisDevice().deviceAddress);

                context.curRequest = deviceRequest;

                HashMap<String, String> record = new HashMap<>();
                record.put("Name", getHandler().getThisDevice().deviceName);
                record.put("Address", getHandler().getThisDevice().deviceAddress);
                record.put("DeviceType",  ACCESS_POINT_WREQ.toString());
                getHandler().addLocalService("Wi-Fi Buddy", record);
            }
        });
        /*
        * TEST purposes
        * */
    }

    /**
     * Sets the service list adapter to display available services
     */
    private void setServiceList() {
        servicesListAdapter = new AvailableServicesListViewAdapter((MainActivity) getActivity(), services);
        deviceList.setAdapter(servicesListAdapter);
    }

    /**
     * Onclick Method for the the reset button to clear the services list
     * and start discovering services again
     */
    private void resetServiceDiscovery(){
        // Clear the list, notify the list adapter, and start discovering services again
        Log.i(TAG, "Resetting Service discovery");
        services.clear();
        servicesListAdapter.notifyDataSetChanged();
        getHandler().resetServiceDiscovery();
    }

    private void registerLocalP2pReceiver() {
        Log.i(TAG, "Registering local P2P broadcast receiver");
        WifiDirectReceiver p2pBroadcastReceiver = new WifiDirectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiDirectHandler.Action.DNS_SD_SERVICE_AVAILABLE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(p2pBroadcastReceiver, intentFilter);
        Log.i(TAG, "Local P2P broadcast receiver registered");
    }

    /**
     * Receiver for receiving intents from the WifiDirectHandler to update UI
     * when Wi-Fi Direct commands are completed
     */
    public class WifiDirectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the intent sent by WifiDirectHandler when a service is found
            if (intent.getAction().equals(WifiDirectHandler.Action.DNS_SD_SERVICE_AVAILABLE)) {
                serviceKey = intent.getStringExtra(WifiDirectHandler.SERVICE_MAP_KEY);
                DnsSdService service = getHandler().getDnsSdServiceMap().get(serviceKey);
                Log.d(AvailableServicesFragment.TAG, "Service Discovered and Accessed " + (new Date()).getTime());

                MainActivity activity = fragment.context;

                //TODO:identify if removeGroup fits in this method
                // Add the service to the UI and update
                Boolean added = servicesListAdapter.addUnique(service, activity.deviceType);
                if (added) {
                    DeviceResponse response = activity.curResponse;
                    DeviceRequest request = activity.curRequest;

                    doResponse(activity, response, request, service);
                }

                // TODO Capture an intent that indicates the peer list has changed
                // and see if we need to remove anything from our list
            }
        }
    }

    String serviceKey;

    public void doResponse(MainActivity activity, DeviceResponse response, DeviceRequest request,
                           DnsSdService service) {
        String deviceAddress = service.getSrcDevice().deviceAddress;
        switch (activity.deviceType) {
            /*case EMITTER:
                Log.i(TAG, "EMITTER Available Services");
                if (!activity.visited.containsKey(deviceAddress)) {
                    Log.i(TAG, "EMITTER Available Services not visited");
                    activity.visited.put(deviceAddress, service);
                    activity.onServiceClick(service);
                }
                break;
            case QUERIER:
                Log.i(TAG, "QUERIER Available Services");
                if (response != null) {
                    Log.i(TAG, "QUERIER Available Services response != null");
                    if (deviceAddress.equals(request.srcMAC)) {
                        activity.onServiceClick(service);
                        Log.i(TAG, "Founded "+ deviceAddress +" is last device.");
                        Toast.makeText(
                                activity,
                                "Founded "+ deviceAddress +" device.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (request != null) {
                    Log.i(TAG, "QUERIER Available Services request != null");
                    if (!activity.visited.containsKey(deviceAddress)) {
                        Log.i(TAG, "QUERIER Available Services not visited");
                        //activity.visited.put(deviceAddress, service);
                        activity.onServiceClick(service);
                    }
                }
                break;
            case ACCESS_POINT:
                Log.i(WifiDirectHandler.TAG, getString(R.string.action_listening_ap));
                if (response != null) {
                    Log.i(TAG, "ACCESS_POINT Available Services response != null");
                    if (request.inRequest(deviceAddress)) {
                        activity.onServiceClick(service);
                        Log.i(TAG, "Founded "+ deviceAddress +" is part of route.");
                        Toast.makeText(
                                activity,
                                "Founded "+ deviceAddress +" is part of route.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "Founded "+ deviceAddress +" is not part of route.");
                        Toast.makeText(
                                activity,
                                "Founded "+ deviceAddress +" is not part of route.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (request != null) {
                    Log.i(TAG, "ACCESS_POINT Available Services request != null");
                    if (!activity.visited.containsKey(deviceAddress)) {
                        Log.i(TAG, "ACCESS_POINT Available Services not visited");
                        //activity.visited.put(deviceAddress, service);
                        activity.onServiceClick(service);
                    }
                }
                break;*/
            case RANGE_EXTENDER:
                DnsSdTxtRecord txtRecord = getHandler().getDnsSdTxtRecordMap().get(serviceKey);
                Log.i(WifiDirectHandler.TAG, "RANGE_EXTENDER Listening for connections.");
                Log.i(WifiDirectHandler.TAG, json.toJson(service));
                Log.i(WifiDirectHandler.TAG, json.toJson(txtRecord));

                if(txtRecord != null) {
                    Map record = txtRecord.getRecord();
                    activity.curRecord = record;

                    String deviceType = (String) record.get("DeviceType");
                    Log.i(AvailableServicesFragment.TAG, deviceType);

                    if (deviceType.equals(ACCESS_POINT_WREQ.toString())) {
                        Log.i(AvailableServicesFragment.TAG, "Pop request.");
                        activity.visited.put(deviceAddress, service);
                        activity.onServiceClick(service);
                    } else if (deviceType.equals(ACCESS_POINT_WRES.toString())) {
                        Log.i(AvailableServicesFragment.TAG, "Pop response.");
                        activity.visited.put(deviceAddress, service);
                        activity.onServiceClick(service);
                    }
                } else {
                    servicesListAdapter.removeItem(service);
                    Log.i(AvailableServicesFragment.TAG, "DnsSdTxtRecord is null.");
                }

                /*if (response != null) {
                    Log.i(TAG, "RANGE_EXTENDER Available Services response != null");
                    if (request.inRequest(deviceAddress)) {
                        activity.onServiceClick(service);
                        Log.i(TAG, "Founded "+ deviceAddress +" is part of route.");
                        Toast.makeText(
                                activity,
                                "Founded "+ deviceAddress +" is part of route.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "Founded "+ deviceAddress +" is not part of route.");
                        Toast.makeText(
                                activity,
                                "Founded "+ deviceAddress +" is not part of route.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (request != null) {
                    Log.i(TAG, "RANGE_EXTENDER Available Services request != null");
                    if (!activity.visited.containsKey(deviceAddress)) {
                        Log.i(TAG, "RANGE_EXTENDER Available Services not visited");
                        //activity.visited.put(deviceAddress, service);
                        activity.onServiceClick(service);
                    }
                }*/
                break;
            default:
                Log.e(WifiDirectHandler.TAG, "Undefined value for Group Owner Intent.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbar != null) {
            toolbar.setTitle("Service Discovery");
        }
    }

    /**
     * Shortcut for accessing the wifi handler
     */
    private WifiDirectHandler getHandler() {
        return wifiDirectHandlerAccessor.getWifiHandler();
    }

    /**
     * This is called when the Fragment is opened and is attached to MainActivity
     * Sets the ListAdapter for the Service List and initiates the service discovery
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            wifiDirectHandlerAccessor = ((WiFiDirectHandlerAccessor) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement WiFiDirectHandlerAccessor");
        }
    }
}
