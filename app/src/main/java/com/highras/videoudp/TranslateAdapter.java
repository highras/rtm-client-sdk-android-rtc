package com.highras.videoudp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

/**
 * @author fengzi
 * @date 2022/5/11 17:53
 */
public class TranslateAdapter extends RecyclerView.Adapter<TranslateAdapter.MyViewHolder> {

    List<Map<String, String>> mapList;
    Context context;

    public TranslateAdapter(Context context, List<Map<String, String>> mapList) {
        this.mapList = mapList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.translate_content_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder( MyViewHolder holder, int position) {
        Map<String, String> item = mapList.get(position);
        holder.userTv.setText(item.get("user"));
        holder.translateTv.setText(item.get("content"));
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userTv;
        TextView translateTv;

        public MyViewHolder( View itemView) {
            super(itemView);
            userTv = itemView.findViewById(R.id.user_tv);
            translateTv = itemView.findViewById(R.id.translate_tv);
        }
    }
}
