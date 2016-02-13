package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TcUtil;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;

import java.util.ArrayList;
import java.util.Collections;

public class BuildStatusViewModel {

    public final int BuildId;
    public final int BuildTypeId;
    public boolean IsRunning;

    public String Name;
    public BuildStatus Status = BuildStatus.Loading;
    public String AgentName;

    private ArrayList<BuildDetailsResponse> _responses = new ArrayList<>();
    private ArrayList<BuildDetailsResponse> _failedDeps = new ArrayList<>();

    public BuildStatusViewModel(int buildId, int buildTypeId){
        BuildId = buildId;
        BuildTypeId = buildTypeId;
    }

    public void addSnapshotDependency(BuildDetailsResponse response){
        _responses.add(response);
        Collections.sort(_responses, ((lhs, rhs) -> lhs.startDate.compareTo(rhs.startDate)));

        if(TcUtil.getBuildStatus(response.status) == BuildStatus.Failure && !response.statusText.toLowerCase().contains("snapshot")){
            _failedDeps.add(response);
            Collections.sort(_responses, ((lhs, rhs) -> lhs.startDate.compareTo(rhs.startDate)));
        }

        if(response.buildId == BuildId){
            Status = TcUtil.getBuildStatus(response.status);
            Name = response.buildType.name;
            IsRunning = response.running;
            AgentName = response.agent.name;
        }
    }

    public String getFailedDeps(){
        String st = "";
        for(BuildDetailsResponse response : _failedDeps){
            st += response.buildType.name + "\n" + response.statusText + "\n" + "\n";
        }

        return st;
    }

}
