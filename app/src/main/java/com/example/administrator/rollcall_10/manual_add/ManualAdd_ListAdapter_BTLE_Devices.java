package com.example.administrator.rollcall_10.manual_add;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.administrator.rollcall_10.R;

import java.util.ArrayList;

/**
 * Created by Kelvin on 5/7/16.
 */
public class ManualAdd_ListAdapter_BTLE_Devices extends ArrayAdapter<ManualAdd_BTLE_Device> {

    Context context;
    int layoutResourceID;
    ArrayList<ManualAdd_BTLE_Device> devices;

    public ManualAdd_ListAdapter_BTLE_Devices(Context context, int resource, ArrayList<ManualAdd_BTLE_Device> objects) {
        super(context.getApplicationContext(), resource, objects);

        this.context = context;
        this.layoutResourceID = resource;
        this.devices = objects;
    }



    @Override
    public  View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }


        ManualAdd_BTLE_Device device = devices.get(position);
        String name = device.getName();
        String address = device.getAddress();

        String Manual_name=device.get_Manual_Name();

        int rssi = device.getRSSI();

        TextView tv_name;
        TextView tv_address;
        TextView tv_rssi ;

        tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        tv_name.setText(device.getName());




            if (Manual_name != null && Manual_name.length() > 0) {
                tv_name.setText(device.get_Manual_Name());
            } else if(Manual_name == null) {
                tv_name.setText(name);
            }
                else{
                tv_name.setText("No Name");
            }



        tv_rssi = (TextView) convertView.findViewById(R.id.tv_rssi);
        tv_rssi.setText("RSSI: " + Integer.toString(rssi));

        tv_address = (TextView) convertView.findViewById(R.id.tv_macaddr);
        if (address != null && address.length() > 0) {
            tv_address.setText(device.getAddress());
        }
        else {
            tv_address.setText("No Address");
        }

        return convertView;
    }




    //**0826
    public void remove(int i) {
        devices.remove(devices.get(i));
}

}
