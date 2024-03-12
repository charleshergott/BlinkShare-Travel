package app.blinkshare.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import app.blinkshare.android.R;

public class DropDownAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<String> values;

    public DropDownAdapter(@NonNull Context context, ArrayList<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drop_down_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.tvTitle);
        if (values != null && values.size() > 0)
            textView.setText(values.get(position));

        return rowView;
    }
}
