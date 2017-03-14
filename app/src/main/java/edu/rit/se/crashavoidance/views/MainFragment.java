package edu.rit.se.crashavoidance.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import edu.rit.se.crashavoidance.R;
import edu.rit.se.crashavoidance.network.Device;
import edu.rit.se.crashavoidance.network.DeviceRequest;
import edu.rit.se.crashavoidance.network.DeviceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * The Main Fragment of the application, which contains the Switches and Buttons to perform P2P tasks
 */
public class MainFragment extends Fragment {

    private WiFiDirectHandlerAccessor wifiDirectHandlerAccessor;
    private EditText nameIDEditText;
    private Switch toggleWifiSwitch;
    private Switch serviceRegistrationSwitch;
    private Switch noPromptServiceRegistrationSwitch;
    private Switch goIntentRegistrationSwitch;
    private Switch rangeExtenderRegistrationSwitch;
    private Button discoverServicesButton;
    private Button lookForNameID;
    private AvailableServicesFragment availableServicesFragment;
    private MainActivity mainActivity;
    private Toolbar toolbar;
    private static final String TAG = WifiDirectHandler.TAG + "MainFragment";

    /**
     * Sets the layout for the UI, initializes the Buttons and Switches, and returns the View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sets the Layout for the UI
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        nameIDEditText = (EditText) view.findViewById(R.id.nameIDEditText);

        // Initialize Switches
        toggleWifiSwitch = (Switch) view.findViewById(R.id.toggleWifiSwitch);
        serviceRegistrationSwitch = (Switch) view.findViewById(R.id.serviceRegistrationSwitch);
        noPromptServiceRegistrationSwitch = (Switch) view.findViewById(R.id.noPromptServiceRegistrationSwitch);
        goIntentRegistrationSwitch = (Switch) view.findViewById(R.id.goIntentRegistrationSwitch);
        rangeExtenderRegistrationSwitch = (Switch) view.findViewById(R.id.rangeExtenderRegistrationSwitch);

        // Initialize Discover Services Button
        discoverServicesButton = (Button) view.findViewById(R.id.discoverServicesButton);

        lookForNameID = (Button) view.findViewById(R.id.lookForNameID);

        updateToggles();

        // Set Toggle Listener for Wi-Fi Switch
        toggleWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Enable or disable Wi-Fi when Switch is toggled
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "\nWi-Fi Switch Toggled");
                if(isChecked) {
                    // Enable Wi-Fi, enable all switches and buttons
                    getHandler().setWifiEnabled(true);
                } else {
                    // Disable Wi-Fi, disable all switches and buttons
                    getHandler().setWifiEnabled(false);
                }
            }
        });

        // Set Toggle Listener for Service Registration Switch
        serviceRegistrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Add or Remove a Local Service when Switch is toggled
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "\nService Registration Switch Toggled");
                if (isChecked) {
                    // Add local service
                    if (getHandler().getWifiP2pServiceInfo() == null) {
                        boolean isMaster = getHandler().getGoIntent() == WifiDirectHandler.GROUP_OWNER_INTENT_ACCESS_POINT;
                        HashMap<String, String> record = new HashMap<>();
                        record.put("Name", getHandler().getThisDevice().deviceName);
                        record.put("Address", getHandler().getThisDevice().deviceAddress);
                        record.put("DeviceType",  mainActivity.deviceType.toString());
                        getHandler().addLocalService("Wi-Fi Buddy", record);
                        noPromptServiceRegistrationSwitch.setEnabled(false);
                    } else {
                        Log.w(TAG, "Service already added");
                    }
                    discoverServicesButton.setEnabled(true);
                } else {
                    // Remove local service
                    getHandler().removeService();
                    noPromptServiceRegistrationSwitch.setEnabled(true);
                    discoverServicesButton.setEnabled(false);
                }
            }
        });

        rangeExtenderRegistrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getHandler().setGoIntent( WifiDirectHandler.GROUP_OWNER_INTENT_SLAVE );
                mainActivity.deviceType = isChecked ? DeviceType.RANGE_EXTENDER : DeviceType.EMITTER;
            }
        });

        goIntentRegistrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getHandler().setGoIntent( isChecked ?
                        WifiDirectHandler.GROUP_OWNER_INTENT_ACCESS_POINT :
                        WifiDirectHandler.GROUP_OWNER_INTENT_SLAVE );
                mainActivity.deviceType = isChecked ? DeviceType.ACCESS_POINT : DeviceType.EMITTER;
            }
        });

        // Set Click Listener for Discover Services Button
        discoverServicesButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Show AvailableServicesFragment when Discover Services Button is clicked
             */
            @Override
            public void onClick(View v) {
                Log.i(TAG, "\nDiscover Services Button Pressed");

                Device curDevice = new Device(
                        nameIDEditText.getText().toString(),
                        mainActivity.getCurrentLocation(),
                        mainActivity.getCurrentDate());

                curDevice.myMAC = getHandler().getThisDevice().deviceAddress;

                mainActivity.curDevice = curDevice;

                if (availableServicesFragment == null) {
                    availableServicesFragment = new AvailableServicesFragment();
                }
                mainActivity.replaceFragment(availableServicesFragment);
            }
        });

        lookForNameID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "\nLook for devices Button Pressed");

                Device curDevice = new Device(
                        nameIDEditText.getText().toString(),
                        mainActivity.getCurrentLocation(),
                        mainActivity.getCurrentDate());

                curDevice.myMAC = getHandler().getThisDevice().deviceAddress;

                mainActivity.curDevice = curDevice;

                Device reqDevice = new Device(
                        nameIDEditText.getText().toString(),
                        "",
                        "");

                DeviceRequest deviceRequest = new DeviceRequest(reqDevice,
                        getHandler().getThisDevice().deviceAddress);

                deviceRequest.route.add(mainActivity.curDevice);

                mainActivity.curRequest = deviceRequest;

                mainActivity.deviceType = DeviceType.QUERIER;

                getHandler().setGoIntent(WifiDirectHandler.GROUP_OWNER_INTENT_SLAVE);

                if (availableServicesFragment == null) {
                    availableServicesFragment = new AvailableServicesFragment();
                }
                mainActivity.replaceFragment(availableServicesFragment);
            }
        });

        toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);

        return view;
    }

    /**
     * Sets the Main Activity instance
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    /**
     * Sets the WifiDirectHandler instance when MainFragment is attached to MainActivity
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

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle("Wi-Fi Direct Handler");
    }

    /**
     * Shortcut for accessing the wifi handler
     */
    private WifiDirectHandler getHandler() {
        return wifiDirectHandlerAccessor.getWifiHandler();
    }

    private void updateToggles() {
        // Set state of Switches and Buttons on load
        Log.i(TAG, "Updating toggle switches");
        if(getHandler().isWifiEnabled()) {
            toggleWifiSwitch.setChecked(true);
            serviceRegistrationSwitch.setEnabled(true);
            noPromptServiceRegistrationSwitch.setEnabled(true);
            discoverServicesButton.setEnabled(true);
        } else {
            toggleWifiSwitch.setChecked(false);
            serviceRegistrationSwitch.setEnabled(false);
            noPromptServiceRegistrationSwitch.setEnabled(false);
            discoverServicesButton.setEnabled(false);
        }
    }

    public void handleWifiStateChanged() {
        if (toggleWifiSwitch != null) {
            if (getHandler().isWifiEnabled()) {
                serviceRegistrationSwitch.setEnabled(true);
                discoverServicesButton.setEnabled(true);
            } else {
                serviceRegistrationSwitch.setChecked(false);
                serviceRegistrationSwitch.setEnabled(false);
                discoverServicesButton.setEnabled(false);
            }
        }
    }
}
