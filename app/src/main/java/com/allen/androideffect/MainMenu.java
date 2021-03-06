package com.allen.androideffect;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allen.androideffect.custom.CustomViewMenu;
import com.allen.androideffect.cyclerview.RecyclerViewMenu;
import com.allen.androideffect.cyclerview.design.DesignMenu;
import com.allen.androideffect.listview.ListViewMenu;

import java.util.ArrayList;

public class MainMenu extends ListActivity implements AdapterView.OnItemClickListener {
    public final static String TAG = "AndroidEffect";
    private ArrayList<Class> list ;
    private ClassAdapter adapter;

    void  intList(){
        list = new ArrayList<Class>();
        list.add(ListViewMenu.class);
        list.add(RecyclerViewMenu.class);
        list.add(DesignMenu.class);
        list.add(CustomViewMenu.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main_list_activity);

        intList();
        adapter = new ClassAdapter(this, list);
        this.getListView().setAdapter(adapter );
        this.getListView().setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    class ClassAdapter extends BaseAdapter {

        ArrayList<Class> mList;
        Context mContext;

        public ClassAdapter(Context context, ArrayList<Class> list){
            mContext = context ;
            mList = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList.size();
        }

        @Override
        public Class getItem(int position) {
            // TODO Auto-generated method stub
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            TextView view = null;
            LayoutInflater inflater = LayoutInflater.from(mContext);
            if(convertView != null){
                view = (TextView) convertView;
            }else{
                view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);

            }
            view.setText(mList.get(position).getSimpleName());
            return view;
        }

        public void jumToSub(int position){
            Intent intent = new Intent();
            Class activity = mList.get(position);
            intent.setClassName(mContext, activity.getName());

            try{
                mContext.startActivity(intent);
            }catch(RuntimeException e){
                e.printStackTrace();
            }
        }

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        Log.d(TAG,"onItemClick: " + position);
        adapter.jumToSub(position);
    }
}
