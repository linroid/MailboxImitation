package com.linroid.interview.teambition;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
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
        return data==null ? 0 : data.size();
    }

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class SampleHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_title)
        TextView titleTV;
        public SampleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
