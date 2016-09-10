package com.allen.androideffect.cyclerview.PullView;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen.androideffect.R;
import com.allen.androideffect.cyclerview.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class PullViewActivity extends Activity {
    MyRecyclerAdapter adapter;
    ArrayList<Drawable> images = new ArrayList<>();
    private static List<ViewModel> items = new ArrayList<>();
    static {
        items.add(new ViewModel("Item1","1x1", R.drawable.item1));
        items.add(new ViewModel("Item2","1x1", R.drawable.item2));
        items.add(new ViewModel("Item3","1x1", R.drawable.item3));
        items.add(new ViewModel("Item4","1x1", R.drawable.item4));
        items.add(new ViewModel("Item5","1x1", R.drawable.item5));
        items.add(new ViewModel("Item6","1x1", R.drawable.item6));
        items.add(new ViewModel("Item7","1x1", R.drawable.item7));
        items.add(new ViewModel("Item8","1x1", R.drawable.item8));
        items.add(new ViewModel("Item9","1x1", R.drawable.item9));
        items.add(new ViewModel("Item10","1x1", R.drawable.item1));
        items.add(new ViewModel("Item11","1x1", R.drawable.item2));
        items.add(new ViewModel("Item12","1x1", R.drawable.item3));
        items.add(new ViewModel("Item13","1x1", R.drawable.item4));
        items.add(new ViewModel("Item14","1x1", R.drawable.item1));
        items.add(new ViewModel("Item15","1x1", R.drawable.item2));
        items.add(new ViewModel("Item16","1x1", R.drawable.item3));
        items.add(new ViewModel("Item17","1x1", R.drawable.item4));
        items.add(new ViewModel("Item18","1x1", R.drawable.item5));
        items.add(new ViewModel("Item19","1x1", R.drawable.item6));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_pullview);
        int size = items.size();
        for(int i = 0;i< size;i++){
            ViewModel mode = items.get(i);
            images.add(this.getResources().getDrawable(mode.getImage()));
        }
        final RecyclerView recycler = (RecyclerView) this.findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false);
        adapter = new MyRecyclerAdapter(this);
        recycler.setAdapter(adapter);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(layoutManager);
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
            holder.t_name.setText(model.getText());
            holder.t_size.setText(model.getText1());
            holder.t_image.setImageDrawable(images.get(position));
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.image_item, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onViewRecycled(MyViewHolder holder) {
            holder.t_image.animate().cancel();
            holder.t_image.setImageDrawable(null);
            holder.t_name.setText(null);
            holder.t_size.setText(null);
        }
        class MyViewHolder extends ViewHolder {
            TextView t_name;
            TextView t_size;
            ImageView t_image;

            public MyViewHolder(View view) {
                super(view);
                t_name = (TextView) view.findViewById(R.id.id_name);
                t_size = (TextView) view.findViewById(R.id.id_size);
                t_image = (ImageView) view.findViewById(R.id.id_image);
            }
        }
    }
}

