package com.linroid.interview.teambition;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 * 一些辅助方法
 */
public class SampleUtils {
    public static final int SAMPLE_TEM_COUNT = 50;
    /**
     * 创建示例数据
     */
    public static List<String> generateSampleListData(Resources resources) {
        List<String> data = new ArrayList<>(SAMPLE_TEM_COUNT);
        for (int i=0; i<SAMPLE_TEM_COUNT; i++) {
            String title = resources.getString(R.string.sample_title, i);
            data.add(title);
        }
        return data;
    }
}
