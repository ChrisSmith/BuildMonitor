package org.collegelabs.buildmonitor.buildmonitor2.logs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LogSearchResultsAdapter extends SelectableRecyclerAdapter<LogSearchResultsAdapter.SearchResultViewHolder> {

    private final int _highLightColor;
    private LayoutInflater _inflater;
    private ArrayList<LogSearchResults.LogSearchResult> _data = new ArrayList<>();

    public LogSearchResultsAdapter(Context context) {
        _inflater = LayoutInflater.from(context);
        _highLightColor = context.getColor(R.color.yellow_fill);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public LogSearchResults.LogSearchResult getItem(int position){
        return _data.get(position);
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _inflater.inflate(R.layout.row_searchresult, null);
        return new SearchResultViewHolder(view, _highLightColor);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

    public void clear() {
        _data.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<LogSearchResults.LogSearchResult> data) {
        _data.addAll(data);
        notifyDataSetChanged();
    }


    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        private final int _highLightColor;
        @InjectView(R.id.row_searchresult_text) public TextView textView;

        public SearchResultViewHolder(View view, int highLightColor){
            super(view);
            ButterKnife.inject(this, view);
            _highLightColor = highLightColor;
        }

        public void bind(LogSearchResults.LogSearchResult item){

            Spannable text = new SpannableStringBuilder(item.preview);

            ForegroundColorSpan span = new ForegroundColorSpan(_highLightColor);
            text.setSpan(span, item.previewMatchStart, item.previewMatchEnd, 0);

            textView.setText(text);
        }
    }
}
