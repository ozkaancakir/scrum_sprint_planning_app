package com.ozkancakir.sprintplanningapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomGridAdapter extends BaseAdapter {

    private Context context;
    private final String[] fibonacciNumbers;

    public CustomGridAdapter(Context context, String[] fibonacciNumbers) {
        this.context = context;
        this.fibonacciNumbers = fibonacciNumbers;
    }

    @Override
    public int getCount() {
        return fibonacciNumbers.length;
    }

    @Override
    public Object getItem(int position) {
        return fibonacciNumbers[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, null);
        }

        TextView textView = convertView.findViewById(R.id.grid_item_text);
        textView.setText(fibonacciNumbers[position]);

        return convertView;
    }
}
