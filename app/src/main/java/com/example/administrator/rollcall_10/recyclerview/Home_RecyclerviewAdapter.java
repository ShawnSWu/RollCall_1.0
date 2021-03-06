package com.example.administrator.rollcall_10.recyclerview;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.administrator.rollcall_10.Calendar.RollCallCalendar;
import com.example.administrator.rollcall_10.R;
import com.example.administrator.rollcall_10.RollCallGoogleMap.RollCall_GoogleMap;
import com.example.administrator.rollcall_10.ble_device_setting.Set_BLE_Device;
import com.example.administrator.rollcall_10.device_io.I_File_Path;
import com.example.administrator.rollcall_10.navigationdrawer.mainview_fragmentlayout_EditList;
import com.example.administrator.rollcall_10.note.Note_RecyclerView_Activity;
import com.example.administrator.rollcall_10.rollcall.RollCall_BLE_MainActivity;
import com.example.administrator.rollcall_10.rollcall_dialog.RollCall_Dialog;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 2016/8/9.
 */
public class Home_RecyclerviewAdapter extends RecyclerView.Adapter<Home_RecyclerviewAdapter.ViewHolder> {
    private Context context;


    File selected;

    private ListView FileList;
    private File PeopleList;

    private Context mContext;
    private ArrayList<File> files;


    public Home_RecyclerviewAdapter(Context context)
    {
     this.context=context;
    }



    private String[] titles = {
            I_CardView.RollCall,
            I_CardView.Set_BLE_Device,
            I_CardView.Memorandum,
            I_CardView.Calendar,
            I_CardView.googlemap,



        };



    private String[] details = {
            I_CardView.RollCall_detail,
            I_CardView.Set_BLE_Device_detail,
            I_CardView.Memorandum_detail,
            I_CardView.Calendar_detail,
            I_CardView.googlemap_detail,


           };



    private int[] images = {
            R.mipmap.rollcallicon256,
            R.mipmap.bluetooth_connect512,
            R.mipmap.remind512,
            R.mipmap.cardview_calendar256,
            R.mipmap.googlemap256,

       };



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.home_recyclerview_cardview, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);

        mContext = v.getContext().getApplicationContext();

        PeopleList =new File(I_File_Path.path_People_list);
        PeopleList.mkdirs();

        files = filter(PeopleList.listFiles());
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i)
    {
        viewHolder.itemTitle.setText(titles[i]);
        viewHolder.itemDetail.setText(details[i]);
        viewHolder.itemImage.setImageResource(images[i]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }






    class ViewHolder extends RecyclerView.ViewHolder{

        public int currentItem;
        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemDetail;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = (ImageView)itemView.findViewById(R.id.item_image);
            itemTitle = (TextView)itemView.findViewById(R.id.item_title);
            itemDetail =
                    (TextView)itemView.findViewById(R.id.item_detail);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                    switch (getAdapterPosition()){
                        case 0:
                            startscan(v);
                            break;


                        case 1:
                            Set_BLEDevice(v);

                            break;

                        case 2:
                            Note(v);

                            break;
                        case 3:
                            Intent Calendar=new Intent(mContext, RollCallCalendar.class);
                            Calendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(Calendar);

                            break;

                        case 4:
                            Intent mpa=new Intent(mContext, RollCall_GoogleMap.class);
                            mpa.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(mpa);

                            break;

                    }

                }
            });
        }


        //**開始掃描
        public void startscan(View v)
        {

            if(files.size() == 0)
            {
                Please_Add_List_First(v);
            }
            else
            {

                    LayoutInflater inflater = (LayoutInflater) v.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    final View layout = inflater.inflate(R.layout.dialog_listview_rollcall_seleted, null);

                    final RollCall_Dialog rollCall_dialog = new RollCall_Dialog(v.getContext());
                    Button btn_close = (Button) layout.findViewById(R.id.btn_close);

                    btn_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rollCall_dialog.dismiss();

                        }
                    });

                    rollCall_dialog.setView(layout);
                    rollCall_dialog.setIcon(R.mipmap.dialogscanicon128);
                    rollCall_dialog.setCancelable(false);
                    rollCall_dialog.setCancelable(true);
                    rollCall_dialog.show();

                    FileList = (ListView) layout.findViewById(R.id.dialog_list_seletor);
                    FileList.setAdapter(new File_RollCAll_Adapter());
                    FileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            selected = new File(String.valueOf(files.get(position)));

                            if (selected.length() == 0) {
                                Toast.makeText(v.getContext(), v.getResources().getString(R.string.ListIsNull), Toast.LENGTH_LONG).show();

                            } else {

                                Intent it = new Intent(Intent.ACTION_VIEW);
                                it.setClass(v.getContext(), RollCall_BLE_MainActivity.class);

                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                Bundle bundle = new Bundle();
                                bundle.putString("Selected_File_Path", selected.getPath());
                                bundle.putString("Selected_File_Name", selected.getName());
                                it.putExtras(bundle);

                                v.getContext().startActivity(it);

                                rollCall_dialog.dismiss();
                            }

                        }
                    });

            }

        }






        //**設置藍芽裝置
        public void Set_BLEDevice(View v)
        {

            if(files.size() ==0)
            {
                Please_Add_List_First(v);

            }else{
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClass(v.getContext(), Set_BLE_Device.class);

                v.getContext().startActivity(it);
            }
        }



        public void Note(View v)
        {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClass(v.getContext(), Note_RecyclerView_Activity.class);

                v.getContext(). startActivity(it);
        }



    }






    private  void Please_Add_List_First(View v)
    {
        FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment, mainview_fragmentlayout_EditList.mainview_fragmentlayout_editList_getIntace())
                .commitAllowingStateLoss();


        Snackbar.make(v,v.getResources().getString(R.string.RollCall_PleaseAddListFirst),
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    public ArrayList<File> filter(File[] fileList) {
        ArrayList<File> files = new ArrayList<File>();
        if(fileList == null){
            return files;
        }
        for(File file: fileList) {
            if(!file.isDirectory() && file.isHidden()) {
                continue;
            }
            files.add(file);
        }
        Collections.sort(files);
        return files;
    }



    private class File_RollCAll_Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Holder holder;
            if(v == null){


                v = LayoutInflater.from(mContext).inflate(R.layout.editlist_file, null);

                holder = new Holder();
                holder.textView = (TextView) v.findViewById(R.id.text);
                holder.imageView =(ImageView)v.findViewById(R.id.imageView);
                v.setTag(holder);


            } else{

                holder = (Holder) v.getTag();

            }

            String filePath = files.get(position).getPath();
            String fileName = FilenameUtils.getName(filePath);
            holder.textView.setText(fileName.substring(0,fileName.length()-4));
            holder.imageView.setImageResource(R.mipmap.txt_list64);




            return v;
        }

        private class Holder{
            TextView textView;
            ImageView imageView;

        }


    }




}