package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import br.unisc.caronasuniscegm.R;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class DayOfTheWeekAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    private List<String> data = null;
    LayoutInflater inflater;

    public DayOfTheWeekAdapter(Context context, LayoutInflater inflater, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = inflater;
        this.setData(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHolder holder = null;

        if(row == null)
        {
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DataHolder();
            holder.txtDayOfTheWeek = (TextView) row.findViewById(R.id.txtDayOfTheWeek);
            holder.checkboxDayOfTheWeek = (CheckBox) row.findViewById(R.id.checkboxDayOfTheWeek);

            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }

        String day = getData().get(position);
        holder.txtDayOfTheWeek.setText(day);

        return row;
    }

    public List<String> getData() { return data; }

    public void setData(List<String> data) {
        this.data = data;
    }


    static class DataHolder
    {
        TextView txtDayOfTheWeek;
        CheckBox checkboxDayOfTheWeek;
    }
}