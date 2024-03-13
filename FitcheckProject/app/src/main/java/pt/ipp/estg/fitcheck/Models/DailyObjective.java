package pt.ipp.estg.fitcheck.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Estrutura do objetivo diário
 */
@Entity
public class DailyObjective implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int daily_id;

    @ColumnInfo(name = "user_id")
    public String user_id;

    public long objective;
}
