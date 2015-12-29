package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;

/**
 */
public class BuildTypeAdapter extends ArrayAdapter<BuildType> {

    private final LayoutInflater _inflater;

    public BuildTypeAdapter(Context context) {
        super(context, R.layout.buildtype_rowitem);
        _inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            convertView = _inflater.inflate(R.layout.buildtype_rowitem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        BuildType item = getItem(position);

        boolean showProjectName = position == 0
                || !getItem(position -1).projectName.equalsIgnoreCase(item.projectName);

        holder.update(item, showProjectName);

        return convertView;
    }

    public static class ViewHolder {

        @InjectView(R.id.buildtype_name) public TextView name;
        @InjectView(R.id.buildtype_projectname) public TextView projectName;

        public ViewHolder(View v){
            ButterKnife.inject(this, v);
        }

        public void update(BuildType item, boolean showProjectName) {
            name.setText(item.name);
            projectName.setText(item.projectName);
            projectName.setVisibility(showProjectName ? View.VISIBLE : View.GONE);
        }
    }
}
