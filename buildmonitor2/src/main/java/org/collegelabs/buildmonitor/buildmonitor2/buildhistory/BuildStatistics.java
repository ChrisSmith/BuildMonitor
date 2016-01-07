package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.graphics.Color;
import android.util.Pair;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.collegelabs.buildmonitor.buildmonitor2.util.Linq.*;

public class BuildStatistics {

    private ArrayList<BuildDetailsResponse> responses;

    public BuildStatistics(BuildStatistics copy, BuildDetailsResponse response){
        this.responses = new ArrayList<>(copy.responses);
        this.responses.add(response);
    }

    public BuildStatistics(){
        responses = new ArrayList<>();
    }

    public BuildStatistics addFailingBuild(BuildDetailsResponse response){
        return new BuildStatistics(this, response);
    }

    public BarData caclulate(){

        HashSet<String> uniqueBuildTypes = new HashSet<>();

        HashMap<String, List<BuildDetailsResponse>> byDate = groupBy(responses, r -> BuildHistoryHeader.DateFormat.format(r.startDate));

        ArrayList<Pair<String, HashMap<String, Integer>>> failuresByBuildByDate = toList(byDate.entrySet(), kvp -> {
            String date = kvp.getKey();
            List<BuildDetailsResponse> builds = kvp.getValue();

            HashMap<String, Integer> result = new HashMap<>();
            for (BuildDetailsResponse item : builds) {
                String key = item.buildType.name;

                uniqueBuildTypes.add(key);

                if (!result.containsKey(key)) {
                    result.put(key, 0);
                }

                Integer count = result.get(key);
                result.put(key, count + 1);
            }

            return Pair.create(date, result);
        });

        Collections.sort(failuresByBuildByDate, (lhs, rhs) -> lhs.first.compareTo(rhs.first));

        ArrayList<String> labels = toList(uniqueBuildTypes);
        Collections.sort(labels, String::compareTo);

        ArrayList<String> xValsDates = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        int i = 0;

        // literally the stupidest shit
        int[] colorTemplate = {
                Color.parseColor("#F44336"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#3F51B5"),
                Color.parseColor("#009688"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#03A9F4"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#FF5722"),

        };
        List<Integer> colors = new ArrayList<>();

        for (Pair<String, HashMap<String, Integer>> item : failuresByBuildByDate){
            String date = item.first;
            HashMap<String, Integer> failuresByBuild = item.second;

            float[] vals = new float[labels.size()];

            for(int j = 0; j < labels.size(); j++){
                String label = labels.get(j);
                if(failuresByBuild.containsKey(label)){
                    vals[j] = failuresByBuild.get(label);
                }else{
                    vals[j] = 0f;
                }
                colors.add(colorTemplate[j % colorTemplate.length]);
            }

            yVals1.add(new BarEntry(vals, i++));
            xValsDates.add(date);
        }

        String[] labelsArray = labels.toArray(new String[labels.size()]);
        Timber.d("Labels: %s", Arrays.deepToString(labelsArray));

        BarDataSet set1 = new BarDataSet(yVals1, "");
        set1.setStackLabels(labelsArray);

        set1.setColors(colors);

        set1.setDrawValues(false);
        set1.setBarSpacePercent(20);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xValsDates, dataSets);

        return data;
    }
}
