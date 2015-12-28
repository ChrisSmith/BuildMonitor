package org.collegelabs.buildmonitor.buildmonitor2.storage;

import android.content.ContentValues;
import android.database.Cursor;

/**
 */
public class ServerDto {

    public static String TABLE = "server";
    public static String SELECT = "select ServerId, UserName, Password, IsGuest, ServerUrl from server";

    public static ServerDto lift(Cursor c){
        return new ServerDto(
                c.getInt(c.getColumnIndexOrThrow("ServerId")),
                c.getString(c.getColumnIndexOrThrow("UserName")),
                c.getString(c.getColumnIndexOrThrow("Password")),
                c.getString(c.getColumnIndexOrThrow("ServerUrl")),
                c.getInt(c.getColumnIndexOrThrow("IsGuest")) == 1
        );
    }

    public String UserName;
    public String ServerUrl;
    public String EncryptedPassword;
    public boolean IsGuest;
    public int Id;

    public ServerDto(int id, String userName, String password, String serverUrl, boolean isGuest) {
        Id = id;
        UserName = userName;
        ServerUrl = serverUrl;
        EncryptedPassword = password;
        IsGuest = isGuest;
    }
}