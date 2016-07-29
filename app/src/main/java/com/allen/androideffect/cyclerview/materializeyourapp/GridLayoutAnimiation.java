/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allen.androideffect.cyclerview.materializeyourapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.Button;

import com.allen.androideffect.R;
import com.allen.androideffect.cyclerview.ViewModel;

import java.util.ArrayList;
import java.util.List;


public class GridLayoutAnimiation extends Activity implements RecyclerViewAdapter.OnItemClickListener {

    private static List<ViewModel> items = new ArrayList<>();

    static {
        items.add(new ViewModel("Item1", R.drawable.item1));
        items.add(new ViewModel("Item2", R.drawable.item2));
        items.add(new ViewModel("Item3", R.drawable.item3));
        items.add(new ViewModel("Item4", R.drawable.item4));
        items.add(new ViewModel("Item5", R.drawable.item5));
        items.add(new ViewModel("Item6", R.drawable.item6));
        items.add(new ViewModel("Item7", R.drawable.item7));
        items.add(new ViewModel("Item8", R.drawable.item8));
        items.add(new ViewModel("Item9", R.drawable.item9));
    }

    private RecyclerView recyclerView;
    private Button mButton;
    private GridLayoutAnimationController mController;
    private GridLayoutAnimationController mController1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_grid);

        initRecyclerView();
        initButton();
        initAnimation();
        setRecyclerAdapter(recyclerView);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        //setRecyclerAdapter(recyclerView);
        //recyclerView.scheduleLayoutAnimation();
    }
    void initAnimation(){
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.slide_in_bottom);
        mController = new GridLayoutAnimationController(animation);
        mController.setColumnDelay(0.3f);
        mController.setRowDelay(0.3f);
        mController.setDirection(GridLayoutAnimationController.DIRECTION_TOP_TO_BOTTOM|GridLayoutAnimationController.DIRECTION_LEFT_TO_RIGHT);

        Animation animation1= AnimationUtils.loadAnimation(this,R.anim.slide_out_bottom);
        mController1 = new GridLayoutAnimationController(animation1);
        mController1.setColumnDelay(0.3f);
        mController1.setRowDelay(0.3f);
        mController1.setDirection(GridLayoutAnimationController.DIRECTION_BOTTOM_TO_TOP|GridLayoutAnimationController.DIRECTION_RIGHT_TO_LEFT);

    }
    private void initButton() {
        mButton = (Button) findViewById(R.id.start);

        mButton.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Log.d("allen","OnClickListener");
                                           v.post(new Runnable() {
                                               @Override
                                               public void run() {
                                                   recyclerView.setLayoutAnimation(mController);
                                                   recyclerView.startLayoutAnimation();
                                               }
                                           });
                                       }
                                   }

        );

        Button  mButton1 = (Button) findViewById(R.id.start1);
        mButton1.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Log.d("allen","OnClickListener");
                                           v.post(new Runnable() {
                                               @Override
                                               public void run() {
                                                   recyclerView.setLayoutAnimation(mController1);
                                                   recyclerView.startLayoutAnimation();
                                               }
                                           });
                                       }
                                   }

        );
    }
    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(glm);

    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(items);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, ViewModel viewModel) {

    }
}
