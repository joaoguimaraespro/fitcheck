package pt.ipp.estg.fitcheck.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pt.ipp.estg.fitcheck.Models.DOHistory;

@Dao
public interface DOHistoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDOHistory(DOHistory d);

    @Delete
    void deleteDOHistory(DOHistory d);

    @Update
    void updateDOHistory(DOHistory d);

    @Query("SELECT * FROM DOHistory WHERE user_id LIKE :u AND data LIKE :d")
    LiveData<DOHistory> findDOHistoryByUserByDate(String u, String d);

    @Query("SELECT * FROM DOHistory WHERE user_id LIKE :u")
    LiveData<List<DOHistory>> findDOHistoryByUser(String u);
}
