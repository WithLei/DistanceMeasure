package com.android.renly.distancemeasure.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.R;
import com.github.pavlospt.roundedletterview.RoundedLetterView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyAdapter extends BaseAdapter {
    private Context context;
    private List<MeasureData> datas;
    private LayoutInflater inflater;

    public MyAdapter(Context context, List<MeasureData> datas) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    private int[] colors= new int[]{
            R.color.colorAccent,
            R.color.main_color_2,
            R.color.main_color_3,
            R.color.main_color_4,
            R.color.main_color_5,
            R.color.main_color_6,
            R.color.main_color_7,
            R.color.main_color_8,
            R.color.main_color_9,
            R.color.main_color_10,
            R.color.main_color_11,

    };

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
        holder.name_preview.setTitleText(data.getCarId().charAt(0) + "");
        holder.name_preview.setBackgroundColor(context.getResources().getColor(colors[pos%11]));
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
        @BindView(R.id.name_preview)
        RoundedLetterView name_preview;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
