package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import br.unisc.caronasuniscegm.R;
import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utilss.CalendarUtils;

/**
 * Created by MateusFelipe on 17/10/2015.
 */
public class AgendaAdapter extends ArrayAdapter<RideIntention> {

    private Context context;
    private int layoutResourceId;
    private List<RideIntention> data = null;
    private LayoutInflater inflater;

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
            holder.txtDate = (TextView) row.findViewById(R.id.txt_date);
            holder.txtPeriod = (TextView) row.findViewById(R.id.txt_period);
            holder.txtAvailabilityType = (TextView) row.findViewById(R.id.txt_availability_type);
            holder.txtPlacesInCar = (TextView) row.findViewById(R.id.txt_places_in_car);
            holder.txtStartingLocationAddress = (TextView) row.findViewById(R.id.txt_starting_location_address);
            holder.layoutAddress = (LinearLayout) row.findViewById(R.id.layout_starting_location_address);
            holder.layoutAvaiablePlacesInCar = (LinearLayout) row.findViewById(R.id.layout_places_in_car);

            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }

        RideIntention rideIntention = getData().get(position);
        holder.txtDate.setText( CalendarUtils.dateToString(rideIntention.getDate()) );
        holder.txtPeriod.setText(rideIntention.getPeriod());
        holder.txtAvailabilityType.setText(rideIntention.getAvailabilityType());
        if( rideIntention.getAvailabilityType().equals(RideIntention.AVAIBILITY_TYPE_GIVE )){
            holder.txtPlacesInCar.setText( rideIntention.getAvailablePlacesInCar() + "" );
            holder.layoutAddress.setVisibility(View.GONE);
            holder.layoutAvaiablePlacesInCar.setVisibility(View.VISIBLE);
        }else{
            holder.txtStartingLocationAddress.setText(rideIntention.getStartingLocationAddress());
            holder.layoutAvaiablePlacesInCar.setVisibility(View.GONE);
            holder.layoutAddress.setVisibility(View.VISIBLE);
        }


        return row;
    }

    public List<RideIntention> getData() { return data; }

    public void setData(List<RideIntention> data) {
        this.data = data;
    }

    public void updateDataList(List<RideIntention> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }

    static class DataHolder
    {
        TextView txtDate;
        TextView txtPeriod;
        TextView txtAvailabilityType;
        TextView txtPlacesInCar;
        TextView txtStartingLocationAddress;
        LinearLayout layoutAvaiablePlacesInCar;
        LinearLayout layoutAddress;
    }
}