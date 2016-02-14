package org.collegelabs.buildmonitor.buildmonitor2.logs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 */
public class BuildLogAdapter extends SelectableRecyclerAdapter<BuildLogAdapter.ViewHolder> {

    private double totalBytes = 0;
    private RandomAccessFile randomAccessFile = null;

    final int batchSize = 8 * 1024;

    public void Update(String path){
        try {
            File file = new File(path);
            totalBytes = file.length();

            Timber.d(getItemCount() + " rows for file: " + path);

            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            Timber.e(e, "Failed to open " + path);
            totalBytes = 0;
        }

        notifyDataSetChanged();
    }

    public int getPositionForOffset(long offset){
        return (int) (offset / batchSize);
    }


    public String getItem(int position) {

        try {

            long offset = position * batchSize;

            Timber.d("reading "+ offset + " to " + (offset+batchSize) + " / " + totalBytes);
            randomAccessFile.seek(offset);
            byte[] buffer = new byte[batchSize];
            int s = randomAccessFile.read(buffer, 0, batchSize);
            return new String(buffer, 0, s, Charset.defaultCharset());

        } catch (IOException e) {
            Timber.e(e, "Failed to seek");
        }

        return "Error";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_log, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(totalBytes == 0){
            return 0;
        }
        return (int) Math.ceil(totalBytes / batchSize);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.row_log_text) TextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        public void bind(String line){
            textView.setText(line);
        }
    }
}
