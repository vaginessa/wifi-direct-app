package edu.rit.se.crashavoidance.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.rit.se.crashavoidance.R;
import edu.rit.se.crashavoidance.network.Device;
import edu.rit.se.crashavoidance.network.DeviceRequest;
import edu.rit.se.crashavoidance.network.DeviceResponse;
import edu.rit.se.crashavoidance.network.DeviceType;
import edu.rit.se.crashavoidance.network.Message;
import edu.rit.se.crashavoidance.network.MessageType;
import edu.rit.se.crashavoidance.network.ObjectType;
import edu.rit.se.wifibuddy.CommunicationManager;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with a send button.
 */
public class ChatFragment extends ListFragment {

    final Gson gson = new Gson();

    private EditText textMessageEditText;
    private ChatMessageAdapter adapter = null;
    private List<String> items = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private WiFiDirectHandlerAccessor handlerAccessor;
    private MainActivity activity = null;
    private Toolbar toolbar;
    private Button sendButton;
    private ImageButton cameraButton;
    private Button closeButton;
    private static final String TAG = WifiDirectHandler.TAG + "ListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        activity = (MainActivity) getActivity();

        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setEnabled(false);

        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);

        closeButton = (Button) view.findViewById(R.id.closeButton);

        textMessageEditText = (EditText) view.findViewById(R.id.textMessageEditText);
        textMessageEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(true);
            }
        });

        ListView messagesListView = (ListView) view.findViewById(android.R.id.list);
        adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1, items);
        messagesListView.setAdapter(adapter);
        messagesListView.setDividerHeight(0);

        // Prevents the keyboard from pushing the fragment and messages up and off the screen
        messagesListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        messagesListView.setStackFromBottom(true);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(WifiDirectHandler.TAG, "Send button tapped");
                CommunicationManager communicationManager = handlerAccessor.getWifiHandler().getCommunicationManager();
                if (communicationManager != null && !textMessageEditText.toString().equals("")) {
                    String message = textMessageEditText.getText().toString();
                    // Gets first word of device name
                    String author = handlerAccessor.getWifiHandler().getThisDevice().deviceName.split(" ")[0];
                    byte[] messageBytes = (author + ": " + message).getBytes();
                    Message finalMessage = new Message(MessageType.TEXT, messageBytes);
                    communicationManager.write(SerializationUtils.serialize(finalMessage));
                } else {
                    Log.e(TAG, "Communication Manager is null");
                }
                String message = textMessageEditText.getText().toString();
                if (!message.equals("")) {
                    pushMessage("Me: " + message);
                    messages.add(message);
                    Log.i(TAG, "Message: " + message);
                    textMessageEditText.setText("");
                }
                sendButton.setEnabled(false);
            }
        });

        toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerAccessor.getWifiHandler().removeGroup();
            }
        });

        switch (activity.deviceType) {
            case EMITTER:
                Log.i(TAG, "EMITTER");
                if (activity.curDevice != null) {
                    sendMessage(gson.toJson(activity.curDevice), ObjectType.HELLO);
                    Log.i(TAG, "Message sent successfully.");
                    returnToListFragment();
                }
                break;
            case QUERIER:
                Log.i(TAG, "QUERIER");
                if (activity.curRequest != null) {
                    Log.i(TAG, "Request");
                    sendMessage(gson.toJson(activity.curRequest), ObjectType.REQUEST);
                    Log.i(TAG, "Message sent successfully.");
                }
                break;
            case ACCESS_POINT:
                Log.i(TAG, "ACCESS_POINT");
                if (activity.curResponse != null) {
                    Log.i(TAG, "Response");

                    sendMessage(gson.toJson(activity.curResponse), ObjectType.RESPONSE);
                    Log.i(TAG, "Message sent successfully.");

                    //TODO: list containing sent requests & responses
                    activity.curResponse = null;
                    activity.curRequest = null;
                } else if (activity.curRequest != null) { //TODO: verify if it doesn't loop
                    Log.i(TAG, "Request");
                    sendMessage(gson.toJson(activity.curRequest), ObjectType.REQUEST);
                    Log.i(TAG, "Message sent successfully.");

                    //TODO: list containing sent requests & responses
                    activity.curRequest = null;
                }
                break;
            case RANGE_EXTENDER:
                Log.i(TAG, "RANGE_EXTENDER");
                if (activity.curResponse != null) {
                    Log.i(TAG, "Response");

                    sendMessage(gson.toJson(activity.curResponse), ObjectType.RESPONSE);
                    Log.i(TAG, "Message sent successfully.");

                    //TODO: list containing sent requests & responses
                    activity.curResponse = null;
                    activity.curRequest = null;
                } else if (activity.curRequest != null) { //TODO: verify if it doesn't loop
                    Log.i(TAG, "Request");
                    sendMessage(gson.toJson(activity.curRequest), ObjectType.REQUEST);
                    Log.i(TAG, "Message sent successfully.");

                    //TODO: list containing sent requests & responses
                    activity.curRequest = null;
                }
                break;
            default:
                Log.e(TAG, "Not defined device type.");
        }

        return view;
    }

    private void returnToListFragment() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                AvailableServicesFragment availableServicesFragment = new AvailableServicesFragment();
                activity.replaceFragment(availableServicesFragment);
                Log.i(TAG, "Switching to Services fragment");
            }
        }, 2000);
    }

    private void postDelayed(Runnable runnable, int time) {
        new Handler().postDelayed(runnable,time);
    }

    public void sendMessage(String message, ObjectType objectType) {
        Log.i(WifiDirectHandler.TAG, "Send button tapped");
        CommunicationManager communicationManager = handlerAccessor.getWifiHandler().getCommunicationManager();
        if (communicationManager != null && !textMessageEditText.toString().equals("")) {
            byte[] messageBytes = (message).getBytes();
            Message finalMessage = new Message(MessageType.TEXT, messageBytes);
            finalMessage.objectType = objectType;
            communicationManager.write(SerializationUtils.serialize(finalMessage));
        } else {
            Log.e(TAG, "Communication Manager is null");
        }
        //String message = textMessageEditText.getText().toString();
        if (!message.equals("")) {
            pushMessage("Me: " + message);
            messages.add(message);
            Log.i(TAG, "Message: " + message);
            textMessageEditText.setText("");
        }
        sendButton.setEnabled(false);
    }

    public interface MessageTarget {
        Handler getHandler();
    }

    public void pushMessage(byte[] readMessage) {
        Message message = SerializationUtils.deserialize(readMessage);
        switch(message.messageType) {
            case TEXT:
                Log.i(TAG, "Text message received");
                String str = new String(message.message);
                pushMessage(str);
                processMessage(message);
                break;
            case IMAGE:
                Log.i(TAG, "Image message received");
                Bitmap bitmap = BitmapFactory.decodeByteArray(message.message, 0, message.message.length);
                ImageView imageView = new ImageView(getContext());
                imageView.setImageBitmap(bitmap);
                loadPhoto(imageView, bitmap.getWidth(), bitmap.getHeight());
                break;
        }
    }

    public void pushMessage(String message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
    }

    public void processMessage(Message message) {
        String msg = new String(message.message);
        Log.i(TAG, ""+message);

        if (message.objectType == ObjectType.RESPONSE) {
            Log.i(TAG, "Processing RESPONSE ----> " + msg);
            DeviceResponse response = gson.fromJson(msg, DeviceResponse.class);

            response.route.add(activity.curDevice);

            activity.curResponse = response;

            handlerAccessor.getWifiHandler().removeGroup();

            returnToListFragment();

        } else if (message.objectType == ObjectType.REQUEST) {
            Log.i(TAG, "Processing REQUEST ----> " + msg);
            DeviceRequest deviceRequest = gson.fromJson(msg, DeviceRequest.class);
            activity.curRequest = deviceRequest;
            activity.deviceRequests.add(deviceRequest);

            if (activity.lookUp(deviceRequest)) {
                Log.i(TAG, "Device " + gson.toJson(deviceRequest) + " found.");
                DeviceResponse response = new DeviceResponse();
                response.resDevice = deviceRequest.reqDevice;
                response.resDevice.curCoordinates = activity.getCurrentLocation();
                response.resDate = activity.getCurrentDate();
                response.srcMAC = handlerAccessor.getWifiHandler().getThisDevice().deviceAddress;

                //TODO:verify if it's useful.
                response.route.add(activity.curDevice);

                activity.curResponse = response;
                Log.i(TAG, "Found: --------> " + gson.toJson(response) + " <--------");
                sendMessage(gson.toJson(response), ObjectType.RESPONSE);

                returnToListFragment();
            } else {
                Log.i(TAG, "Device " + gson.toJson(deviceRequest) + " not found.");

                //Sometimes the connection doesn't get stablished correctly at this point
                //so added this fix.
                if(deviceRequest.srcMAC.equals(handlerAccessor.getWifiHandler().getThisDevice().deviceAddress)) {
                    ChatFragment chatFragment = new ChatFragment();
                    activity.replaceFragment(chatFragment);
                } else {
                    sendMessage("", ObjectType.WAIT);

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handlerAccessor.getWifiHandler().removeGroup();

                            AvailableServicesFragment availableServicesFragment = new AvailableServicesFragment();
                            activity.replaceFragment(availableServicesFragment);
                            Log.i(TAG, "Switching to Services fragment");
                        }
                    }, 2000);
                }
            }
        } else if (message.objectType ==  ObjectType.HELLO) {
            Device device = gson.fromJson(msg, Device.class);
            activity.devices.add(device);
            Log.i(TAG, "Found: --------> " + device.nameID + " <--------");
            handlerAccessor.getWifiHandler().removeGroup();
            returnToListFragment();
        } else if (message.objectType == ObjectType.WAIT) {
            Log.i(TAG, "Processing WAIT ----> " + msg);
            handlerAccessor.getWifiHandler().removeGroup();
            returnToListFragment();
        }
    }

    public void pushImage(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Message message = new Message(MessageType.IMAGE, byteArray);
        CommunicationManager communicationManager = handlerAccessor.getWifiHandler().getCommunicationManager();
        Log.i(TAG, "Attempting to send image");
        communicationManager.write(SerializationUtils.serialize(message));
    }

    /**
     * ArrayAdapter to manage chat messages.
     */
    public class ChatMessageAdapter extends ArrayAdapter<String> {

        public ChatMessageAdapter(Context context, int textViewResourceId, List<String> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            String message = items.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText = (TextView) v.findViewById(android.R.id.text1);
                if (nameText != null) {
                    nameText.setText(message);
                    if (message.startsWith("Me: ")) {
                        // My message
                        nameText.setGravity(Gravity.RIGHT);
                    } else {
                        // Buddy's message
                        nameText.setGravity(Gravity.LEFT);
                    }
                }
            }
            return v;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle("Chat");
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textMessageEditText.getWindowToken(), 0);
    }

    /**
     * This is called when the Fragment is opened and is attached to MainActivity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            handlerAccessor = ((WiFiDirectHandlerAccessor) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement WiFiDirectHandlerAccessor");
        }
    }

    private void loadPhoto(ImageView imageView, int width, int height) {

        ImageView tempImageView = imageView;


        AlertDialog.Builder imageDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_fullimage_dialog,
                (ViewGroup) getActivity().findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(tempImageView.getDrawable());
        imageDialog.setView(layout);
        imageDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        imageDialog.create();
        imageDialog.show();
    }
}
