package com.example.administrator.rollcall_10.navigationdrawer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.rollcall_10.device_io.I_File_Path;
import com.example.administrator.rollcall_10.R;
import com.example.administrator.rollcall_10.recyclerview.List_Long_click_RecyclerviewAdapter;
import com.example.administrator.rollcall_10.recyclerview.Recyclerview_WatchList;
import com.example.administrator.rollcall_10.device_io.Device_IO;
import com.example.administrator.rollcall_10.rollcall_dialog.RollCall_Dialog;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 2016/8/6.
 */

public class mainview_fragmentlayout_EditList extends Fragment {


    private Device_IO device_io =new Device_IO();
    private File selected;
    private ListView FileList;
    private File_PeopleList_Adapter file_peopleList_adapter;
    private Context mContext;
    private ArrayList<File> files;
    public static File PeopleList;
    private static mainview_fragmentlayout_EditList mainview_fragmentlayout_editList;





    public static mainview_fragmentlayout_EditList mainview_fragmentlayout_editList_getIntace(){

        if(mainview_fragmentlayout_editList==null){
            mainview_fragmentlayout_editList=new mainview_fragmentlayout_EditList();
        }

        return mainview_fragmentlayout_editList;
    }

   public static  ArrayList<String> fileList=new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //換標題
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.RollCall_Fragment_Title_EditList);

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View frist_view=inflater.inflate(R.layout.mainview_fragmentlayout_editlist,null);

        return frist_view;
    }




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


           mContext = getActivity().getApplicationContext();



            PeopleList =new File(I_File_Path.path_People_list);
            PeopleList.mkdirs();

           files = filter(PeopleList.listFiles());



            FileList = (ListView)getActivity().findViewById(R.id.list);

            FileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                selected = new File(String.valueOf(files.get(position)));

                if(selected.length()==0){
                    Snackbar.make(v,  getResources().getString(R.string.RollCall_Dialog_Title_ListEmpty),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {

                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClass(v.getContext(), Recyclerview_WatchList.class);

                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Bundle bundle = new Bundle();

                    bundle.putSerializable("Txt_path",device_io.HashMap_ReadDataFromTxt(String.valueOf(files.get(position))));
                    bundle.putString("List_Name",selected.getName());

                    bundle.putString("List_Path",selected.getPath());

                            it.putExtras(bundle);

                    v.getContext(). startActivity(it);

                }


            }
        });


        //**長按事件
        FileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selected = new File(String.valueOf(files.get(position)));

                List_long_onclick();


                return true;
            }
        });


        file_peopleList_adapter = new File_PeopleList_Adapter();

        FileList.setEmptyView(getActivity().findViewById(R.id.nofileicon));

        FileList.setAdapter(file_peopleList_adapter);

    }





    private void List_long_onclick(){



        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog_list_longclick_layout, null);

        TextView title_txt=(TextView)layout.findViewById(R.id.Dialog_Title);

        title_txt.setText(selected.getName().substring(0,selected.getName().length()-4));


        RecyclerView recyclerView=(RecyclerView)layout.findViewById(R.id.recyclerView_list_long_click);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String item_txt[]={"重新命名","刪除"};


        int item_image[]={R.mipmap.pen_rename,R.mipmap.recyclerbin64};



        final RollCall_Dialog rollCall_dialog = new RollCall_Dialog(layout.getContext());
        rollCall_dialog.setView(layout);
        rollCall_dialog.setIcon(R.mipmap.dialogscanicon128);
        rollCall_dialog.setCancelable(false);
        rollCall_dialog.setCancelable(true);

        ///**關閉dialog
        Button btn_close =(Button)layout.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollCall_dialog.dismiss();
            }
        });

        List_Long_click_RecyclerviewAdapter recyclerviewAdapter_long_click =new List_Long_click_RecyclerviewAdapter(item_txt,item_image,selected,getActivity(),rollCall_dialog);

        recyclerView.setAdapter(recyclerviewAdapter_long_click);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        rollCall_dialog.show();


    }







    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_file, menu);

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        switch (id) {
            case R.id.action_create_file:
                Create_File_Dialog();

                return true;


        }

        return false;
    }




    //****新增檔案的Dialog
    public void Create_File_Dialog(){

        LoadingData();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog_create_file_layout, null);



        final RollCall_Dialog rollCall_dialog = new RollCall_Dialog(layout.getContext());
        rollCall_dialog.setView(layout);
        rollCall_dialog.setIcon(R.mipmap.dialogscanicon128);
        rollCall_dialog.setCancelable(false);
        rollCall_dialog.setCancelable(true);

        ///**關閉dialog
        Button btn_close =(Button)layout.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollCall_dialog.dismiss();
            }
        });

        //**編輯的dialog的確認鍵
        Button btn_ok =(Button)layout.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                EditText FileName_edit = (EditText) (layout.findViewById(R.id.create_file_name_edit));

                String Filename_string =FileName_edit.getText().toString();

                String NewList = Filename_string+".txt";

                if(Filename_string.contains(I_File_Path.Slash) || Filename_string.startsWith(" ") ||Filename_string.endsWith(" ") || Filename_string.contains(I_File_Path.Slash2)) {

                    Toast.makeText(getActivity(), getResources().getText(R.string.RollCall__NewFile_Dialog__Error_Messages), Toast.LENGTH_SHORT).show();


                }
                else if(FileName_edit.length() ==0){

                    Toast.makeText(getActivity(),  String.valueOf(R.string.NotYetEnteredFileName), Toast.LENGTH_SHORT).show();
                }

                else if(fileList.contains(NewList))
                {
                    Toast.makeText(getActivity(), getResources().getString(R.string.DuplicateName), Toast.LENGTH_SHORT).show();
                }


                else {
                    //**自行創建文字檔
                    File peoplefile = new File(I_File_Path.path_People_list + I_File_Path.Slash + Filename_string + I_File_Path.TextFile);

                    try {
                        peoplefile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                }


                FragmentManager fragmentManager = getActivity().getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, new mainview_fragmentlayout_EditList())
                        .commitAllowingStateLoss();

                rollCall_dialog.dismiss();


            }



        });

        rollCall_dialog.show();

    }




    public static ArrayList<File> filter(File[] fileList) {
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











    private class File_PeopleList_Adapter extends BaseAdapter {

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

            //**將.txt隱藏起來
            holder.textView.setText(fileName.substring(0,fileName.length()-4));
            holder.imageView.setImageResource(R.mipmap.txt_list64);




            return v;
        }

        private class Holder{
            TextView textView;
            ImageView imageView;

        }


    }

    public static void LoadingData(){

        for(int a=0;a<filter(PeopleList.listFiles()).size();a++)
        {
            fileList.add(filter(PeopleList.listFiles()).get(a).getName());
        }

    }


}

