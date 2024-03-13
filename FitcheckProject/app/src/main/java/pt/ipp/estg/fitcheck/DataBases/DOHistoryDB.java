package pt.ipp.estg.fitcheck.DataBases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pt.ipp.estg.fitcheck.DAO.DOHistoryDAO;
import pt.ipp.estg.fitcheck.Models.DOHistory;

@Database(entities = {DOHistory.class}, version = 1, exportSchema = false)
public abstract class DOHistoryDB extends RoomDatabase {
    private static DOHistoryDB INSTANCE;

    private static final Object sLock = new Object();

    public abstract DOHistoryDAO daoAccess();
    public static DOHistoryDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        DOHistoryDB.class, "doHistoryDB")
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return INSTANCE;
    }

}
