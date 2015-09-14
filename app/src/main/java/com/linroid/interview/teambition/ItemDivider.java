package com.linroid.interview.teambition;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 */
public class ItemDivider extends RecyclerView.ItemDecoration {
    private int space;

    /**
     * @param resources Resources
     * @param dp 分割的高度
     */
    public ItemDivider(Resources resources, int dp) {
        super();
        this.space = (int) (resources.getDisplayMetrics().density * dp);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = space;
        }
    }
}
