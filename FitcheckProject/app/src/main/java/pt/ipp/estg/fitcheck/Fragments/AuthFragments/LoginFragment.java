package pt.ipp.estg.fitcheck.Fragments.AuthFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.DataBases.DailyObjectiveDB;
import pt.ipp.estg.fitcheck.DataBases.TrainingDB;
import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Models.DailyObjective;
import pt.ipp.estg.fitcheck.Models.LatLng;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.Models.TrainingResponse;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.Models.UserTrainings;
import pt.ipp.estg.fitcheck.R;


public class LoginFragment extends Fragment {

    public TextInputLayout emailEditText, passwordEditText;

    public Button loginButton;

    public TextView registerBT, resetPasswordBT;

    private TextView error;

    private FragmentChange context;

    private boolean isCheckEmail;

    private FirebaseAuth auth;

    private FirebaseFirestore firebaseFirestore;

    private ProgressBar loginProgress;

    private TrainingDB databaseTraining;

    private DailyObjectiveDB dailyObjectiveDB;


    public LoginFragment(FragmentChange context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
        firebaseFirestore = FirebaseFirestore.getInstance();
        isCheckEmail = false;
    }


    public LoginFragment() {
        this.context = (FragmentChange) getContext();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
        firebaseFirestore = FirebaseFirestore.getInstance();
        isCheckEmail = false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.fragment_login, container, false);

        registerBT = contextView.findViewById(R.id.registerLink);
        resetPasswordBT = contextView.findViewById(R.id.resetPassTextView);

        emailEditText = contextView.findViewById(R.id.textFieldEmail);
        passwordEditText = contextView.findViewById(R.id.textFieldPassword);
        loginButton = contextView.findViewById(R.id.buttonLogin);
        error = contextView.findViewById(R.id.error);
        loginProgress = contextView.findViewById(R.id.loginProgress);

        loginButton.setEnabled(false);

        emailEditText.setError(null);
        passwordEditText.setError(null);
        error.setText(null);

        emailEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEmail();
                checkAll();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkAll();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerBT.setOnClickListener((v) -> {
            RegisterFragment registerPage = new RegisterFragment(context);
            context.exchangeFrag(registerPage);
        });

        resetPasswordBT.setOnClickListener((v) -> {
            ResetPasswordFragment registerPage = new ResetPasswordFragment(context);
            context.exchangeFrag(registerPage);
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                loginProgress.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(emailEditText.getEditText().getText().toString(),
                        passwordEditText.getEditText().getText().toString()).addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        boolean emailVerified = user.isEmailVerified();

                        if (emailVerified) {

                            loginProgress.setVisibility(View.GONE);
                            emailEditText.setError(null);
                            passwordEditText.setError(null);
                            error.setText(null);
                            loginProgress.setVisibility(View.GONE);
                            loginSuccess();
                        } else {
                            loginProgress.setVisibility(View.GONE);
                            error.setText("Necessita de verificar a sua conta no email");
                        }
                    } else {
                        loginProgress.setVisibility(View.GONE);
                        if (task.getException().getMessage().equals("There is no user record corresponding to this identifier." +
                                " The user may have been deleted.")) {
                            emailEditText.setError("Email inexistente");
                        } else if (task.getException().getMessage().equals("The password is invalid or " +
                                "the user does not have a password.")) {
                            passwordEditText.setError("Palavra-passe incorreta");
                        } else if (task.getException().getMessage().equals("We have blocked all requests " +
                                "from this device due to unusual activity. Try again later. " +
                                "[ Access to this account has been temporarily disabled due to many " +
                                "failed login attempts. You can immediately restore it by resetting " +
                                "your password or you can try again later. ]")) {
                            error.setText("Conta temporáriamente inativa devido a demasiadas tentativas falhadas." +
                                    " Pode esperar e tentar outra vez ou restaurar a palavra-passe.");
                        }
                    }
                });

            }
        });


        return contextView;
    }

    private void loginSuccess() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || !user.isEmailVerified()) {
            auth.signOut();
        } else {
            databaseTraining = TrainingDB.getInstance(getContext());
            dailyObjectiveDB = DailyObjectiveDB.getInstance(getContext());

            new DatabaseAsync().execute();
            Intent it = new Intent(getContext(), MenuActivity.class);
            startActivity(it);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        emailEditText.setError(null);
        passwordEditText.setError(null);
        error.setText(null);
        loginSuccess();
    }


    private void checkEmail() {
        Matcher mEmail = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
                .matcher(emailEditText.getEditText().getText().toString());
        if (emailEditText.getEditText().length() == 0) {
            emailEditText.setError("Campo Obrigatório");
            isCheckEmail = false;
        } else if (!mEmail.find()) {
            emailEditText.setError("Email inválido");
            isCheckEmail = false;
        } else {
            isCheckEmail = true;
            emailEditText.setError(null);
        }
    }

    private void checkAll() {
        if (isCheckEmail && passwordEditText.getEditText().length() != 0) {
            loginButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
        }
    }

    private class DatabaseAsync extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {

            if (databaseTraining != null) {
                if (auth.getCurrentUser() != null) {

                    databaseTraining.clearAllTables();
                    dailyObjectiveDB.clearAllTables();

                    firebaseFirestore.collection("Trainings").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.get("trainingList") != null) {
                                UserTrainings trainings = documentSnapshot.toObject(UserTrainings.class);

                                for (final TrainingResponse training : trainings.trainingList) {
                                    Training trainingResponse = new Training();
                                    trainingResponse.data = training.data;
                                    trainingResponse.distancia = training.distancia;
                                    trainingResponse.tipo = training.tipo;
                                    trainingResponse.duracao = training.duracao;
                                    trainingResponse.n_passos = training.n_passos;
                                    trainingResponse.treino_id = training.treino_id;
                                    trainingResponse.user_id = training.user_id;
                                    trainingResponse.v_max =  training.v_max;
                                    List<LatLng> list = new ArrayList<>();
                                    if(training.percurso != null){
                                        for(LatLng latLng: training.percurso){
                                            LatLng latLng1 = new LatLng(latLng.latitude,latLng.longitude);
                                            list.add(latLng1);
                                        }
                                    }

                                    trainingResponse.percurso = list;
                                    databaseTraining.daoAccess().insertTreino(trainingResponse);
                                }
                            }
                        }
                    });

                    firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);

                            DailyObjective dailyObjective = new DailyObjective();

                            if (user.dailyObjective != 0) {
                                dailyObjective.objective = user.dailyObjective;
                                dailyObjective.user_id = auth.getCurrentUser().getUid();
                                dailyObjectiveDB.daoAccess().insertDailyObjective(dailyObjective);
                            }

                        }
                    });
                }
            }

            return null;
        }
    }

}