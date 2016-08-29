package com.example.administrator.rollcall_10;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AutoAdd_BLE_MainActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;

    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private AutoAdd_ListAdapter_BTLE_Devices adapter;

    ListView listView;

    int a=0;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;


    private AutoAdd_BLE_Scanner_BTLE autoAdd_BLE_Scanner_BTLE;


    public int DeviceAmount = 1;

    Device_IO device_io =new Device_IO();


    BTLE_Device btleDevice;

    Menu mymenu;
    MenuItem progress_menu_item,scan;




    ArrayList<String> savepeople =new ArrayList<>();



    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.test_ble_activity_main);

        //**Actionbar跟標題資料
        Acttionbar_TitleData();





        Button buttons =(Button)findViewById(R.id.add);
        assert buttons != null;

        buttons.setOnClickListener(new View.OnClickListener() {

    @Override
    public void onClick(View view) {

        Bundle bundle = getIntent().getExtras();
        String Seletor_File=  bundle.getString("Selected_File_Path");


        device_io.AutowriteData(savepeople,true,Seletor_File);

        stopScan();


        Toast.makeText(view.getContext(),"加入成功",Toast.LENGTH_SHORT).show();

        finish();



    }
});







        //用以檢查,是否用在設備上
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }



        //**掃描時間 先給1分鐘
        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        autoAdd_BLE_Scanner_BTLE = new AutoAdd_BLE_Scanner_BTLE(this,60000, -75);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();










        adapter = new AutoAdd_ListAdapter_BTLE_Devices(this, R.layout.test_btle_device_list_item, mBTDevicesArrayList);

        listView=(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String address = mBTDevicesArrayList.get(i).getAddress();




                  adapter.remove(i);
                  savepeople.remove(address);
                  mBTDevicesHashMap.remove(address);
                  adapter.notifyDataSetChanged();
            }
        });





        startScan();

    }







    //****Scan返回鍵(左上角鍵頭)監聽事件 Start***\\\
    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }
    //****Scan返回鍵(左上角鍵頭)監聽事件 End***\\\




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

    @Override
    protected void onPause() {
        super.onPause();

//        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

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
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
        else if (requestCode == BTLE_SERVICES) {
            // Do something
        }
    }
//
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Context context = view.getContext();



        //        Utils.toast(context, "List Item clicked");
        // do something with the text views and start the next activity.

//        stopScan();
        //********************* 裝置名稱
//        String name = mBTDevicesArrayList.get(position).getName();

//        String address = mBTDevicesArrayList.get(position).getAddress();


    }










    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!mBTDevicesHashMap.containsKey(address)) {


            BTLE_Device btleDevice = new BTLE_Device(device);
            btleDevice.setRSSI(rssi);

            mBTDevicesHashMap.put(address, btleDevice);
            mBTDevicesArrayList.add(btleDevice);

            savepeople.add(address);

//            Bundle bundle = getIntent().getExtras();
//            String Seletor_File=  bundle.getString("Selected_File_Path");
//
//
//
//            device_io.writeData(address,true,Seletor_File);
//




        }



        else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);

        }

        adapter.notifyDataSetChanged();
    }

//    public DialogInterface.OnClickListener yes = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//
////                    DeviceAmount++;
////
////                     String address = mBTDevicesArrayList.get(DeviceAmount-1).getAddress();
////
////
////                     Bundle bundle = getIntent().getExtras();
////                     String Seletor_File=  bundle.getString("Selected_File_Path");
////
////
////
////              device_io.writeData(address,true,Seletor_File);
//        }
//
//    };





    public DialogInterface.OnClickListener no = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {




        }

    };





    public BTLE_Device getDevice(int position) {

        return mBTDevicesArrayList.get(position);

    }











    public void startScan(){

        Bundle bundle = getIntent().getExtras();
        String Seletor_File=  bundle.getString("Selected_File_Path");
        File peoplefile = new File(Seletor_File);

        peoplefile.delete();////////////////////////////////////////////下次加入時 刪除之前的資料

        //****剛剛把資料刪掉 在建一個一樣的
        try {
            peoplefile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();

        autoAdd_BLE_Scanner_BTLE.start();



    }

    public void stopScan() {

        autoAdd_BLE_Scanner_BTLE.stop();


    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.scan_set, menu);
        mymenu = menu;
        progress_menu_item = mymenu.findItem(R.id.action_progress_show);
        //**一開始就掃描progress
        progress_menu_item.setActionView(R.layout.progress_scantime);

        scan = mymenu.findItem(R.id.action_scan).setIcon(R.drawable.stopscanbtn);


        return true;

    }


    //**Toolbar元鍵控制
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (!autoAdd_BLE_Scanner_BTLE.isScanning()) {
                    startScan();
                    //**progress開始
                    progress_menu_item.setActionView(R.layout.progress_scantime);


                    scan.setIcon(R.drawable.stopscanbtn);



                }
                else {
                    stopScan();
                    //**progress停止
                    progress_menu_item.setActionView(null);
                    scan.setIcon(R.drawable.startscanbtn);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //**Toolbar元鍵控制



}