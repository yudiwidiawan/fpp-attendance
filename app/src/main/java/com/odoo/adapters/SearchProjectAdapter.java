package com.odoo.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.odoo.R;

import java.util.List;

/**
 * Created by makan on 22/08/2017.
 */

public class SearchProjectAdapter extends CursorAdapter {
    private List<String> items;
    private TextView text;

    public SearchProjectAdapter(Context context, Cursor c, List<String> items) {
        super(context, c, false);
        this.items = items;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item, parent, false);
        text = (TextView) view.findViewById(R.id.textSearch);
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()));
    }


}
