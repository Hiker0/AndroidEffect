package com.allen.androideffect.cyclerview.itemanimation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Administrator on 2016/7/25.
 */
public class CustomAnimator extends RecyclerView.ItemAnimator  {
    final static String TAG = "allen";

    CustomAnimator(){

    }
    @Override
    public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        Log.d(TAG,"animateDisappearance");
        return false;
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        Log.d(TAG,"animateAppearance");
        return false;
    }

    @Override
    public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        Log.d(TAG,"animatePersistence");
        return false;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        Log.d(TAG,"animateChange");
        return false;
    }

    @Override
    public void runPendingAnimations() {
        Log.d(TAG,"runPendingAnimations");
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        Log.d(TAG,"endAnimation");
    }

    @Override
    public void endAnimations() {
        Log.d(TAG,"endAnimations");
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
