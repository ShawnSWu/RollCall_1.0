package com.example.administrator.rollcall_10.rollcall;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

;
import com.example.administrator.rollcall_10.demo.BroadcastReceiver_BTState;
import com.example.administrator.rollcall_10.demo.ListAdapter_BTLE_Devices;
import com.example.administrator.rollcall_10.R;
import com.example.administrator.rollcall_10.demo.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RollCall_BLE_MainActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener {
//    private final static String TAG = MainActivity.class.getSimpleName();
//    int Scan_Btn_Count=1;


    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;

    private HashMap<String, RollCall_BTLE_Device> mBTDevicesHashMap;
    private ArrayList<RollCall_BTLE_Device> mBTDevicesArrayList;
    private RollCall_ListAdapter_BTLE_Devices adapter;
    private ListView listView;



    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private RollCall_Scanner_BTLE mBTLeScanner;


    Menu mymenu;
    MenuItem scan,countdown;
    private CountDownTimer mCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.ble_activity_main);

        Acttionbar_TitleData();





        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();

        }


        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new RollCall_Scanner_BTLE(this,10000, -75);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new RollCall_ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);


        listView=(ListView)findViewById(R.id.listView_rollcall);
        listView.setAdapter(adapter);


        //*****原本scan按鍵  End***\\\
        startScan();

    }




    //**Actionbar跟標題
    public void Acttionbar_TitleData(){

        //****Scan返回鍵監聽事件
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //**掃描的清單名稱
        Bundle bundle = getIntent().getExtras();
        String Seletor_File_Name = bundle.getString("Selected_File_Name");

        //清單名稱當標題
        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle(Seletor_File_Name);
    }





    //****Scan返回鍵(左上角鍵頭)監聽事件 Start***\\\
    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }
    //****Scan返回鍵(左上角鍵頭)監聽事件 End***\\\










    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }






    @Override
    protected void onResume() {
        super.onResume();

//        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }





    ///**暫時停時 停止掃描
    @Override
    protected void onPause() {
        super.onPause();

//        unregisterReceiver(mBTStateUpdateReceiver);
        Log.e("1","shawn-pause停止掃描");
        stopScan();
    }







    ///***停止掃描
    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }
    ///***停止掃描






    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "請打開藍芽");
            }
        }
        else if (requestCode == BTLE_SERVICES) {
            // Do something
        }

    }


    ///**掃描到的每個裝置
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Context context = view.getContext();
//
////        Utils.toast(context, "List Item clicked");
//
//        // do something with the text views and start the next activity.
//
//        stopScan();
//        //********************* 裝置名稱
//        String name = mBTDevicesArrayList.get(position).getName();
//
//        String address = mBTDevicesArrayList.get(position).getAddress();
//
//        Intent intent = new Intent(this, Activity_BTLE_Services.class);
//        intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
//        intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
//        startActivityForResult(intent, BTLE_SERVICES);

    }
















    ///***由Bundle過來的路徑去掃描要的檔案
    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        String[] list = new String[15];


        //**從首頁 點名Bundle過來的資料
        Bundle bundle = getIntent().getExtras();
        String Seletor_File = bundle.getString("Selected_File_Path");



        if (!mBTDevicesHashMap.containsKey(address)) {


            String s = "";

            int abc = 0;

            FileReader fr = null;

            try {
                fr = new FileReader(Seletor_File);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedReader br = new BufferedReader(fr);


            do {
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                list[abc] = s;
                abc++;

            } while (s != null);




            if (Arrays.asList(list).contains(address)) {


                if (device.getName().startsWith("KSU")) {//////////////////////////////開頭限制


                    RollCall_BTLE_Device btleDevice = new RollCall_BTLE_Device(device);
                    btleDevice.setRSSI(rssi);

                    mBTDevicesHashMap.put(address, btleDevice);


                    mBTDevicesArrayList.add(btleDevice);
                }

            }






        }
        else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);

        }

        adapter.notifyDataSetChanged();
    }


















    public void startScan(){

        //*****原本scan按鍵
//        btn_Scan.setText("Scanning...");
        //*****原本scan按鍵


        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();

        mBTLeScanner.start();

    }

    public void stopScan() {
        //*****原本scan按鍵
//        btn_Scan.setText("Scan Again");
        //*****原本scan按鍵


        mBTLeScanner.stop();
    }

    public void countdown()

    {
        mCountDown = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;



                String countdown_time = "" + (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                countdown.setTitle(countdown_time);
                scan.setIcon(R.drawable.stopscanbtn);

                if(countdown_time== "1"){
                    mCountDown.onFinish();
                }



            }

            public void onFinish() {

                countdown.setTitle("done");
                scan.setIcon(R.drawable.startscanbtn);
                Log.e("1","倒數結束");

            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.scan_set, menu);
        mymenu = menu;

        countdown= mymenu.findItem(R.id.conutdown);
        scan = mymenu.findItem(R.id.action_scan).setIcon(R.drawable.stopscanbtn);
        countdown();
        return true;

    }

    

    //**Toolbar元鍵控制
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (!mBTLeScanner.isScanning()) {
                    startScan();
                    countdown();

                }
                else {
                    stopScan();
                    //**記時結束
                    mCountDown.cancel();
                    mCountDown.onFinish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //**Toolbar元鍵控制







}
