package com.highras.videoudp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highras.videoudp.R;
import com.highras.videoudp.weight.CustomSwitch;

import java.util.List;

/**
 * @author fengzi
 * @date 2022/2/18 12:15
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolderView> {
    List<Long> list;
    Context context;

    public UserAdapter(Context context, List<Long> list) {
        this.list = list;
        this.context = context;
    }

    static class MyHolderView extends RecyclerView.ViewHolder {
        TextView nickNameTv;
        CustomSwitch customSwitch;

        public MyHolderView( View itemView) {
            super(itemView);
            nickNameTv = itemView.findViewById(R.id.nickName_tv);
            customSwitch = itemView.findViewById(R.id.top_switch);
        }
    }

    
    @Override
    public MyHolderView onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new MyHolderView(view);
    }

    @Override
    public void onBindViewHolder( MyHolderView holder, int position) {
        long uid = list.get(position);
        holder.nickNameTv.setText(String.valueOf(uid));

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
        void clickItem(int position);
    }
}
