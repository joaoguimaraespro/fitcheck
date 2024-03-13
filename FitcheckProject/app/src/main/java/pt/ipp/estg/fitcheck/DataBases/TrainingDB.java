package pt.ipp.estg.fitcheck.DataBases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pt.ipp.estg.fitcheck.DAO.TrainingDAO;
import pt.ipp.estg.fitcheck.Models.Training;

@Database(entities = {Training.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class TrainingDB extends RoomDatabase {
    private static TrainingDB INSTANCE;

    private static final Object sLock = new Object();

    public abstract TrainingDAO daoAccess();
    public static TrainingDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        TrainingDB.class, "treinosDB")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigrationFrom(1)
                        .build();
            }
            return INSTANCE;
        }
    }
}
