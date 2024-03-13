package pt.ipp.estg.fitcheck.Fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipp.estg.fitcheck.Activities.AuthenticationActivity;
import pt.ipp.estg.fitcheck.DataBases.DailyObjectiveDB;
import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Models.DailyObjective;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;


public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;

    private FirebaseFirestore db;

    private FirebaseUser currentUser;

    private FragmentChange context;

    private Button logout, toogleChangePassword, cancelBT, saveChangesBT;

    private TextInputLayout height, weight, username, password, confirmPassword, goalDay;

    private boolean isCheckUsername, isCheckHeight, isCheckWeight, isCheckGoals, isCheckPassword,
            isCheckConfirmPassword, isExistUsername;

    private DocumentSnapshot currentUserDocument;

    private User currentUserModel;

    private NumberPicker numHeight, numWeight, numGoals;

    private LinearLayout saveLayoutBT;

    private TextView error;

    private ProgressBar progressBar;

    private Long currentGoalDay;

    private DailyObjectiveDB dailyObjectiveDB;

    CountStepsForegroundService services;

    private int goalDayId;

    public ProfileFragment(FragmentChange context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        isCheckUsername = false;
        isCheckWeight = false;
        isCheckHeight = false;
        isCheckPassword = false;
        isCheckConfirmPassword = false;
        isCheckGoals = false;
        isExistUsername = false;
    }


    public ProfileFragment() {
        this.context = (FragmentChange) getContext();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        isCheckUsername = false;
        isCheckWeight = false;
        isCheckHeight = false;
        isCheckPassword = false;
        isCheckConfirmPassword = false;
        isCheckGoals = false;
        isExistUsername = false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyObjectiveDB = DailyObjectiveDB.getInstance(getActivity().getApplicationContext());
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.fragment_profile, container, false);

        logout = contextView.findViewById(R.id.buttonLogout);

        goalDay = contextView.findViewById(R.id.textFieldGoals);

        cancelBT = contextView.findViewById(R.id.cancelBT);

        saveChangesBT = contextView.findViewById(R.id.saveChangeBT);

        saveLayoutBT = contextView.findViewById(R.id.layoutChangeBT);

        error = contextView.findViewById(R.id.error);

        error.setVisibility(View.GONE);

        saveLayoutBT.setVisibility(View.GONE);

        progressBar = contextView.findViewById(R.id.updateProgress);
        progressBar.setVisibility(View.GONE);
        saveChangesBT.setEnabled(false);
        saveChangesBT.setBackgroundColor(getResources().getColor(R.color.grayDisabled));


        getUser();


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(isForegroundServiceRunning(CountStepsForegroundService.class)){
                    Intent intent2 = new Intent(getContext(), CountStepsForegroundService.class);
                    getActivity().stopService(intent2);
                }

                Intent intent = new Intent(getContext(), AuthenticationActivity.class);
                startActivity(intent);
                auth.signOut();
                getActivity().finish();


            }
        });


        goalDay.getEditText().setInputType(InputType.TYPE_NULL);
        goalDay.getEditText().setEnabled(false);
        goalDay.getEditText().setTextIsSelectable(true);
        goalDay.getEditText().setFocusable(false);
        goalDay.getEditText().setFocusableInTouchMode(false);


        goalDay.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goalDay.getEditText().isEnabled()) {
                    goalDay.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                    goalDay.setError(null);
                    goalDay.getEditText().setText(currentGoalDay + " passos");
                    goalDay.getEditText().setEnabled(false);
                } else {
                    goalDay.setEndIconDrawable(R.drawable.ic_baseline_edit_off_24);
                    goalDay.getEditText().setEnabled(true);
                    checkGoals();
                }
                checkAll();
            }
        });


        goalDay.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View viewNumber = getActivity().getLayoutInflater().inflate(R.layout.dialog_picker_number, null);
                builder.setView(viewNumber);
                numGoals = (NumberPicker) viewNumber.findViewById(R.id.picker);
                final TextView textView = (TextView) viewNumber.findViewById(R.id.textView);

                int minValue = 1000;
                int maxValue = 15000;
                int step = 1000;

                String[] valueSet = new String[maxValue / minValue];

                for (int i = minValue; i <= maxValue; i += step) {
                    valueSet[(i / step) - 1] = String.valueOf(i);
                    Log.d("array", String.valueOf((i / step) - 1));
                }

                numGoals.setMaxValue(valueSet.length - 1);


                numGoals.setDisplayedValues(valueSet);
                textView.setText("passos");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        goalDay.getEditText().setText(((numGoals.getValue() + 1) * 1000) + " passos");

                    }
                })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //negative button action
                            }
                        });
                builder.create().show();
            }
        });

        goalDay.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (goalDay.getEditText().isEnabled()) {
                    checkGoals();
                    checkAll();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        height = contextView.findViewById(R.id.textFieldHeigth);
        weight = contextView.findViewById(R.id.textFieldWeight);

        height.getEditText().setInputType(InputType.TYPE_NULL);
        height.getEditText().setEnabled(false);
        height.getEditText().setTextIsSelectable(true);
        height.getEditText().setFocusable(false);
        height.getEditText().setFocusableInTouchMode(false);
        height.getEditText().setText("cm");

        height.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (height.getEditText().isEnabled()) {
                    height.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                    height.getEditText().setEnabled(false);
                    height.getEditText().setText(currentUserModel.height + "cm");
                    height.setError(null);
                } else {
                    height.setEndIconDrawable(R.drawable.ic_baseline_edit_off_24);
                    height.getEditText().setEnabled(true);
                    checkHeight();
                }
                checkAll();
            }
        });


        height.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View viewNumber = getActivity().getLayoutInflater().inflate(R.layout.dialog_picker_number, null);
                builder.setView(viewNumber);
                numHeight = (NumberPicker) viewNumber.findViewById(R.id.picker);
                final TextView textView = (TextView) viewNumber.findViewById(R.id.textView);
                numHeight.setMinValue(0);
                numHeight.setMaxValue(230);
                textView.setText("cm");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        height.getEditText().setText(numHeight.getValue() + "cm");

                    }
                })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //negative button action
                            }
                        });
                builder.create().show();
            }
        });

        height.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (height.getEditText().isEnabled()) {
                    checkHeight();
                    checkAll();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        weight.getEditText().setInputType(InputType.TYPE_NULL);
        weight.getEditText().setEnabled(false);
        weight.getEditText().setTextIsSelectable(true);
        weight.getEditText().setFocusable(false);
        weight.getEditText().setFocusableInTouchMode(false);
        weight.getEditText().setText("kg");

        weight.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weight.getEditText().isEnabled()) {
                    weight.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                    weight.getEditText().setEnabled(false);
                    weight.setError(null);
                    weight.getEditText().setText(currentUserModel.weight + "kg");
                } else {
                    weight.setEndIconDrawable(R.drawable.ic_baseline_edit_off_24);
                    weight.getEditText().setEnabled(true);
                    checkWeight();
                }
                checkAll();
            }
        });

        weight.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View viewNumber = getActivity().getLayoutInflater().inflate(R.layout.dialog_picker_number, null);
                builder.setView(viewNumber);
                //builder.setTitle();
                numWeight = (NumberPicker) viewNumber.findViewById(R.id.picker);
                final TextView textView = (TextView) viewNumber.findViewById(R.id.textView);
                numWeight.setMinValue(0);
                numWeight.setMaxValue(200);
                textView.setText("kg");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        weight.getEditText().setText(numWeight.getValue() + "kg");

                    }
                })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //negative button action
                            }
                        });
                builder.create().show();
            }
        });

        weight.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (weight.getEditText().isEnabled()) {
                    checkWeight();
                    checkAll();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        username = contextView.findViewById(R.id.usernameEdit);

        username.getEditText().setEnabled(false);


        username.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getEditText().isEnabled()) {
                    username.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                    username.getEditText().setEnabled(false);
                    username.getEditText().setText(currentUser.getDisplayName());
                    username.setError(null);
                } else {
                    username.setEndIconDrawable(R.drawable.ic_baseline_edit_off_24);
                    username.getEditText().setEnabled(true);
                    checkUsername();
                }
                checkAll();
            }
        });

        username.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (username.getEditText().isEnabled()) {
                    isUsernameExists();
                    checkUsername();
                    checkAll();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password = contextView.findViewById(R.id.Password);
        confirmPassword = contextView.findViewById(R.id.ConfirmPassword);
        toogleChangePassword = contextView.findViewById(R.id.changePWButton);

        password.setVisibility(View.GONE);
        confirmPassword.setVisibility(View.GONE);

        toogleChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password.getVisibility() == View.GONE) {
                    password.setVisibility(View.VISIBLE);
                    confirmPassword.setVisibility(View.VISIBLE);
                    toogleChangePassword.setText("Cancelar");
                } else {
                    password.setVisibility(View.GONE);
                    confirmPassword.setVisibility(View.GONE);
                    toogleChangePassword.setText("Mudar Palavra-passe");
                    password.getEditText().getText().clear();
                    confirmPassword.getEditText().getText().clear();
                    password.setError(null);
                    confirmPassword.setError(null);
                    error.setVisibility(View.GONE);
                }
                checkAll();
            }
        });

        password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
                checkAll();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirmPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkConfirmPassword();
                checkAll();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        saveChangesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                if (username.getEditText().isEnabled()) {
                    if (!isExistUsername) {
                        submit();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        username.setError("Username já existe");
                    }
                } else {
                    submit();
                }

            }
        });

        cancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username.getEditText().setText(currentUserModel.username);
                height.getEditText().setText(currentUserDocument.getLong("height").intValue() + "cm");
                weight.getEditText().setText(currentUserDocument.getLong("weight").intValue() + "kg");
                username.getEditText().setEnabled(false);
                username.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                height.getEditText().setEnabled(false);
                height.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                weight.getEditText().setEnabled(false);
                weight.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                goalDay.getEditText().setEnabled(false);
                goalDay.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                password.getEditText().getText().clear();
                confirmPassword.getEditText().getText().clear();
                toogleChangePassword.setText("Mudar Palavra-passe");
                password.setVisibility(View.GONE);
                confirmPassword.setVisibility(View.GONE);
                username.setError(null);
                height.setError(null);
                weight.setError(null);
                password.setError(null);
                goalDay.setError(null);
                confirmPassword.setError(null);
                error.setVisibility(View.GONE);
                saveLayoutBT.setVisibility(View.GONE);
            }
        });

        return contextView;
    }

    public void getUser() {
        db.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    currentUserDocument = task.getResult();
                    currentUserModel = new User(currentUserDocument.getString("username"),
                            currentUserDocument.getString("email"),
                            currentUserDocument.getString("gender"),
                            currentUserDocument.getString("birthDate"),
                            currentUserDocument.getLong("height").intValue(),
                            currentUserDocument.getLong("weight").intValue(),
                            currentUserDocument.getLong("dailyObjective").intValue(),
                            currentUserDocument.getDouble("totalDistance").intValue());
                    username.getEditText().setText(currentUserModel.username);
                    height.getEditText().setText(currentUserDocument.getLong("height").intValue() + "cm");
                    weight.getEditText().setText(currentUserDocument.getLong("weight").intValue() + "kg");
                }
            }
        });
    }



    public void submit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userMap = new HashMap<>();
        username.setError(null);

        error.setVisibility(View.GONE);
        error.setText(null);

        if (username.getEditText().isEnabled()) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username.getEditText().getText().toString())
                    .build();
            auth.getCurrentUser().updateProfile(profileUpdate);
            userMap.put("username", username.getEditText().getText().toString());
        }


        if (height.getEditText().isEnabled()) {
            String[] st = height.getEditText().getText().toString().split("c");
            userMap.put("height", Integer.parseInt(st[0]));
        }

        if (weight.getEditText().isEnabled()) {
            String[] st = weight.getEditText().getText().toString().split("k");
            userMap.put("weight", Integer.parseInt(st[0]));
        }

        if (goalDay.getEditText().isEnabled()) {
            String[] strs = goalDay.getEditText().getText().toString().split(" ");
            userMap.put("dailyObjective", Long.parseLong(strs[0]));
            new DatabaseAsync().execute();

            getDailyObjective();
            goalDay.getEditText().setEnabled(false);
            goalDay.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
            saveLayoutBT.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(),
                    "Alterações efetuadas com successo!!!", Toast.LENGTH_LONG).show();
        }

        if (password.getVisibility() == View.VISIBLE) {
            auth.getCurrentUser().updatePassword(password.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        password.getEditText().getText().clear();
                        confirmPassword.getEditText().getText().clear();
                        toogleChangePassword.setText("Mudar Palavra-passe");
                        password.setVisibility(View.GONE);
                        confirmPassword.setVisibility(View.GONE);
                        saveLayoutBT.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Palavra-Passe efetuadas com successo!!!", Toast.LENGTH_LONG).show();
                    } else {
                        if (task.getException().getMessage().equals("This operation is sensitive and " +
                                "requires recent authentication. Log in again before retrying this request.")) {
                            error.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            error.setText("Para alterar palavra-passe necessita de estar autenticado recentemente");
                        }
                    }
                }
            });
        }

        if (!userMap.isEmpty()) {
            db.collection("Users").document(currentUser.getUid()).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        getUser();
                        username.getEditText().setEnabled(false);
                        username.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                        height.getEditText().setEnabled(false);
                        height.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                        weight.getEditText().setEnabled(false);
                        weight.setEndIconDrawable(R.drawable.ic_baseline_edit_24);
                        error.setVisibility(View.GONE);
                        saveLayoutBT.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Alterações efetuadas com successo!!!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    public void isUsernameExists() {
        isExistUsername = false;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (username.getEditText().getText().toString().equals(document.getString("username"))) {
                                    isExistUsername = true;

                                }
                            }
                        }
                    }
                });
    }

    private void checkUsername() {
        Matcher mUsername = Pattern.compile("[a-zA-Z0-9)]+$").matcher(username.getEditText().getText());

        if (username.getEditText().length() == 0) {
            username.setError("Campo Obrigatório");
            isCheckUsername = false;
        } else if (username.getEditText().length() < 5) {
            username.setError("Conter mais que 5 caracteres");
            isCheckUsername = false;
        } else if (!mUsername.find()) {
            username.setError("Não pode conter caracteres especiais");
            isCheckUsername = false;
        } else if (username.getEditText().getText().toString().equals(currentUser.getDisplayName())) {
            username.setError("Não alterado");
            isCheckUsername = false;
        } else {
            username.setError(null);
            isCheckUsername = true;
        }
    }

    private void checkHeight() {
        String[] st = height.getEditText().getText().toString().split("c");

        if (height.getEditText().length() == 0) {
            height.setError("Campo Obrigatório");
            isCheckHeight = false;
        } else if (Integer.parseInt(st[0]) == currentUserModel.height) {
            height.setError("Não alterado");
            isCheckHeight = false;
        } else {
            height.setError(null);
            isCheckHeight = true;
        }
    }

    private void checkWeight() {
        String[] st = weight.getEditText().getText().toString().split("k");

        if (weight.getEditText().length() == 0) {
            weight.setError("Campo Obrigatório");
            isCheckWeight = false;
        } else if (Integer.parseInt(st[0]) == currentUserModel.weight) {
            weight.setError("Não alterado");
            isCheckWeight = false;
        } else {
            weight.setError(null);
            isCheckWeight = true;
        }
    }

    private void checkGoals() {
        String [] strs = goalDay.getEditText().getText().toString().split(" ");

        if (goalDay.getEditText().length() == 0) {
            goalDay.setError("Campo Obrigatório");
            isCheckGoals = false;
            //falta corrigir a condição
        } else if (currentGoalDay != null && (Long.parseLong(strs[0]) == currentGoalDay)) {
            goalDay.setError("Não alterado");
            isCheckGoals = false;
        } else {
            goalDay.setError(null);
            isCheckGoals = true;
        }
    }

    private void checkPassword() {
        Matcher mPassword = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$").matcher(password.getEditText().getText().toString());

        if (password.getEditText().length() == 0) {
            password.setError("Campo Obrigatório");
            isCheckPassword = false;
        } else if (!mPassword.find()) {
            isCheckPassword = false;
            password.setError("Mínimo de 8 caracteres, pelo menos uma letra maiúscula, uma letra minúscula e um número");
        } else {
            isCheckPassword = true;
            password.setError(null);
        }
    }

    private void checkConfirmPassword() {
        if (confirmPassword.getEditText().length() == 0) {
            confirmPassword.setError("Campo Obrigatório");
            isCheckConfirmPassword = false;
        }
        if (!confirmPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())) {
            confirmPassword.setError("Palavras-Passes não correspondem");
            isCheckConfirmPassword = false;
        } else {
            confirmPassword.setError(null);
            isCheckConfirmPassword = true;
        }
    }

    private void checkAll() {
        if (username.getEditText().isEnabled() || height.getEditText().isEnabled()
                || weight.getEditText().isEnabled() || goalDay.getEditText().isEnabled()
                || password.getVisibility() == View.VISIBLE) {
            saveLayoutBT.setVisibility(View.VISIBLE);
            if (username.getError() != null || height.getError() != null
                    || weight.getError() != null || goalDay.getError() != null
                    || (password.getVisibility() == View.VISIBLE && (password.getEditText().length() == 0
                    || password.getError() != null || confirmPassword.getEditText().length() == 0
                    || confirmPassword.getError() != null))) {
                saveChangesBT.setEnabled(false);
                saveChangesBT.setBackgroundColor(getResources().getColor(R.color.grayDisabled));
            } else {
                saveChangesBT.setEnabled(true);
                saveChangesBT.setBackgroundColor(getResources().getColor(R.color.green));
            }

        } else {
            saveLayoutBT.setVisibility(View.GONE);
        }

    }

    private boolean isForegroundServiceRunning(Class<?> foregroundService) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (foregroundService.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDailyObjective();
    }

    private void getDailyObjective(){
        dailyObjectiveDB.daoAccess().findDailyObjectiveByUser(currentUser.getUid()).observe(getViewLifecycleOwner(), dailyObjective -> {
            if (dailyObjective != null) {
                currentGoalDay = dailyObjective.objective;
                goalDayId = dailyObjective.daily_id;
                goalDay.getEditText().setText(currentGoalDay + " passos");

            } else {
                currentGoalDay = null;
            }

        });
    }

    private class DatabaseAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            DailyObjective dailyObjective = new DailyObjective();

            dailyObjective.user_id = currentUser.getUid();

            String[] strs = goalDay.getEditText().getText().toString().split(" ");

            dailyObjective.objective = Long.parseLong(strs[0]);


            if(currentGoalDay == null){
                dailyObjectiveDB.daoAccess().insertDailyObjective(dailyObjective);
            }else {
                dailyObjective.daily_id = goalDayId;
                dailyObjectiveDB.daoAccess().updateDailyObjective(dailyObjective);
            }


            return null;
        }


    }
}