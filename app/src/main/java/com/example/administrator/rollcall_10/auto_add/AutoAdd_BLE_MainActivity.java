package com.example.administrator.rollcall_10.auto_add;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.administrator.rollcall_10.R;
import com.example.administrator.rollcall_10.demo.Utils;
import com.example.administrator.rollcall_10.device_io.Device_IO;
import com.example.administrator.rollcall_10.notifications.Successful_NotificationDisplayService;
import com.example.administrator.rollcall_10.rollcall_dialog.RollCall_Dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AutoAdd_BLE_MainActivity extends AppCompatActivity {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;

    private HashMap<String, Auto_BTLE_Device> mBTDevicesHashMap;
    private ArrayList<Auto_BTLE_Device> mBTDevicesArrayList;
    private AutoAdd_ListAdapter_BTLE_Devices adapter;

    ListView listView;

    int a=0;

    private AutoAdd_BroadcastReceiver_BTState mBTStateUpdateReceiver;


    private AutoAdd_BLE_Scanner_BTLE autoAdd_BLE_Scanner_BTLE;


    public int DeviceAmount = 1;

    Device_IO device_io =new Device_IO();

    Menu mymenu;
    MenuItem scan,countdown;


    private CountDownTimer mCountDown;

//    public  ArrayList<String> savepeople_address =new ArrayList<>();
//    public  ArrayList<String> savepeople_name =new ArrayList<>();


    private HashMap<String,String> ReadySaveToTxt_hashmap=new HashMap<>();



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

    void leavel_dailog(){
        onPause();
        RollCall_Dialog.Builder rd=new RollCall_Dialog.Builder(this);
        rd.setTitle( getResources().getString(R.string.LeaveThiePage)).
                setMessage( getResources().getString(R.string.LeaveThiePage_meassage)).
                setPositiveButton(getResources().getString(R.string.LeaveThiePage_btnYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        stopScan();
                        finish();
                    }
                }).
                setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        onRestart();
                    }
                });

        rd.show();


    }

    void List_hasno_data_warning(){
        onPause();
        RollCall_Dialog.Builder rd=new RollCall_Dialog.Builder(this);
        rd.setTitle(getResources().getString(R.string.NoAddAnyData)).
                setMessage(getResources().getString(R.string.DoNothing)).
                setPositiveButton(getResources().getString(R.string.LeaveThiePage_btnYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).
                setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        onRestart();
                    }
                });

        rd.show();
    }

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.autoadd_ble_activity_main);

        //**Actionbar跟標題資料
        Acttionbar_TitleData();

        Button buttons =(Button)findViewById(R.id.add);
        assert buttons != null;
        buttons.setOnClickListener(new View.OnClickListener() {

    @Override
    public void onClick(View view) {

        Bundle bundle = getIntent().getExtras();
        String Seletor_File=  bundle.getString("Selected_File_Path");


        stopScan();


        if(ReadySaveToTxt_hashmap.size() ==0) {


            Toast.makeText(view.getContext(), String.valueOf(R.string.YouNoAddAnyData), Toast.LENGTH_SHORT).show();


        }else {

            Intent startNotificationServiceIntent = new Intent(AutoAdd_BLE_MainActivity.this,Successful_NotificationDisplayService.class);

            startNotificationServiceIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Bundle bundle1=new Bundle();
            bundle1.putInt("List_size",ReadySaveToTxt_hashmap.size());
            bundle1.putString("List_name",bundle.getString("Selected_File_Name"));
            bundle1.putString("List_Path",bundle.getString("Selected_File_Path"));
            bundle1.putSerializable("Txt_path",ReadySaveToTxt_hashmap);

            startNotificationServiceIntent.putExtras(bundle1);

            startService(startNotificationServiceIntent);

            device_io.Auto_WriteDataToTxt(ReadySaveToTxt_hashmap,true,Seletor_File);


            finish();
        }
    }
});





        //用以檢查,是否用在設備上
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }



        //**掃描時間 先給2分鐘
        mBTStateUpdateReceiver = new AutoAdd_BroadcastReceiver_BTState(getApplicationContext());
        autoAdd_BLE_Scanner_BTLE = new AutoAdd_BLE_Scanner_BTLE(this,120000, -75);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();










        adapter = new AutoAdd_ListAdapter_BTLE_Devices(this, R.layout.autoadd_btle_device_list_item, mBTDevicesArrayList);

        listView=(ListView)findViewById(R.id.listView_autoadd);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String address = mBTDevicesArrayList.get(i).getAddress();




                  adapter.remove(i);
//                  savepeople_address.remove(address);
                ReadySaveToTxt_hashmap.remove(address);
                  mBTDevicesHashMap.remove(address);
                  adapter.notifyDataSetChanged();
            }
        });





        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {


                //***Dialog
                RollCall_Dialog rollCall_dialog = new RollCall_Dialog(adapterView.getContext());
                rollCall_dialog.setTitle(getResources().getString(R.string.RollCall_Dialog_Title_FindDevice));
                rollCall_dialog.setMessage((getResources().getString(R.string.RollCall_Dialog__Message_AddYesOrNo)));
                rollCall_dialog.setIcon(android.R.drawable.ic_dialog_info);
                rollCall_dialog.setCancelable(false);
                rollCall_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, AutoAdd_BLE_MainActivity.this.getString(R.string.RollCall_Dialog__Button_No), no);
                rollCall_dialog.setButton(DialogInterface.BUTTON_POSITIVE, AutoAdd_BLE_MainActivity.this.getString(R.string.RollCall_Dialog__Button_Yes), yes);
                rollCall_dialog.show();
                //***Dialog



                return true;
            }
        });





                startScan();

    }


    public DialogInterface.OnClickListener yes = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {


            Toast.makeText(listView.getContext(), "Coming Soon！！", Toast.LENGTH_SHORT).show();



        }

    };







    //判斷最終清單內有無資料
    boolean ListHasNoData(){

        if(ReadySaveToTxt_hashmap.size() == 0){
            return true;
        }
        return false;
    }


    //****Scan返回鍵(左上角鍵頭)監聽事件 Start***\\\
    @Override
    public Intent getSupportParentActivityIntent() {

        if(ListHasNoData()){
            List_hasno_data_warning();
        }
        else {
            leavel_dailog();
        }

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
        Log.e("1","123");
    }


    @Override
    public void onBackPressed() {
        if(ListHasNoData()){
            List_hasno_data_warning();
        }
        else {
            leavel_dailog();
        }

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






    public void addDevice(BluetoothDevice device, int rssi) {
        String device_name = device.getName();
        String device_address = device.getAddress();


        if (!mBTDevicesHashMap.containsKey(device_address)) {


            if(device_name !=null) {

                //if (device.getName().startsWith(I_Set_BLEDevice.device_startwith)) {//////////////////////////////開頭限制

                    Auto_BTLE_Device btleDevice = new Auto_BTLE_Device(device);
                    btleDevice.setRSSI(rssi);


                    mBTDevicesHashMap.put(device_address, btleDevice);
                    mBTDevicesArrayList.add(btleDevice);


                    ReadySaveToTxt_hashmap.put(device_address,device_name);




            }

        }


        else {
            mBTDevicesHashMap.get(device_address).setRSSI(rssi);

        }

        adapter.notifyDataSetChanged();
    }





    public DialogInterface.OnClickListener no = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {




        }

    };





    public Auto_BTLE_Device getDevice(int position) {

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

        countdown= mymenu.findItem(R.id.conutdown);
        scan = mymenu.findItem(R.id.action_scan).setIcon(R.drawable.stopscanbtn);
        countdown();

        return true;

    }


    public void countdown()

    {
        mCountDown = new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;


                String countdown_time = "剩下時間："+
                        (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))+":"+
                        (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                countdown.setTitle(countdown_time);
                scan.setIcon(R.drawable.stopscanbtn);


                if(countdown_time =="1"){
                    mCountDown.onFinish();

                }


            }

            public void onFinish() {

                countdown.setTitle("done");
                scan.setIcon(R.drawable.startscanbtn);
                Log.e("shawn","倒數結束");

            }
        }.start();

    }





    //**Toolbar元鍵控制
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (!autoAdd_BLE_Scanner_BTLE.isScanning()) {
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
