package pt.ipp.estg.fitcheck.Models;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.util.List;

import pt.ipp.estg.fitcheck.Models.Enums.TipoTreinoEnum;

public class TrainingResponse {
    public int treino_id;

    public String user_id;

    public TipoTreinoEnum tipo;
    public int duracao;
    public double distancia;
    public float n_passos;
    public double v_max;
    public String data;
    public List<LatLng> percurso;


    public TrainingResponse() {
    }

}
