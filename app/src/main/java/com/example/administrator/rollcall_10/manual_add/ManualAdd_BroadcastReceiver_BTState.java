package com.example.administrator.rollcall_10.manual_add;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.administrator.rollcall_10.demo.Utils;

/**
 * Created by Kelvin on 4/18/16.
 */
public class ManualAdd_BroadcastReceiver_BTState extends BroadcastReceiver {

    Context activityContext;

    public ManualAdd_BroadcastReceiver_BTState(Context activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Utils.toast(activityContext, "Bluetooth is off");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Utils.toast(activityContext, "Bluetooth is turning off...");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Utils.toast(activityContext, "Bluetooth is on");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Utils.toast(activityContext, "Bluetooth is turning on...");
                    break;
            }
        }
    }
}
