package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import br.unisc.caronasuniscegm.AgendaActivity;
import br.unisc.caronasuniscegm.R;
import br.unisc.caronasuniscegm.UpdateRideActivity;
import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utils.CalendarUtils;

/**
 * Created by MateusFelipe on 17/10/2015.
 */
public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ViewHolder> {

    private List<RideIntention> data = null;
    private Context mContext;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView txtDate;
        public TextView txtPeriod;
        public TextView txtAvailabilityType;
        public TextView txtPlacesInCar;
        public TextView txtStartingLocationAddress;
        public TextView txtDataPosition;

        public ImageView iconGiveReceiveRide;

        public LinearLayout layoutAvaiablePlacesInCar;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            txtDate = (TextView) v.findViewById(R.id.txt_date);
            txtPeriod = (TextView) v.findViewById(R.id.txt_period);
            txtAvailabilityType = (TextView) v.findViewById(R.id.txt_availability_type);
            txtPlacesInCar = (TextView) v.findViewById(R.id.txt_places_in_car);
            txtStartingLocationAddress = (TextView) v.findViewById(R.id.txt_starting_location_address);
            txtDataPosition = (TextView) v.findViewById(R.id.txt_data_position);
            iconGiveReceiveRide = (ImageView) v.findViewById(R.id.icon_give_receive_ride);
            //layoutAvaiablePlacesInCar = (LinearLayout) v.findViewById(R.id.layout_places_in_car);

            mListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.initUpdateRideActivity(v);
        }

        public static interface IMyViewHolderClicks {
            public void initUpdateRideActivity(View v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AgendaAdapter(List<RideIntention> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AgendaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
    int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_agenda_item_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        AgendaAdapter.ViewHolder vh = new ViewHolder(v, new AgendaAdapter.ViewHolder.IMyViewHolderClicks() {
            @Override
            public void initUpdateRideActivity(View v) {
                int position = Integer.valueOf(((TextView) v.findViewById(R.id.txt_data_position)).getText().toString());
                RideIntention rideIntention = data.get(position);

                ((AgendaActivity) mContext).startUpdateRideActivity(rideIntention);
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RideIntention rideIntention = getData().get(position);
        holder.txtDataPosition.setText(position + "");
        holder.txtDate.setText( CalendarUtils.dateToString(rideIntention.getDate()) );
        holder.txtPeriod.setText(rideIntention.getPeriod());
        holder.txtAvailabilityType.setText(rideIntention.getAvailabilityType());
        if( rideIntention.getAvailabilityType().equals(RideIntention.AVAILABILITY_TYPE_GIVE)){
            holder.txtPlacesInCar.setText( rideIntention.getAvailablePlacesInCar() + "" );
            holder.txtStartingLocationAddress.setText(rideIntention.getStartingLocationAddress());
            Drawable drawable = mContext.getResources().getDrawable(android.R.mipmap.sym_def_app_icon);
            holder.iconGiveReceiveRide.setImageDrawable(drawable);

            //holder.layoutAddress.setVisibility(View.GONE);
            //holder.layoutAvaiablePlacesInCar.setVisibility(View.VISIBLE);
        }else{
            holder.txtStartingLocationAddress.setText(rideIntention.getStartingLocationAddress());
            Drawable drawable = mContext.getResources().getDrawable(android.R.mipmap.sym_def_app_icon);
            holder.iconGiveReceiveRide.setImageDrawable(drawable);
            //holder.layoutAvaiablePlacesInCar.setVisibility(View.GONE);
            //holder.layoutAddress.setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
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
}