package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import br.unisc.caronasuniscegm.R;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class PeriodAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    private List<String> data = null;
    public boolean[] checkedPeriods;
    LayoutInflater inflater;

    public PeriodAdapter(Context context, LayoutInflater inflater, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = inflater;
        this.setData(data);
        createCheckedPeriod();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHolder holder = null;

        if(row == null)
        {
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DataHolder();
            holder.txtPeriod = (TextView) row.findViewById(R.id.txt_period);
            holder.checkboxPeriod = (CheckBox) row.findViewById(R.id.checkboxPeriod);

            holder.checkboxPeriod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    checkedPeriods[position] = isChecked;
                }
            });

            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }

        String day = getData().get(position);
        holder.txtPeriod.setText(day);

        return row;
    }

    public List<String> getData() { return data; }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void createCheckedPeriod(){
        this.checkedPeriods = new boolean[getCount()];
    }


    static class DataHolder
    {
        TextView txtPeriod;
        CheckBox checkboxPeriod;
    }
}