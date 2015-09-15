package com.linroid.interview.teambition.widget;

import android.graphics.drawable.Drawable;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/15/15
 */
public class Action {
    int color;
    Drawable icon;
    ActionView.State state;

    public Action() {
    }

    public Action(int color, Drawable icon, ActionView.State state) {
        this.color = color;
        this.icon = icon;
        this.state = state;
    }
}
