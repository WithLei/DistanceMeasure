package com.android.renly.distancemeasure.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.R;

import java.util.List;

import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder;

public class experimentAdapter extends RecyclerView.Adapter<experimentAdapter.ViewHolder> {
    private Context mContext;
    private List<MeasureData> list;
    private MyItemClickListener listener;
    private MyItemLongClickListener longClickListener;

    public experimentAdapter(Context mContext, List<MeasureData> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_experiment,parent,false);
        return new ViewHolder(view,listener,longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final MeasureData data = list.get(pos);
        holder.carid.setText(data.getCarId());
        holder.measureTime.setText(data.getMeasureTime());
        holder.result.setText(data.getResult());
        holder.time.setText(data.getTime());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void remove(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void add(MeasureData data, int position) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements AnimateViewHolder,View.OnClickListener, View.OnLongClickListener{
        private TextView carid;
        private TextView measureTime;
        private TextView result;
        private TextView time;
        private MyItemClickListener listener;
        private MyItemLongClickListener longClickListener;

        public ViewHolder(View itemView,MyItemClickListener listener,MyItemLongClickListener longClickListener) {
            super(itemView);
            carid = itemView.findViewById(R.id.carid);
            measureTime = itemView.findViewById(R.id.measureTime);
            result = itemView.findViewById(R.id.result);
            time = itemView.findViewById(R.id.time);
            this.listener = listener;
            this.longClickListener = longClickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
            ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
            ViewCompat.setAlpha(itemView, 0);
        }

        @Override
        public void preAnimateRemoveImpl(RecyclerView.ViewHolder holder) {
        }

        @Override
        public void animateAddImpl(RecyclerView.ViewHolder holder, ViewPropertyAnimatorListener listener) {
            ViewCompat.animate(itemView)
                    .translationY(0)
                    .alpha(1)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        @Override
        public void animateRemoveImpl(RecyclerView.ViewHolder holder, ViewPropertyAnimatorListener listener) {
            ViewCompat.animate(itemView)
                    .translationY(-itemView.getHeight() * 0.3f)
                    .alpha(0)
                    .setDuration(300)
                    .setListener(listener)
                    .start();
        }

        @Override
        public void onClick(View view) {
            if (listener != null){
                listener.onItemClick(view,getPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null){
                longClickListener.onItemLongClick(view,getPosition());
            }
            // return true 已经处理完事件
            return true;
        }
    }

    public interface MyItemClickListener{
        void onItemClick(View view, int pos);
    }

    public interface MyItemLongClickListener{
        void onItemLongClick(View view, int pos);
    }

    public void setItemClickListener(MyItemClickListener listener){
        this.listener = listener;
    }
    public void setItemLongClickListener(MyItemLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }
}
