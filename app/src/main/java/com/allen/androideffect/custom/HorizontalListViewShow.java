package com.allen.androideffect.custom;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allen.androideffect.R;
import com.allen.androideffect.listview.circlelist.CircleAdapter;
import com.allen.androideffect.listview.circlelist.CircleAdapter2;

import java.util.ArrayList;

public class HorizontalListViewShow extends Activity {
    ArrayList<Integer> data;
    ListAdapter adapter;
    HorizontalListView listView;
    int[] images = {
            R.drawable.item1,
            R.drawable.item2,
            R.drawable.item3,
            R.drawable.item4,
            R.drawable.item5,
            R.drawable.item6,
            R.drawable.item7,
            R.drawable.item8,
            R.drawable.item9,
            R.drawable.item1,
            R.drawable.item2,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_horizontal_view);
        initData();

        listView = (HorizontalListView) findViewById(R.id.list);
        listView.setAdapter(new ImageAdapter(this, data));

    }

    private void initData() {

        data = new ArrayList<Integer>();
        for (int i : images) {
            data.add(i);
        }
    }
}
