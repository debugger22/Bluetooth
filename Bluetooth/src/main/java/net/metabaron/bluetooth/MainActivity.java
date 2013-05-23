package net.metabaron.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.*;

import java.util.Set;

public class MainActivity extends Activity {

    protected BluetoothAdapter myBluetoothAdapter;

    {
        myBluetoothAdapter = null;
    }

    protected int REQUEST_ENABLE_BT;

    {
        REQUEST_ENABLE_BT = 1;
    }

    private ArrayAdapter<String> mArrayAdapter;
    private ListView bluetoothListView;
    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothListView = (ListView) findViewById(R.id.listView);
        description = (TextView) findViewById((R.id.textView));

        /* Initialize Bluetooth adapter */
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            System.out.println("No Bluetooth Adapter");
            description.setText(getString(R.string.noBTAdapterDetected));
        } else {
            System.out.println("Bluetooth");
            description.setText(getString(R.string.BTAdapterDetected));
            if (!myBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth Adapter off");
                description.setText(getString(R.string.enablingBTAdapter));
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                dealBluetooth();
            } else {
                System.out.println("Bluetooth adapter is on");
                description.setText(getString(R.string.BTAdapterOn));
                dealBluetooth();
            }
        }

        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Item clicked: " + i);
            }
        });
    }

    /* Discover new devices */
    public void startDiscovery(View view){
        System.out.println("Discovery starting");
        description.setText(getString(R.string.discoveryStarted));
        myBluetoothAdapter.startDiscovery();
        /* Register the BroadcastReceiver */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    /* Create a BroadcastReceiver for ACTION_FOUND */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /* When discovery finds a device */
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("BT device found");
                description.setText(getString(R.string.deviceFound));
                /* Get the BluetoothDevice object from the Intent */
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            if (mArrayAdapter.getCount() != 0) {
                bluetoothListView.setAdapter(mArrayAdapter);
            } else {
                System.out.println("No devices discovered");
                description.setText(getString(R.string.noDeviceDiscovered));
            }
            myBluetoothAdapter.cancelDiscovery();
        }
    };

    /* Click on button to discover new devices */
    private void dealBluetooth() {
        System.out.println("Bluetooth ON = Good to go!");
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            /* Loop through paired devices */
            for (BluetoothDevice device : pairedDevices) {
                System.out.println(device.getAddress());
                mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            if (mArrayAdapter.getCount() != 0) {
                bluetoothListView.setAdapter(mArrayAdapter);
            } else {
                System.out.println("No devices discovered");
                description.setText(getString(R.string.noDeviceDiscovered));
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                System.out.println("User enabled Bluetooth");
                description.setText(getString(R.string.enabledBluetooth));
                dealBluetooth();
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("User disabled Bluetooth");
                description.setText(getString(R.string.disablingBluetooth));
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
            System.out.println("No registered");
        }
    }
}
