package com.highras.voiceDemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.highras.voiceDemo.R;
import com.highras.voiceDemo.model.Member;
import com.highras.voiceDemo.weight.CustomSwitch;

import java.util.List;

/**
 * @author fengzi
 * @date 2022/2/18 12:15
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyHolderView> {
    List<Member> list;
    Context context;

    public MemberAdapter(Context context, List<Member> list) {
        this.list = list;
        this.context = context;
    }

    static class MyHolderView extends RecyclerView.ViewHolder {
        SwitchCompat customSwitch;
        TextView uidText;
        TextView statusTextView;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);
            customSwitch = itemView.findViewById(R.id.subscribe_switch);
            uidText = itemView.findViewById(R.id.nameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }

    @NonNull
    @Override
    public MemberAdapter.MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subscrib_item, parent, false);
        return new MyHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.MyHolderView holder, int position) {
        Member item = list.get(position);
        holder.uidText.setText(String.valueOf(item.uid));
        if (item.subscribe) {
            holder.customSwitch.setChecked(true);
            holder.statusTextView.setText("取消订阅");
        } else {
            holder.customSwitch.setChecked(false);
            holder.statusTextView.setText("订阅");
        }
        holder.customSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onClicklistener.clickItem(position, item.uid, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private OnClickListener onClicklistener;

    public void setOnClickListener(OnClickListener clickListener) {
        this.onClicklistener = clickListener;
    }

    public interface OnClickListener {
        void clickItem(int position, long uid, Boolean isOff);
    }
}
