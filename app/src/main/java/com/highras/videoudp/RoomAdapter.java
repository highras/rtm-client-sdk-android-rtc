package com.highras.videoudp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author fengzi
 * @date 2022/5/11 10:51
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyViewHolder> {

    List<String> list;
    Context context;

    public RoomAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.room_member_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String content = list.get(position);
        holder.userTv.setText(content);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userTv = itemView.findViewById(R.id.user_id);
        }
    }
}
