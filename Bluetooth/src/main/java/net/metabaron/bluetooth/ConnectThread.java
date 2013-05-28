package net.metabaron.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
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
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
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
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        System.out.println("Yeah. You paired the device");
        InputStream bis = null;
        try{
            bis = mmSocket.getInputStream();
            byte[] buffer = new byte[4096];
            int read = bis.read(buffer, 0, 4096);
            while (read != -1) {
                byte[] tempdata = new byte[read];
                System.arraycopy(buffer, 0, tempdata, 0, read);
                System.out.println(new String(tempdata));
                read = bis.read(buffer, 0, 4096); // This is blocking
            }
        }catch (IOException e){
            System.out.println("Error: " + e.toString());
        }
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
