package com.odoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import com.odoo.R;

import java.util.ArrayList;

/**
 * Created by makan on 03/09/2017.
 */

public class CommonListViewEditTextAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<ListItem> myItems = new ArrayList<>();

    public CommonListViewEditTextAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 1; i++) {
            ListItem listItem = new ListItem();
            listItem.caption = "";
            myItems.add(listItem);
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return myItems.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public ArrayList<ListItem> getMyItems() {
        return myItems;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_edittext, null);
            holder.caption = (EditText) convertView
                    .findViewById(R.id.itemCaption);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ImageButton deleteItem = (ImageButton) convertView
                .findViewById(R.id.deleteItem);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myItems.remove(position);
                notifyDataSetChanged();
            }
        });
        //Fill EditText with the value you have in data source
        holder.caption.setText(myItems.get(position).caption);
        holder.caption.setId(position);

        //we need to update adapter once we finish with editing
        holder.caption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && myItems.size() > 0){
                    final int position = v.getId();
                    final EditText Caption = (EditText) v;
                    myItems.get(position).caption = Caption.getText().toString();
                    deleteItem.setVisibility(View.VISIBLE);
                }
            }
        });


        return convertView;
    }

    public void addLastEditText() {
        ListItem listItem = new ListItem();
        listItem.caption = "";
        myItems.add(listItem);
        notifyDataSetChanged();
    }
}


class ViewHolder {
    EditText caption;

}


