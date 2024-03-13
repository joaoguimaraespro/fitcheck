package pt.ipp.estg.fitcheck.Fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.Serializable;

import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.R;


public class TrainingDetailsFragment extends Fragment {

    private Training tr;

    private TextView tipoTreino, duracaoTreino, distanciaTreino, npassosTreino, vMediaTreino, dataTreino;
    private ImageView typeTrainingImage;
    public LinearLayout linearLayout;


    public TrainingDetailsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            tr = (Training) getArguments().getSerializable("trainingDetails");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_training_details, container, false);

        typeTrainingImage = v.findViewById(R.id.typeTrainingImage);
        tipoTreino = v.findViewById(R.id.tipoTreino);
        duracaoTreino = v.findViewById(R.id.duracaoTreino);
        distanciaTreino = v.findViewById(R.id.distanciaTreino);
        npassosTreino = v.findViewById(R.id.npassosTreino);
        vMediaTreino = v.findViewById(R.id.vMaxTreino);
        dataTreino = v.findViewById(R.id.dataTreino);
        linearLayout = v.findViewById(R.id.maps);


        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                typeTrainingImage.setColorFilter(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                typeTrainingImage.setColorFilter(Color.BLACK);
                break;
        }


        switch (tr.tipo) {
            case bicicleta:
                typeTrainingImage.setImageResource(R.drawable.ic_cycling);
                tipoTreino.setText("Bicicleta");
                break;
            case caminhada:
                typeTrainingImage.setImageResource(R.drawable.ic_walking);
                tipoTreino.setText("Caminhada");
                break;
            case corrida:
                typeTrainingImage.setImageResource(R.drawable.ic_run);
                tipoTreino.setText("Corrida");
                break;
            case outro:
                typeTrainingImage.setImageResource(R.drawable.ic_another_exercise);
                tipoTreino.setText("Outro");
                break;
        }

        dataTreino.setText("" + tr.data);


        int temp = tr.duracao;
        String duracao = String.format("%02d:%02d:%02d", temp / 3600000, (temp / 60000) % 60, (temp / 1000) % 60);
        duracaoTreino.setText("" + duracao);


        double distancia = tr.distancia;
        distanciaTreino.setText(String.format("%.2f m", distancia));

        npassosTreino.setVisibility(View.GONE);


        if (tr.distancia >= 1000) {
            vMediaTreino.setText(String.format("%.2f km/h", ((distancia / 1000) / (temp / 3600000))));
        } else {
            vMediaTreino.setText(String.format("%.2f m/s", (distancia / (((temp / 3600000) * 3600) + ((temp / 60000) * 60) + ((temp / 1000) % 60)))));
        }


        if(tr.percurso != null && !tr.percurso.isEmpty()){
            linearLayout.setVisibility(View.VISIBLE);
            TrainingRouteMapFragment trainingRouteMap = new TrainingRouteMapFragment();
            FragmentTransaction ftr = getChildFragmentManager().beginTransaction();
            Bundle b = new Bundle();
            b.putSerializable("trainingRoute", (Serializable) tr.percurso);
            trainingRouteMap.setArguments(b);
            ftr.replace(R.id.trailMap, trainingRouteMap);
            ftr.commit();
        }else{
            linearLayout.setVisibility(View.GONE);
        }


        return v;
    }
}