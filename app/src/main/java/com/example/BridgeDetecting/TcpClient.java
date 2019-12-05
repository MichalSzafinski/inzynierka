package com.example.BridgeDetecting;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();

    private Socket socket;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    private DataOutputStream mOutputStream;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    private String serverIp;
    private int serverPort;
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(String ip, int port, OnMessageReceived listener) {
        mMessageListener = listener;
        serverIp = ip;
        serverPort = port;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mBufferOut != null) {
//                    Log.d(TAG, "Sending: " + message);
//                    mBufferOut.println(message);
//                    mBufferOut.flush();
//                }
//            }
//        };
//        Thread thread = new Thread(runnable);
//        thread.start();
        if(mOutputStream!=null) {
            try {
                mOutputStream.writeBytes(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendDouble(double value) {
        if(mOutputStream!=null) {
            try {
                mOutputStream.writeDouble(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public boolean run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIp);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, serverPort);
            mOutputStream = new DataOutputStream(socket.getOutputStream());
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//            try {
//
//                //sends the message to the server
//                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//
//                //receives the message which the server sends back
//                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//
//                //in this while the client listens for the messages sent by the server
//                while (mRun) {
//
//                    mServerMessage = mBufferIn.readLine();
//
//                    if (mServerMessage != null && mMessageListener != null) {
//                        //call the method messageReceived from MyActivity class
//                        mMessageListener.messageReceived(mServerMessage);
//                    }
//
//                }
//
//                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
//
//            } catch (Exception e) {
//                Log.e("TCP", "S: Error", e);
//            } finally {
//                //the socket must be closed. It is not possible to reconnect to this socket
//                // after it is closed, which means a new socket instance has to be created.
//                socket.close();
//            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            return false;
        }
        return true;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}