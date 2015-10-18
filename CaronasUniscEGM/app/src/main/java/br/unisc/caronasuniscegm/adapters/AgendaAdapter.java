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
import br.unisc.caronasuniscegm.rest.RideIntention;

/**
 * Created by MateusFelipe on 17/10/2015.
 */
public class AgendaAdapter extends ArrayAdapter<RideIntention> {

    Context context;
    int layoutResourceId;
    private List<RideIntention> data = null;
    LayoutInflater inflater;
    public boolean[] checkedDays;

    public AgendaAdapter(Context context, LayoutInflater inflater, int layoutResourceId, List<RideIntention> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = inflater;
        this.setData(data);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHolder holder = null;

        if(row == null)
        {
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DataHolder();
            holder.txtDate = (TextView) row.findViewById(R.id.txtDate);
            holder.txtDayOfTheWeek = (TextView) row.findViewById(R.id.txtDayOfTheWeek);
            holder.txtPeriod = (TextView) row.findViewById(R.id.txtPeriod);
            holder.txtAvailabilityType = (TextView) row.findViewById(R.id.txtAvailabilityType);

            TextView txtDate;
            TextView txtDayOfTheWeek;
            TextView txtPeriod;
            TextView txtAvailabilityType;


            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }

        return row;
    }

    public List<RideIntention> getData() { return data; }

    public void setData(List<RideIntention> data) {
        this.data = data;
    }

    static class DataHolder
    {
        TextView txtDate;
        TextView txtDayOfTheWeek;
        TextView txtPeriod;
        TextView txtAvailabilityType;
    }
}