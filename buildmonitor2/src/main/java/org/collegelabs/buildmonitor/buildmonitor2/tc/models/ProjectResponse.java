package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import java.util.List;

public class ProjectResponse {

    public String id, name, href, description, archived, webUrl;

    public Project parentProject;
    public ProjectCollection projects;
    public BuildTypeCollection buildTypes;

    public class ProjectCollection {
        public List<Project> project;
    }

    public class BuildTypeCollection {
        public List<BuildType> buildType;
    }
}

