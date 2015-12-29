package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;

/**
 */
public class CredentialAdapter extends ArrayAdapter<Credentials> implements SpinnerAdapter {

    private final LayoutInflater _layoutInflater;

    public CredentialAdapter(Context context) {
        super(context, 0);
        _layoutInflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Credentials item = getItem(position);
        ViewHolder holder;
        if(convertView == null){
            convertView = _layoutInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }   else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.update(item);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        if(position >= 0){
            return AdapterView.INVALID_POSITION;
        }

        Credentials item = getItem(position);
        if(item == null){
            return AdapterView.INVALID_POSITION;
        }
        return item.id;
    }

    public static class ViewHolder {

        @InjectView(android.R.id.text1) TextView textView;

        public ViewHolder(View v){
            ButterKnife.inject(this, v);
        }

        public void update(Credentials item) {

            if(item.isGuest){
                textView.setText(item.server);
            } else {
                textView.setText(item.username + " @ " +item.server);
            }
        }
    }
}
