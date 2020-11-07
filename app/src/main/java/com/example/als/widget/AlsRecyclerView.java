package com.example.als.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AlsRecyclerView extends RecyclerView {

    private List<View> mNonEmptyViews = Collections.emptyList();
    private List<View> mEmptyViews = Collections.emptyList();

    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            toggleViews();
        }
    };

    private void toggleViews() {
        if(getAdapter() != null && !mEmptyViews.isEmpty()){
            if(getAdapter().getItemCount() == 0){

                //show all the empty views
                for(View view : mEmptyViews){
                    view.setVisibility(View.VISIBLE);
                }

                //hide the Recycler View
                setVisibility(View.GONE);

                //hide all the views which are meant to be hidden
                for(View view:mNonEmptyViews){
                    view.setVisibility(View.GONE);
                }


            }
            else{
                //hide all the emptyViews
                for(View view : mEmptyViews){
                    view.setVisibility(View.GONE);
                }

                //show the recycler view
                setVisibility(View.VISIBLE);

                //hide all the views which are meant to be hidden
                for(View view : mNonEmptyViews){
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public AlsRecyclerView(@NonNull Context context) {
        super(context);
    }

    public AlsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null){
            adapter.registerAdapterDataObserver(observer);
        }

        observer.onChanged();
    }

    public void hideIfEmpty(View ...views) {
        mNonEmptyViews = Arrays.asList(views);
    }

    public void showIfEmpty(View ...emptyViews) {
        mEmptyViews = Arrays.asList(emptyViews);
    }
}
