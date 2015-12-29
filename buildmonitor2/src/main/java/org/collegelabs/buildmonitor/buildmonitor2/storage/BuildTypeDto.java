package org.collegelabs.buildmonitor.buildmonitor2.storage;

import android.content.ContentValues;
import android.database.Cursor;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;

/**
 */
public class BuildTypeDto {
    public static String TABLE = "buildType";
    public static String SELECT = "select buildTypeId, serverId, buildTypeStringId, displayName, name, href, webUrl, projectName, projectId from buildType";

    public BuildTypeDto(){}

    public BuildTypeDto(String displayName, BuildType buildType, Credentials credentials) {
        this.displayName = displayName;
        this.buildTypeStringId = buildType.id;
        this.name = buildType.name;
        this.href = buildType.href;
        this.webUrl = buildType.webUrl;
        this.projectName = buildType.projectName;
        this.projectId = buildType.projectId;
        this.serverId = credentials.id;
    }

    public static BuildTypeDto lift(Cursor c){
        BuildTypeDto dto = new BuildTypeDto();
        dto.buildTypeStringId = c.getString(c.getColumnIndexOrThrow("buildTypeStringId"));
        dto.name = c.getString(c.getColumnIndexOrThrow("name"));
        dto.href = c.getString(c.getColumnIndexOrThrow("href"));
        dto.webUrl = c.getString(c.getColumnIndexOrThrow("webUrl"));
        dto.projectName = c.getString(c.getColumnIndexOrThrow("projectName"));
        dto.projectId = c.getString(c.getColumnIndexOrThrow("projectId"));
        dto.displayName = c.getString(c.getColumnIndexOrThrow("displayName"));
        dto.id = c.getInt(c.getColumnIndexOrThrow("buildTypeId"));
        dto.serverId = c.getInt(c.getColumnIndexOrThrow("serverId"));
        return dto;
    }

    public String buildTypeStringId;
    public String name;
    public String href;
    public String webUrl;
    public String projectName;
    public String projectId;
    public String displayName;
    public int id;
    public int serverId;

    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        if(this.id > 0){
            cv.put("buildTypeId", this.id);
        }

        cv.put("ServerId", this.serverId);
        cv.put("buildTypeStringId", this.buildTypeStringId);
        cv.put("name", this.name);
        cv.put("href", this.href);
        cv.put("webUrl", this.webUrl);
        cv.put("projectName", this.projectName);
        cv.put("projectId", this.projectId);
        cv.put("displayName", this.displayName);
        return cv;
    }
}
