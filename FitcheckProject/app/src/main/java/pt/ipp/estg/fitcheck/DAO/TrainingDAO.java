package pt.ipp.estg.fitcheck.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import pt.ipp.estg.fitcheck.Models.Training;

@Dao
public interface TrainingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTreino(Training treino);

    @Delete
    void deleteTreino(Training treino);

    @Query("SELECT * FROM Training WHERE tipo LIKE :search")
    LiveData<List<Training>> findTreinoByType(String search);

    @Query("SELECT * FROM Training WHERE user_id LIKE :search")
    LiveData<List<Training>> findTreinoByUser(String search);

    @Query("SELECT * FROM Training WHERE user_id LIKE :search_uid AND tipo LIKE :search_tipo")
    LiveData<List<Training>> findTreinoByUserAndType(String search_uid, String search_tipo);
}
