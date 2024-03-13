package pt.ipp.estg.fitcheck.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import pt.ipp.estg.fitcheck.Models.DOHistory;
import pt.ipp.estg.fitcheck.Models.DailyObjective;

@Dao
public interface DailyObjectiveDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDailyObjective(DailyObjective d);

    @Delete
    void deleteDailyObjective(DailyObjective d);

    @Update
    void updateDailyObjective(DailyObjective d);

    @Query("SELECT * FROM DailyObjective WHERE user_id LIKE :search")
    LiveData<DailyObjective> findDailyObjectiveByUser(String search);
}
