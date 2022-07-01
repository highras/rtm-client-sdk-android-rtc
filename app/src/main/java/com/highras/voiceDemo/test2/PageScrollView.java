package com.highras.voiceDemo.test2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

import com.highras.voiceDemo.common.DisplayUtils;

public class PageScrollView extends HorizontalScrollView {
    private static final String TAG = "PageScrollView";
    public int PAGE_WIDTH = 0;
    public static final int DISTANCE_LIMIT = 300;
    public static final float SCROLL_CRITICAL_SPEED = 1000f;
    private static final int TO_SECOND = 1000;
    private Scroller mScroller;
    private int mDownX;
    private long mDownTime;
    private PageChangedListener mPageChangedListener;

    public void setPageChangedListener(Context context, PageChangedListener pageChangedListener) {
        mPageChangedListener = pageChangedListener;
        this.PAGE_WIDTH = DisplayUtils.getScreenWidth(context);
    }

    public PageScrollView(Context context) {
        super(context);
        mScroller = new Scroller(context);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }


    @Override
    public void fling(int velocityX) {
        super.fling(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /**
                 * 记录mDownX，以判断滑动方向
                 * 如果滑动临界是页宽的一半，不用记录mDownX
                 */
                mDownX = getScrollX();
                mDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                int currX = getScrollX();
                int finalX = findFinalX(currX);
                if (currX - mDownX != 0) { //用来判断是否滑动到最后
                    mScroller.startScroll(currX, 0, finalX - currX, 0);
                    invalidate();
                    if (mPageChangedListener != null) {
                        mPageChangedListener.onPageChanged(finalX / PAGE_WIDTH);
                    }
                }
                break;
            default:
                // Do nothing.
        }
        return super.onTouchEvent(event);
    }


    /**
     * 获取scroll终点（较为通用）
     */
    private int findFinalX(int currX) {
        int remainder = currX % PAGE_WIDTH;
        int multiple = currX / PAGE_WIDTH;
        float speed = (currX - mDownX) * TO_SECOND / (System.currentTimeMillis() - mDownTime);
        Log.d(TAG, "scroll speed = " + speed);
        if (speed < SCROLL_CRITICAL_SPEED && speed > -SCROLL_CRITICAL_SPEED) {
            //滑动速度慢，才会判断距离
            if (remainder < DISTANCE_LIMIT) {
                return PAGE_WIDTH * multiple;
            }
            if (remainder > PAGE_WIDTH - DISTANCE_LIMIT) {
                return PAGE_WIDTH * (multiple + 1);
            }
        }

        /**
         * 滑动速度快，直接走以下步骤
         */
        if (currX > mDownX) {
            return PAGE_WIDTH * (multiple + 1);
        } else {
            return PAGE_WIDTH * (multiple);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }
    }

    public void refreshUI() {
        invalidate();
    }

    public interface PageChangedListener {
        void onPageChanged(int pageNum);
    }
}
