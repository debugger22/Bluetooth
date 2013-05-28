package net.metabaron.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {
    /*
    Display elements
     */
    private TextView description;
    private Button BTDiscovery;
    private ListView bluetoothListView;
    private ArrayAdapter<String> mArrayAdapter;

    /*
    Variables
     */
    public BluetoothAdapter myBluetoothAdapter = null;
    protected int REQUEST_ENABLE_BT = 1;
    public static ArrayList<BluetoothDevice> myBTDevices = new ArrayList<BluetoothDevice>();

    /*
    Create a BroadcastReceiver for ACTIONS
    */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /*
            When a device is found
            */
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                description.setText(getString(R.string.deviceFound));
                /*
                Get the BluetoothDevice object from the Intent
                */
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try{
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    Log.v("BroadcastReceiver", "BT device found: " + device.getName() + " AND " + device.getAddress());
                    try{
                        myBTDevices.add(device);
                    }catch (Exception e){
                        Log.v("BroadcastReceiver", "Error when adding Bluetooth device: " + e.toString());
                    }
                }catch (NullPointerException e){
                    Log.v("BroadcastReceiver", "Value: " + device.toString());
                }
                if (mArrayAdapter.getCount() != 0) {
                    bluetoothListView.setAdapter(mArrayAdapter);
                } else {
                    Log.v("BroadcastReceiver", "No devices discovered");
                    description.setText(getString(R.string.noDeviceDiscovered));
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                BTDiscovery.setEnabled(false);
                Log.v("BroadcastReceiver", "Discovery started");
                BTDiscovery.setText(getString(R.string.discoveryStarted));
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                BTDiscovery.setEnabled(true);
                Log.v("BroadcastReceiver", "Discovery finished");
                BTDiscovery.setText(getString(R.string.discoveryFinished));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Display elements initialisation
         */
        bluetoothListView = (ListView) findViewById(R.id.listView);
        description = (TextView) findViewById(R.id.textView);
        BTDiscovery = (Button) findViewById(R.id.bluetoothDiscovery);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        /*
        Initialize Bluetooth adapter and deal with user not willing to enable it
         */
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Log.v("onCreate", "No Bluetooth Adapter detected");
            description.setText(getString(R.string.noBTAdapterDetected));
            BTDiscovery.setEnabled(false);
        } else {
            Log.v("onCreate", "Bluetooth Adapter detected");
            description.setText(getString(R.string.BTAdapterDetected));
            if (!myBluetoothAdapter.isEnabled()) {
                Log.v("onCreate", "Bluetooth Adapter off");
                description.setText(getString(R.string.enablingBTAdapter));
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                if(!myBluetoothAdapter.isEnabled()){
                    Log.v("onCreate", "Bluetooth Adapter off despite asking for it to the user");
                    description.setText(getString(R.string.enablingBTAdapter));
                    BTDiscovery.setEnabled(false);
                }else{
                    dealBluetooth();
                }
            } else {
                Log.v("onCreate", "Bluetooth adapter is on");
                description.setText(getString(R.string.BTAdapterOn));
                BTDiscovery.setEnabled(true);
                dealBluetooth();
            }
        }

        /*
        Dealing with a click on an item
         */
        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Log.v("setOnItemClickListener", "Item clicked: " + i);
                myBTDevices.get(i).fetchUuidsWithSdp();
                myBluetoothAdapter.cancelDiscovery();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("setOnItemClickListener", "Running pairing");
                        final ConnectThread temp = new ConnectThread(myBTDevices.get(i), myBluetoothAdapter);
                        temp.run();
                    }
                }).start();
            }
        });
    }

    /*
    New devices discovery
     */
    public void startDiscovery(View view){
        /*
        Initialization
         */
        Log.v("startDiscovery", "Discovery starting");
        description.setText(getString(R.string.discoveryStarted));
        mArrayAdapter.clear();
        myBTDevices = new ArrayList<BluetoothDevice>();
        if (myBluetoothAdapter.isDiscovering())
            myBluetoothAdapter.cancelDiscovery();

        myBluetoothAdapter.startDiscovery();
        /* Register the BroadcastReceiver */
        IntentFilter actionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, actionFound);

        IntentFilter discoveryStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, discoveryStarted);
        IntentFilter discoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, discoveryFinished);
    }

    /*
    Display paired devices
    NEED TO ADD PAIRED DEVICES TO A LIST OBJECT
     */
    private void dealBluetooth() {
        Log.v("dealBluetooth", "Bluetooth ON = Good to go!");
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            /* Loop through paired devices */
            for (BluetoothDevice device : pairedDevices) {
                System.out.println(device.getAddress());
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            if (mArrayAdapter.getCount() != 0) {
                bluetoothListView.setAdapter(mArrayAdapter);
            } else {
                Log.v("dealBluetooth", "No devices discovered");
                description.setText(getString(R.string.noDeviceDiscovered));
            }
        }
        BTDiscovery.setEnabled(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.v("onActivityResult", "User enabled Bluetooth");
                description.setText(getString(R.string.enabledBluetooth));
                dealBluetooth();
            } else if (resultCode == RESULT_CANCELED) {
                Log.v("onActivityResult", "User disabled Bluetooth");
                description.setText(getString(R.string.disablingBluetooth));
                description.setText(getString(R.string.disablingBluetooth));
                BTDiscovery.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myBluetoothAdapter.cancelDiscovery();
        try{
            unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){
            Log.v("onDestroy", "No registered");
        }
    }
}
