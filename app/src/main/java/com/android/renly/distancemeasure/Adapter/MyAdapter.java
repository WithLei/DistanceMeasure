package com.android.renly.distancemeasure.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyAdapter extends BaseAdapter {
    List<MeasureData> datas;
    private LayoutInflater inflater;

    public MyAdapter(Context context, List<MeasureData> datas) {
        this.inflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.item_experiment, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        MeasureData data = datas.get(pos);
        holder.carid.setText(data.getCarId());
        holder.measureTime.setText(data.getMeasureTime());
        holder.result.setText(data.getResult());
        holder.time.setText(data.getTime());
        return view;
    }

    public void remove(int pos){
        datas.remove(pos);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.carid)
        TextView carid;
        @BindView(R.id.measureTime)
        TextView measureTime;
        @BindView(R.id.result)
        TextView result;
        @BindView(R.id.time)
        TextView time;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
