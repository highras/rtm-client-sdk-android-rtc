package com.highras.videoudp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highras.videoudp.R;
import com.highras.videoudp.model.VoiceMember;

import java.util.List;

/**
 * @author fengzi
 * @date 2022/5/16 19:00
 */
public class VoiceMemberAdapter extends RecyclerView.Adapter<VoiceMemberAdapter.MyHolderView> {

    private List<VoiceMember> list;
    private Context context;

    public VoiceMemberAdapter(Context context, List<VoiceMember> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.voice_member_item, parent, false);
        return new MyHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolderView holder, int position) {
        if (position < list.size()) {
            VoiceMember member = list.get(position);
            holder.nickNameTv.setText(member.getNickName() + "(" + member.getUid() + ")");
            if (System.currentTimeMillis() - member.getPreviousVoiceTime() < 2000) {
                holder.imageView.setVisibility(View.VISIBLE);
            } else {
                holder.imageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyHolderView extends RecyclerView.ViewHolder {
        TextView nickNameTv;
        ImageView imageView;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);
            nickNameTv = itemView.findViewById(R.id.name_tv);
            imageView = itemView.findViewById(R.id.voice_image);
        }
    }
}
