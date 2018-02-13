package com.odoo.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.OdooUtility;
import com.odoo.R;
import com.odoo.SharedData;
import com.odoo.TaskActivity;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.models.AttachmentModel;
import com.odoo.models.ProjectModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by makan on 20/09/2017.
 */

public class AttachmentAdapter extends ArrayAdapter {

    private Activity context;
    private ArrayList<AttachmentModel> attachmentModels;
    private OUser user;
    private OdooUtility odoo;
    private long deleteTaskId;

    public AttachmentAdapter(Activity context, ArrayList<AttachmentModel> attachmentModels) {
        super(context, R.layout.list_item_project, attachmentModels);
        user = new OUser().current(context);
        odoo = new OdooUtility(user.getHost(), "object");
        this.context = context;
        this.attachmentModels = attachmentModels;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_attachment, null, true);

        TextView name = (TextView) rowView.findViewById(R.id.txvAttachmentName);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imgAttachmentThumb);
        AttachmentModel attachmentModel = attachmentModels.get(position);
        name.setText(attachmentModel.getName());
        Bitmap bitmap = BitmapUtils.getBitmapImage(context, attachmentModel.getAttachmentByte());
        imageView.setImageBitmap(bitmap);

        rowView.setOnClickListener(new AdapterView.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        return rowView;
    }


}

