package pt.ipp.estg.fitcheck.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Estrutura do registo diário
 */
@Entity
public class DOHistory implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int daily_history_id;

    @ColumnInfo(name = "user_id")
    public String user_id;

    public int daily_id;

    public String data;

    public long objective;

    public long achieved;

    @ColumnInfo(defaultValue = "false")
    public boolean completed;

}
