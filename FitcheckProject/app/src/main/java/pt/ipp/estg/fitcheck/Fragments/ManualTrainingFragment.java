package pt.ipp.estg.fitcheck.Fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.DataBases.TrainingDB;
import pt.ipp.estg.fitcheck.Models.Enums.TipoTreinoEnum;
import pt.ipp.estg.fitcheck.Models.LatLng;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.Models.TrainingResponse;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.Models.UserTrainings;
import pt.ipp.estg.fitcheck.R;

public class ManualTrainingFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private AsyncTask asyncTask;
    private TrainingDB trainingDB;
    private TextInputLayout editDistancia;
    private Button guardar;
    private TextView type, data, duration;
    private ImageView icon;
    private Training treinoSemi;

    public ManualTrainingFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingDB = TrainingDB.getInstance(getActivity().getApplicationContext());
        if (getArguments() != null && getArguments().getSerializable("treino") != null) {
            treinoSemi = (Training) getArguments().getSerializable("treino");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manual_training, container, false);
        editDistancia = view.findViewById(R.id.editDistancia);
        guardar = view.findViewById(R.id.button);
        type = view.findViewById(R.id.textViewType);
        icon = view.findViewById(R.id.icon);
        data = view.findViewById(R.id.dataTreino);
        duration = view.findViewById(R.id.duracaoTreino);

        if (treinoSemi != null) {

            if (treinoSemi.tipo.equals(TipoTreinoEnum.caminhada)) {
                icon.setImageResource(R.drawable.ic_walking);
            }
            if (treinoSemi.tipo.equals(TipoTreinoEnum.bicicleta)) {
                icon.setImageResource(R.drawable.ic_cycling);
            }
            if (treinoSemi.tipo.equals(TipoTreinoEnum.corrida)) {
                icon.setImageResource(R.drawable.ic_run);
            }

            type.setText(treinoSemi.tipo.toString());

            data.setText("Data: " + treinoSemi.data);
            int temp = treinoSemi.duracao;
            String duracao = String.format("%02d:%02d:%02d", temp / 3600000, (temp / 60000) % 60, (temp / 1000) % 60);
            duration.setText(duracao);
        }

        editDistancia.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkDistance();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        guardar.setEnabled(false);


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                asyncTask = new ManualTrainingFragment.DatabaseAsync();
                asyncTask.execute();
                ((MenuActivity) getActivity()).ItemId(R.id.ic_statistics);
                ((MenuActivity) getActivity()).setVisibleBottomNav();
                Toast.makeText(getContext(),
                        "Treino registado com sucesso", Toast.LENGTH_LONG).show();

            }
        });
        return view;
    }



    public void checkDistance() {
        if (editDistancia.getEditText().length() == 0) {
            editDistancia.setError("Campo obrigatório");
        } else {
            if (treinoSemi != null) {
                guardar.setEnabled(true);
            }
            editDistancia.setError(null);
        }
    }



    @SuppressLint("StaticFieldLeak")
    private class DatabaseAsync extends AsyncTask<Object, Void, Void> {


        @Override
        protected Void doInBackground(Object... voids) {

            if (treinoSemi != null) {
                treinoSemi.distancia = Float.parseFloat(editDistancia.getEditText().getText().toString());
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Trainings").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(documentSnapshot.get("trainingList") != null){
                            UserTrainings trainings = new UserTrainings(new ArrayList((Collection) Objects.requireNonNull(documentSnapshot.get("trainingList"))));
                            Map<String, Object> trainingsMap = new HashMap<>();
                            TrainingResponse trainingResponse = new TrainingResponse();
                            trainingResponse.data = treinoSemi.data;
                            trainingResponse.distancia = treinoSemi.distancia;
                            trainingResponse.tipo = treinoSemi.tipo;
                            trainingResponse.duracao = treinoSemi.duracao;
                            trainingResponse.n_passos = treinoSemi.n_passos;
                            trainingResponse.treino_id = treinoSemi.treino_id;
                            trainingResponse.user_id = treinoSemi.user_id;
                            trainingResponse.v_max =  treinoSemi.v_max;
                            List<pt.ipp.estg.fitcheck.Models.LatLng> list = new ArrayList<>();
                            if(treinoSemi.percurso != null){
                                for(pt.ipp.estg.fitcheck.Models.LatLng latLng : treinoSemi.percurso){
                                    pt.ipp.estg.fitcheck.Models.LatLng latLng1 = new pt.ipp.estg.fitcheck.Models.LatLng();
                                    latLng1.setLatitude(latLng.latitude);
                                    latLng1.setLongitude(latLng.longitude);
                                    list.add(latLng1);
                                }
                            }

                            trainingResponse.percurso = list;

                            trainings.trainingList.add(trainingResponse);
                            trainingsMap.put("trainingList", trainings.trainingList);
                            db.collection("Trainings").document(user.getUid()).update( trainingsMap);
                        }else{
                            TrainingResponse trainingResponse = new TrainingResponse();
                            trainingResponse.data = treinoSemi.data;
                            trainingResponse.distancia = treinoSemi.distancia;
                            trainingResponse.tipo = treinoSemi.tipo;
                            trainingResponse.duracao = treinoSemi.duracao;
                            trainingResponse.n_passos = treinoSemi.n_passos;
                            trainingResponse.treino_id = treinoSemi.treino_id;
                            trainingResponse.user_id = treinoSemi.user_id;
                            trainingResponse.v_max =  treinoSemi.v_max;
                            List<pt.ipp.estg.fitcheck.Models.LatLng> list = new ArrayList<>();
                            if(treinoSemi.percurso != null){
                                for(LatLng latLng: treinoSemi.percurso){
                                    pt.ipp.estg.fitcheck.Models.LatLng latLng1 = new pt.ipp.estg.fitcheck.Models.LatLng();
                                    latLng1.setLatitude(latLng.latitude);
                                    latLng1.setLongitude(latLng.longitude);
                                    list.add(latLng1);
                                }
                            }

                            trainingResponse.percurso = list;
                            ArrayList<TrainingResponse> lt = new ArrayList<>();
                            lt.add(trainingResponse);

                            UserTrainings ts = new UserTrainings(lt);
                            db.collection("Trainings").document(user.getUid()).set(ts);
                        }

                        db.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user1 = new User(documentSnapshot.getString("username"),
                                        documentSnapshot.getString("email"),
                                        documentSnapshot.getString("gender"),
                                        documentSnapshot.getString("birthDate"),
                                        documentSnapshot.getLong("height").intValue(),
                                        documentSnapshot.getLong("weight").intValue(),
                                        documentSnapshot.getLong("dailyObjective").intValue(),
                                        documentSnapshot.getDouble("totalDistance").intValue());

                                user1.totalDistance += treinoSemi.distancia;

                                Map<String, Object> userMap = new HashMap<>();

                                userMap.put("totalDistance", user1.totalDistance);

                                db.collection("Users").document(user.getUid()).update(userMap);
                            }
                        });
                        trainingDB.daoAccess().insertTreino(treinoSemi);
                    }
                });

            }
            return null;
        }

    }
}