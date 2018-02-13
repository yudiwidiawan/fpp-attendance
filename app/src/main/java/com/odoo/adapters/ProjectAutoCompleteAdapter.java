package com.odoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.models.ProjectModel;

import java.util.ArrayList;

/**
 * Created by makan on 07/09/2017.
 */

public class ProjectAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private ArrayList<ProjectModel> originalList;
    private ArrayList<ProjectModel> suggestions = new ArrayList<>();
    private Filter filter = new CustomFilter();

    public ProjectAutoCompleteAdapter(Context context, ArrayList<ProjectModel> originalList) {
        this.context = context;
        this.originalList = originalList;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Object getItem(int position) {
        return suggestions.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return suggestions.get(position).getId();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.autotext_project,
                    parent,
                    false);
            holder = new ViewHolder();
            holder.autoText = (TextView) convertView.findViewById(R.id.autoText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.autoText.setText(suggestions.get(position).getName());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            suggestions.clear();

            if (originalList != null && constraint != null) { // Check if the Original List and Constraint aren't null.
                for (int i = 0; i < originalList.size(); i++) {
                    if (originalList.get(i).getName().toLowerCase().contains(constraint.toString().toLowerCase())) { // Compare item in original list if it contains constraints.
                        suggestions.add(originalList.get(i)); // If TRUE add item in Suggestions.
                    }
                }
            }
            FilterResults results = new FilterResults(); // Create new Filter Results and return this to publishResults;
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
            notifyDataSetChanged();
        }
    }
    class ViewHolder {
        TextView autoText;

    }
}
