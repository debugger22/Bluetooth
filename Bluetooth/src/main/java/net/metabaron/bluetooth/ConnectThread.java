package net.metabaron.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Mathieu on 5/23/13.
 */
public class ConnectThread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter myBluetoothAdapter) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            //tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("12301101-0340-1070-8649-00805F9B34FB"));
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            System.out.println("Cannot retrieve Bluetooth socket: " + e.toString());
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            //java.io.IOException: read failed, socket might closed or timeout
            System.out.println("Connection to " + mmDevice.getName() + " at " + mmDevice.getAddress() + " failed: " + connectException.toString());
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                System.out.println("Close: " + closeException.toString());
            }
            return;
        }
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        System.out.println("Yeah. You paired the device");
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            System.out.println("Connection closed through CANCEL failed: " + e.toString());
        }
    }
}
