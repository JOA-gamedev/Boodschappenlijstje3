package com.NovaLabs.boodschappenlijstje;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {
    private final Context context; //context
    private final ArrayList<Item> items; //data source of the list adapter

    //public constructor
    public CustomListAdapter(Context context, ArrayList<Item> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return items.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.itemlayout, parent, false);
        }

        // get current item to be displayed
        Item currentItem = (Item) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemName = convertView.findViewById(R.id.item_name);
        TextView textViewItemNumber = convertView.findViewById(R.id.item_number);
        TextView textViewItemUnit = convertView.findViewById(R.id.item_unit);

        textViewItemName.setText(currentItem.getItemName());
        textViewItemNumber.setText(currentItem.getItemNumber());
        textViewItemUnit.setText(context.getResources().getStringArray(R.array.units)[currentItem.getItemUnit()]);

        // returns the view for the current row
        return convertView;
    }
}