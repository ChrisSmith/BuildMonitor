package org.collegelabs.buildmonitor.buildmonitor2.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.util.SparseArray;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import org.collegelabs.buildmonitor.buildmonitor2.tc.CredentialException;
import org.collegelabs.buildmonitor.buildmonitor2.tc.CredentialStore;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;
import rx.Observable;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 */
public class Database  {

    private OpenHelper openHelper;
    public BriteDatabase Db;

    public Database(Context context, boolean useInMemoryDb){
        openHelper = new OpenHelper(context, useInMemoryDb);
        SqlBrite sqlBrite = SqlBrite.create();
        Db = sqlBrite.wrapDatabaseHelper(openHelper);
    }

    public long insertBuildType(BuildTypeDto dto) {
        return Db.insert(BuildTypeDto.TABLE, dto.getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Observable<List<BuildTypeWithCredentials>> getAllBuildTypesWithCreds(){
        return Observable.combineLatest(getAllBuildTypes(), GetAllCredentials(), Pair::create)
                .map(t -> {
                    ArrayList<BuildTypeWithCredentials> result = new ArrayList<>();
                    SparseArray<Credentials> credentialsById = new SparseArray<>();

                    for(Credentials c : t.second){
                        credentialsById.put(c.id, c);
                    }

                    for (BuildTypeDto buildType : t.first) {
                        Credentials credentials = credentialsById.get(buildType.serverId);
                        if(credentials != null){
                            result.add(new BuildTypeWithCredentials(buildType, credentials));
                        }
                    }

                    return result;
                });
    }

    public Observable<List<BuildTypeDto>> getAllBuildTypes(){
        return Db.createQuery(BuildTypeDto.TABLE, BuildTypeDto.SELECT)
                .mapToList(BuildTypeDto::lift)
                ;
    }

    public long InsertCredentials(String username, String serverUrl, String password, boolean isGuest) {

        try {
            ContentValues values = new ContentValues();
            values.put("ServerUrl", serverUrl);
            values.put("IsGuest", isGuest);

            if(!isGuest){
                CredentialStore store = new CredentialStore();
                String encrypted = store.encrypt(password);
                values.put("username", username);
                values.put("password", encrypted);
            }

            return Db.insert(ServerDto.TABLE, values);

        } catch (CredentialException e) {
            throw new RuntimeException("Failed to encrypt password", e);
        }

    }

    public Observable<List<Credentials>> GetAllCredentials(){
        return Db.createQuery(ServerDto.TABLE, ServerDto.SELECT)
                .mapToList(ServerDto::lift)
                .map(servers -> {

                    try {
                        CredentialStore store = new CredentialStore();
                        List<Credentials> result = new ArrayList<>();

                        for (ServerDto dto : servers){
                            if(dto.IsGuest){
                                result.add(new Credentials(dto.Id, dto.ServerUrl));
                            } else {
                                String decrypted = store.decrypt(dto.EncryptedPassword);
                                result.add(new Credentials(dto.Id, dto.UserName, decrypted, dto.ServerUrl));
                            }
                        }

                        return result;
                    } catch (CredentialException e) {
                        Timber.e(e, "Failed to get creds");
                    }

                    return new ArrayList<>(); // TODO null object pattern? how can i invoke the error path?
                });
    }

    static class OpenHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "buildMonitorDb";
        public static final int DATABASE_VERSION = 1;

        OpenHelper(Context context, boolean useInMemoryDb) {
            super(context, useInMemoryDb ? null : DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Timber.i("Creating database");

            Execute(db, "create table Server (" +
                    "ServerId integer primary key autoincrement" +
                    ", UserName text null" +
                    ", Password text null" +
                    ", IsGuest int not null" +
                    ", ServerUrl text not null" +
                    ")"
            );

            Execute(db, "create table buildType (" +
                    "buildTypeId integer primary key autoincrement" +
                    ", serverId integer not null" +
                    ", buildTypeStringId text not null" +
                    ", name text not null" +
                    ", displayName text not null" +
                    ", href text not null" +
                    ", webUrl text not null" +
                    ", projectName text not null" +
                    ", projectId text not null" +
                    ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Timber.w("Updating database from " + oldVersion + " to " + newVersion);
        }

        private static void Execute(SQLiteDatabase db, String cmd){
            Timber.w("Executing: " + cmd);
            db.execSQL(cmd);
        }
    }
}
