package com.feasycom.feasyhome.utils;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MaxHeightLinearLayoutManager extends LinearLayoutManager {
    private int mMaxLine;

    public MaxHeightLinearLayoutManager(Context context, int maxLine) {
        super(context);
        setMaxLine(maxLine);
    }

    public MaxHeightLinearLayoutManager(Context context, int orientation, boolean reverseLayout, int maxLine) {
        super(context, orientation, reverseLayout);
        setMaxLine(maxLine);
    }

    private void setMaxLine(int maxLine) {
        if (maxLine <= 0) {
            throw new IllegalArgumentException("maxLine must be greater than 0");
        }
        this.mMaxLine = maxLine;
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        if (getItemCount() <= mMaxLine || getItemCount() == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        View child = recycler.getViewForPosition(0);
        addView(child);
        measureChildWithMargins(child, 0, 0);
        int itemWidth = getDecoratedMeasuredWidth(child);
        int itemHeight = getDecoratedMeasuredHeight(child);
        removeAndRecycleView(child, recycler);

        int widthMode = View.MeasureSpec.getMode(widthSpec);
        int heightMode = View.MeasureSpec.getMode(heightSpec);
        int width = 0;
        int height = 0;

        if (getOrientation() == HORIZONTAL) {
            height = (heightMode == View.MeasureSpec.EXACTLY) ? View.MeasureSpec.getSize(heightSpec) : itemHeight;
            width = itemWidth * mMaxLine;
        } else {
            width = (widthMode == View.MeasureSpec.EXACTLY) ? View.MeasureSpec.getSize(widthSpec) : itemWidth;
            height = itemHeight * mMaxLine;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        if (getItemCount() <= mMaxLine) {
            return super.isAutoMeasureEnabled();
        }
        // 通常情况下，如果项目数超过最大行数，我们可能希望禁用自动测量以确保onMeasure被调用。
        // 但这里的具体行为取决于你的需求。你可能希望总是返回false来确保onMeasure被调用。
        // return false; // 如果总是想禁用自动测量，可以取消注释这行代码。
        return false; // 或者保持默认行为。
    }

}
