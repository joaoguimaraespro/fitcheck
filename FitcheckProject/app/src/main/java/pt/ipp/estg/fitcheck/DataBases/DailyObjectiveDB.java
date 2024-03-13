package pt.ipp.estg.fitcheck.DataBases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pt.ipp.estg.fitcheck.DAO.DailyObjectiveDAO;
import pt.ipp.estg.fitcheck.Models.DailyObjective;

@Database(entities = {DailyObjective.class}, version = 1, exportSchema = false)
public abstract class DailyObjectiveDB extends RoomDatabase {
    private static DailyObjectiveDB INSTANCE;

    private static final Object sLock = new Object();

    public abstract DailyObjectiveDAO daoAccess();
    public static DailyObjectiveDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        DailyObjectiveDB.class, "dailyObjectiveDB")
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
        }
    }
}
