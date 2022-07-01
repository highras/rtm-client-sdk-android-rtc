package com.highras.voiceDemo.test2;

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.highras.voiceDemo.R;
import com.highras.voiceDemo.common.DisplayUtils;
import com.highras.voiceDemo.common.MyUtils;

import java.util.HashSet;

public class TestActivity2 extends AppCompatActivity {


    private PageScrollView mScrollView;
    private TextView mPageNumTv;
    LinearLayout linearLayout;
    int itemWidth1 = 0;

    int count = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        mScrollView = findViewById(R.id.psv_pages);
        mPageNumTv = findViewById(R.id.tv_page_num);
        itemWidth1 = DisplayUtils.getScreenWidth(this) / 3;

        mScrollView.setPageChangedListener(this, new PageScrollView.PageChangedListener() {
            @Override
            public void onPageChanged(int pageNum) {
                Log.d("fengzi", "onPageChanged: " + pageNum);
                mPageNumTv.setText("第" + pageNum + "页");
                int index = MyUtils.getIndex(count, pageNum);
                HashSet<Long> subUid = new HashSet<>();
                for (int i = index; i < index + 3; i++) {
                    if (i < count) {
                        Log.d("fengzi", "id is: " + i);
                    }
                }
            }
        });
        linearLayout = findViewById(R.id.linearlayout);
        initData();
    }


    private void initData() {
        for (int i = 0; i < count; i++) {
            addMember(i);
        }
    }

    private void addMember(long uid) {
        View view = getLayoutInflater().inflate(R.layout.member_item, null);
        TextView textView = view.findViewById(R.id.member_name);
        SurfaceView surfaceView = view.findViewById(R.id.member_surface);
        setSurfaceViewCorner(surfaceView, 30, uid);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(itemWidth1, RelativeLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        linearLayout.addView(view);
        textView.setText(String.valueOf(uid));
    }

    private void setSurfaceViewCorner(SurfaceView surfaceView, final float radius, long uid) {
        surfaceView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                int itemWidth = rect.right - rect.left;
                int leftMargin = 0;
                int topMargin = 0;
                Rect selfRect = new Rect(leftMargin, topMargin,
                        itemWidth - leftMargin,
                        rect.bottom - rect.top - topMargin);
                outline.setRoundRect(selfRect, radius);
                Log.d("fengzi", "fengzi: uid:" + uid);
                Log.d("fengzi", "getOutline: rect left:" + rect.left + " top:" + rect.top + " right:" + rect.right + " bottom:" + rect.bottom);
                Log.d("fengzi", "selfRect: selfRect left:" + selfRect.left + " top:" + selfRect.top + " right:" + selfRect.right + " bottom:" + selfRect.bottom);
            }
        });
        surfaceView.setClipToOutline(true);
    }


}