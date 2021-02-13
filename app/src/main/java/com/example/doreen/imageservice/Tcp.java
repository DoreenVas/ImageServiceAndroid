package com.example.doreen.imageservice;

import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Tcp implements Runnable{
    int port;
    String address;
    private NotificationManager nm;
    private NotificationCompat.Builder builder;

    /***
     * Initializes the Tcp components.
     * @param address
     * @param port
     */
    public Tcp(String address, int port) {
        this.port = port;
        this.address = address;
    }

    /***
     * Setter
     * @param nm
     */
    public void setNM(NotificationManager nm) {
        this.nm = nm;
    }

    /***
     * Setter
     * @param builder
     */
    public void setBuilder(NotificationCompat.Builder builder) {
        this.builder = builder;
    }

    /***
     * Goes over all the photos in the received file and returns a Collection of them.
     * @param dir
     * @return collection of pics.
     */
    private static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<File>();
        if(dir==null||dir.listFiles()==null){
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(listFileTree(entry));
        }
        return fileTree;
    }

    /***
     * gets bytes from bitmap.
     * @param bitmap
     * @return array of bytes.
     */
    private byte[] getBytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,70,stream);
        return stream.toByteArray();
    }

    /***
     * Converts an int to a four bytes array.
     * @param x
     * @return 4 byte array.
     */
    private static byte[] getBytes(int x){
        return new byte[]{
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>> 8),
                (byte)x
        };
    }

    /**
     * Connects to the server and sends header size, image size, header and image. Also updates the scroll bar.
     */
    @Override
    public void run() {
        try {
            int barState = 0;
            final int notify_id = 1;
            InetAddress serverAddr = InetAddress.getByName(address);
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, port);
            try {
                //Sends the message to the server
                OutputStream output = socket.getOutputStream();
                File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                if (dcim == null) {
                    return;
                }
                Collection<File> pics=listFileTree(dcim);
                int numberOfFiles = pics.size();
                if (pics != null) {
                    for (File pic : pics) {
                        //get header (picture name)
                        String path=pic.getPath();
                        String fileName=path.substring(path.lastIndexOf("/")+1);
                        byte[] header=fileName.getBytes();
                        byte[] headerSize= getBytes(header.length);
                        FileInputStream fis = new FileInputStream(pic);
                        Bitmap bm = BitmapFactory.decodeStream(fis);
                        byte[] img = getBytesFromBitmap(bm);
                        byte[] imgSize=getBytes(img.length);

                        output.write(headerSize);
                        output.write(imgSize);
                        output.write(header);
                        output.write(img);

                        barState = barState + 100 / numberOfFiles;
                            this.builder.setProgress(100, barState, false);
                            this.nm.notify(notify_id, builder.build());

                    }
                }
                output.flush();
                this.builder.setProgress(0, 0, false);
                builder.setContentText("Download complete");
                this.nm.notify(notify_id, this.builder.build());
            } catch (Exception e) {
                Log.e("TCP", "S:Error", e);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C:Error", e);
        }
    }
}
