package com.example.administrator.rollcall_10.rollcall;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.example.administrator.rollcall_10.demo.BroadcastReceiver_BTState;
import com.example.administrator.rollcall_10.R;
import com.example.administrator.rollcall_10.demo.Utils;
import com.example.administrator.rollcall_10.rollcall_dialog.RollCall_Dialog;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RollCall_BLE_MainActivity extends AppCompatActivity {


    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;

    private HashMap<String, RollCall_BTLE_Device> mBTDevicesHashMap;
    private ArrayList<RollCall_BTLE_Device> mBTDevicesArrayList;
    private RollCall_ListAdapter_BTLE_Devices adapter;
    private ListView listView;
    private RollCall_BTLE_Device btleDevice;
    private static String countdown_time;
    private boolean isscaning=false;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private RollCall_Scanner_BTLE mBTLeScanner;
    private Menu mymenu;
    private MenuItem scan,countdown;
    private CountDownTimer mCountDown;
    private String Seletor_File_Name;

    private HashMap<String,String> MainHashMapList=new HashMap<>();

    private ArrayList<String> RollCall_successful_key =new ArrayList<>();


    private ArrayList<String> ScanList_Name =new ArrayList<>();
    private ArrayList<String> ScanList_Key =new ArrayList<>();

    private void LoadingTxtData(){

        Bundle bundle = getIntent().getExtras();
        String Seletor_File = bundle.getString("Selected_File_Path");

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(Seletor_File);
            bufferedReader= new BufferedReader(fileReader);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while(bufferedReader.ready()){

                String data_split=bufferedReader.readLine();
                String[] array_data_split=data_split.split(",");
                ScanList_Name.add(array_data_split[0]);
                ScanList_Key.add(array_data_split[1]);
                MainHashMapList.put(array_data_split[1],array_data_split[0]);


            }

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void leavel_dailog(final Context context){
        onPause();
        RollCall_Dialog.Builder rd=new RollCall_Dialog.Builder(this);


        if(countdown_time!="DONE") {
            rd.setTitle("離開此頁面?").
                    setMessage("點名尚未結束,是否離開?").
                    setPositiveButton("確定離開", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopScan();
                            finish();
                        }
                    }).
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onRestart();
                            Toast.makeText(context, "繼續點名", Toast.LENGTH_LONG).show();
                        }
                    });

            rd.show();
        }else{

            rd.setTitle("離開此頁面?").
                    setMessage("確定離開?").
                    setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopScan();
                            finish();
                        }
                    }).
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onRestart();
                        }
                    });

            rd.show();
        }


    }


    //**Actionbar跟標題
    public void Acttionbar_TitleData(){

        //****Scan返回鍵監聽事件
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //**掃描的清單名稱
        Bundle bundle = getIntent().getExtras();
        Seletor_File_Name = bundle.getString("Selected_File_Name");

        //清單名稱當標題
        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle(Seletor_File_Name.substring(0,Seletor_File_Name.length()-4));



    }

    private  void ShowRollCallResults(){

        Intent it=new Intent();
        Bundle bundle=new Bundle();
        bundle.putStringArrayList("RollCall_successful_key",RollCall_successful_key);
        bundle.putSerializable("MainHashMapList",MainHashMapList);
        bundle.putString("Selected_File_Name",Seletor_File_Name);
        it.putExtras(bundle);

        it.setClass(this,RollCall_Result.class);

        startActivity(it);

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.ble_activity_main);


        LoadingTxtData();



        Acttionbar_TitleData();


        Button finsh_rollcall=(Button)findViewById(R.id.finsh_rollcall);
        assert finsh_rollcall != null;
        finsh_rollcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                RollCall_Dialog.Builder rd=new RollCall_Dialog.Builder(v.getContext());

                if(mBTLeScanner.isScanning()) {
                    onPause();
                    rd.setTitle("點名尚未結束?").
                            setMessage("是否結束點名,前往看結果?").
                            setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    stopScan();
                                    ShowRollCallResults();
                                    finish();
                                }
                            }).
                            setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    onRestart();
                                    Toast.makeText(v.getContext(), "繼續點名", Toast.LENGTH_LONG).show();
                                }
                            });

                    rd.show();
                }else{

                    ShowRollCallResults();
                }


            }
        });




        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();

        }


        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new RollCall_Scanner_BTLE(this,300000, -75);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new RollCall_ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);


        listView=(ListView)findViewById(R.id.listView_rollcall);
        listView.setAdapter(adapter);


        //*****原本scan按鍵  End***\\\
        startScan();

    }







    //****Scan返回鍵(左上角鍵頭)監聽事件 Start***\\\
    @Override
    public Intent getSupportParentActivityIntent() {
        leavel_dailog(this);
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

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mBTLeScanner.start();
    }





    ///**暫時停時 停止掃描
    @Override
    protected void onPause() {
        super.onPause();

//        unregisterReceiver(mBTStateUpdateReceiver);
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
        leavel_dailog(this);
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


    ///***由Bundle過來的路徑去掃描要的檔案
    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();


        if (!mBTDevicesHashMap.containsKey(address)) {

            if(device.getName() !=null) {



                    if (device.getName().startsWith(getResources().getString(R.string.app_name))) {//////////////////////////////開頭限制


                        btleDevice = new RollCall_BTLE_Device(device);


                        //**如果hashmap的key有的話 在顯示
                        for (int i = 0; i < ScanList_Key.size(); i++)
                        {
                            if (btleDevice.getAddress().equals(ScanList_Key.get(i)))
                            {
                                btleDevice.setName(ScanList_Name.get(i));

                                adapter.notifyDataSetChanged();

                                mBTDevicesHashMap.put(address, btleDevice);

                                RollCall_successful_key.add(address);

                                mBTDevicesArrayList.add(btleDevice);
                            }
                        }
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
        isscaning=true;
    }

    public void stopScan() {
        //*****原本scan按鍵
//        btn_Scan.setText("Scan Again");
        //*****原本scan按鍵

        isscaning=false;
        mBTLeScanner.stop();
    }

    public void countdown()

    {
        mCountDown = new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;



                countdown_time =getResources().getString(R.string.Remaining_time) +
                        (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))+":"+
                        (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                countdown.setTitle(countdown_time);
                scan.setIcon(R.drawable.stopscanbtn);

                if(countdown_time.equals("1")){
                    mCountDown.onFinish();
                }

            }

            public void onFinish() {

                countdown_time = "DONE";
                countdown.setTitle("done");

                scan.setIcon(R.drawable.startscanbtn);


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
                    mCountDown.start();


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

