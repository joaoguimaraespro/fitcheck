package pt.ipp.estg.fitcheck.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.ipp.estg.fitcheck.Models.Enums.TipoTreinoEnum;

/**
 * Estrutura de um treino
 */
@Entity
public class Training implements Serializable{
    @PrimaryKey(autoGenerate = true)
    public int treino_id;

    @ColumnInfo(name = "user_id")
    public String user_id;

    public TipoTreinoEnum tipo;
    public int duracao;
    public double distancia;
    public float n_passos;
    public double v_max;
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    public String data;
    public List<LatLng> percurso;
}
