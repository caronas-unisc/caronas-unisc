package br.unisc.caronasuniscegm.adapters;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.List;

import br.unisc.caronasuniscegm.R;

public class SuggestionsCursosAdapter extends CursorAdapter {
    private List<Address> items;

    private TextView text;

    public SuggestionsCursosAdapter(Context context, Cursor cursor, List<Address> items) {

        super(context, cursor, false);

        this.items = items;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String fullAddress = "";
        for(int i = 0; i < items.get(cursor.getPosition()).getMaxAddressLineIndex(); i++) {
            fullAddress += (items.get(cursor.getPosition()).getAddressLine(i));
            if( i + 1 <  items.get(cursor.getPosition()).getMaxAddressLineIndex() ){
                fullAddress += " - ";
            }
        }

        text.setText(fullAddress);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.activity_add_place_search_item, parent, false);

        text = (TextView) view.findViewById(R.id.search_item);

        return view;

    }

}