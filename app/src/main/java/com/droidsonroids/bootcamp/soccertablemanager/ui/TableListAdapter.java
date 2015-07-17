package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.api.model.Table;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TableListAdapter extends ArrayAdapter<Table> {

    private LayoutInflater mLayoutInflater;
    public ArrayList<Table> mTableArrayList = null;
    private Context context;

    public TableListAdapter(Context context, ArrayList<Table> tableArrayList) {
        super(context, R.layout.list_view_item, tableArrayList);
        this.context = context;
        mTableArrayList = tableArrayList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TableListViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_view_item, parent, false);
            holder = new TableListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (TableListViewHolder) convertView.getTag();
        }

        Table table = mTableArrayList.get(position);
        if (table != null) {
            holder.timeTxtView.setText(table.getTime());
            List<String> userNameList = table.getUserNameList();
            if (userNameList != null) {
                String names = "";
                for (String name : userNameList) {
                    if (names.length() != 0) {
                        names += ", ";
                    }
                    names += name;
                }
                holder.usersTxtView.setText(names);
            }
        }
        holder.freeSpotsTxtView.setText(table.getFreeSpotsNumber() + "");

        return convertView;
    }

    static class TableListViewHolder {
        @Bind(R.id.time_text_view)
        TextView timeTxtView;
        @Bind(R.id.users_text_view)
        TextView usersTxtView;
        @Bind(R.id.free_spots_text_view)
        TextView freeSpotsTxtView;

        TableListViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
