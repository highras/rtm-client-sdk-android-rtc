package com.highras.voiceDemo.common;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * @author fengzi
 * @date 2022/6/6 10:08
 */
public class MyUtils {
    public static boolean isEmpty(String data) {
        return data == null || data.length() == 0;
    }

    public static void setSurfaceViewCorner(SurfaceView surfaceView, final float radius) {
        surfaceView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                int itemWidth1 = rect.right - rect.left;
                int leftMargin = 0;
                int topMargin = 0;
                Rect selfRect = new Rect(leftMargin, topMargin,
                        itemWidth1 - leftMargin,
                        rect.bottom - rect.top - topMargin);
                outline.setRoundRect(selfRect, radius);
            }
        });
        surfaceView.setClipToOutline(true);
    }

    public static int getIndex(int count, int currentPage) {
        if (currentPage > getMaxPage(count)) {
            currentPage = getMaxPage(count);
        }
        int duoyu = count % 3;
        int pageCount = duoyu > 0 ? count / 3 : count / 3 - 1;
        int index = 0;
        if (currentPage == pageCount && duoyu > 0) {
            index = currentPage * 3 - 3 + (count % 3);
        } else {
            index = currentPage * 3;
        }
        if (index < 0)
            index = 0;
        return index;
    }

    public static int getMaxPage(int count) {
        int duoyu = count % 3;
        return duoyu > 0 ? count / 3 : count / 3 - 1;
    }
}
