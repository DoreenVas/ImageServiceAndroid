package com.example.doreen.imageservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    /***
     *  Creates the component.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /***
     * Starts the Service function.
     * @param view
     */
    public void startService(View view){
        Intent intent=new Intent(this,ImageServiceService.class);
        startService(intent);
    }

    /***
     *  Stops the Service function.
     * @param view
     */
    public void stopService(View view){
        Intent intent = new Intent(this,ImageServiceService.class);
        stopService(intent);
    }
}
