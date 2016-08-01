package com.allen.androideffect.cyclerview.refresh;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen.androideffect.R;
import com.allen.androideffect.cyclerview.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 */
public class SwipeRefreshLayoutActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    MyRecyclerAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private static List<ViewModel> items = new ArrayList<>();

    static {
        items.add(new ViewModel("Item1", "1x1", R.drawable.item1));
        items.add(new ViewModel("Item2", "1x1", R.drawable.item2));
        items.add(new ViewModel("Item3", "1x1", R.drawable.item3));
        items.add(new ViewModel("Item4", "1x1", R.drawable.item4));
        items.add(new ViewModel("Item5", "1x1", R.drawable.item5));
        items.add(new ViewModel("Item6", "1x1", R.drawable.item6));
        items.add(new ViewModel("Item7", "1x1", R.drawable.item7));
        items.add(new ViewModel("Item8", "1x1", R.drawable.item8));
        items.add(new ViewModel("Item9", "1x1", R.drawable.item9));
        items.add(new ViewModel("Item10", "1x1", R.drawable.item1));
        items.add(new ViewModel("Item11", "1x1", R.drawable.item2));
        items.add(new ViewModel("Item12", "1x1", R.drawable.item3));
        items.add(new ViewModel("Item13", "1x1", R.drawable.item4));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe_refresh);

        final RecyclerView recycler = (RecyclerView) this.findViewById(R.id.recycler);
        mSwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, OrientationHelper.VERTICAL, false);
        mAdapter = new MyRecyclerAdapter(this);
        recycler.setAdapter(mAdapter);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(layoutManager);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {


        private Context mContext;
        private LayoutInflater inflater;

        MyRecyclerAdapter(Context context) {
            this.mContext = context;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ViewModel model = items.get(position);
            holder.t_title.setText(model.getText());
            holder.t_image.setImageResource(model.getImage());
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.image_item1, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView t_title;
            ImageView t_image;

            public MyViewHolder(View view) {
                super(view);
                t_title = (TextView) view.findViewById(R.id.id_title);
                t_image = (ImageView) view.findViewById(R.id.id_image);
            }
        }
    }
}
