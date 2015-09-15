package com.linroid.interview.teambition;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 */
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleHolder> {
    List<String> data;

    public SampleAdapter(List<String> data) {
        this.data = data;
    }

    @Override
    public SampleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_sample, parent, false);
        return new SampleHolder(view);
    }

    @Override
    public void onBindViewHolder(SampleHolder holder, int position) {
        String title = data.get(position);
        holder.titleTV.setText(title);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class SampleHolder extends RecyclerView.ViewHolder implements MailboxSwipeLayout.OnSwipeActionListener {

        @Bind(R.id.tv_title)
        TextView titleTV;
        @Bind(R.id.swipeLayout)
        MailboxSwipeLayout swipeLayout;

        public SampleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            swipeLayout.setOnSwipeActionListener(this);
            swipeLayout.close();
        }

        @DebugLog
        @Override
        public void onLeftNearAction() {
            remove(R.string.sample_left_near_action, getAdapterPosition());
        }

        @DebugLog
        @Override
        public void onLeftFarAction() {
            remove(R.string.sample_left_far_action, getAdapterPosition());
            swipeLayout.close();
        }

        @DebugLog
        @Override
        public void onRightNearAction() {
            Snackbar.make(swipeLayout, R.string.sample_right_near_action, Snackbar.LENGTH_SHORT).show();
            swipeLayout.close(true);
        }

        @DebugLog
        @Override
        public void onRightFarAction() {
            Snackbar.make(swipeLayout, R.string.sample_right_far_action, Snackbar.LENGTH_SHORT).show();
            swipeLayout.close(true);
        }

        @Override
        public void onLeftLongDragAction() {
            Snackbar.make(swipeLayout, R.string.sample_left_long_drag_action, Snackbar.LENGTH_SHORT).show();
            swipeLayout.close(true);
        }

        @Override
        public void onRightLongDragAction() {
            Snackbar.make(swipeLayout, R.string.sample_right_long_drag_action, Snackbar.LENGTH_SHORT).show();
            swipeLayout.close(true);
        }

        public void remove(int msgResId, final int position) {
            if (position < 0) {
                return;
            }
            final String title = data.remove(position);
            notifyItemRemoved(position);
            Snackbar.make(swipeLayout, msgResId, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.btn_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            data.add(position, title);
                            notifyItemInserted(position);
                        }
                    }).show();
        }
    }
}
