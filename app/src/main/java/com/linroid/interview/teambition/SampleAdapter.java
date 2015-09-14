package com.linroid.interview.teambition;

import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

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
        }

        @Override
        public void onLeftAction() {
            remove(R.string.sample_left_action, getAdapterPosition());
        }

        @Override
        public void onLeftLeftAction() {
            remove(R.string.sample_left_left_action, getAdapterPosition());
        }

        @Override
        public void onRightAction() {
            Snackbar.make(swipeLayout, R.string.sample_right_action, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onRightRightAction() {
            Snackbar.make(swipeLayout, R.string.sample_right_right_action, Snackbar.LENGTH_SHORT).show();
        }

        public void remove(int msgResId, final int position) {

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
