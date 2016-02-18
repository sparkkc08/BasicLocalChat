package com.simplechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.simplechat.R;
import com.simplechat.model.MessagePackageModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by xack1 on 18.02.2016.
 */
public class ClientAdapter extends BaseAdapter {

    private Context ctx;
    private LayoutInflater inflater;
    private LinkedHashMap<String, MessagePackageModel> objects;

    public ClientAdapter(Context context, LinkedHashMap<String, MessagePackageModel> list) {
        inflater = LayoutInflater.from(context);
        objects = list;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public MessagePackageModel getItem(int position) {
        return (new ArrayList<>(objects.values())).get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the layout
            convertView = inflater.inflate(R.layout.client_row, parent, false);

            viewHolder = new ViewHolder();
            if (convertView != null) {
                viewHolder.tvClientName = (TextView) convertView.findViewById(R.id.tvClientName);

                viewHolder.vStatus = convertView.findViewById(R.id.vStatus);
                convertView.setTag(viewHolder);
            }
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MessagePackageModel messagePackage = getItem(position);

        viewHolder.tvClientName.setText(messagePackage.name);
        viewHolder.vStatus.setBackgroundResource(messagePackage.online ? R.drawable.bg_online : R.drawable.bg_offline);

        return convertView;
    }

    static class ViewHolder {
        public TextView tvClientName;
        public View vStatus;
    }
}
