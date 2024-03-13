package pt.ipp.estg.fitcheck.Fragments.AuthFragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.DialogTitle;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Fragments.DatePickerBirthDateFragment;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.R;


public class RegisterFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


    public TextInputLayout username, email, password, confirmPassword, editDate, genderInput;

    private ProgressBar registerProgress;

    public String gender;

    private int day, month, year;

    private RadioButton radioMale, radioFemale;

    private RadioGroup radioGender;

    public Button registerButton;

    private FragmentChange context;

    private FirebaseAuth auth;

    private boolean isCheckUsername, isCheckEmail, isCheckPassword, isCheckConfirmPassword,
            isCheckDate, isCheckGender, isCheckAll;

    private String regBirthDate;

    private boolean isExistUsername;


    public RegisterFragment(FragmentChange context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("PT");
        this.gender = null;
        isCheckUsername = false;
        isCheckEmail = false;
        isCheckPassword = false;
        isCheckConfirmPassword = false;
        isCheckDate = false;
        isCheckGender = false;
        isCheckAll = false;
        isExistUsername = false;
    }

    public RegisterFragment() {
        // Required empty public constructor

        this.context = (FragmentChange) getContext();
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

        View mContentView = inflater.inflate(R.layout.fragment_register, container, false);
        username = mContentView.findViewById(R.id.textFieldUsername);
        email = mContentView.findViewById(R.id.textFieldEmail);
        password = mContentView.findViewById(R.id.Password);
        confirmPassword = mContentView.findViewById(R.id.ConfirmPassword);
        editDate = mContentView.findViewById(R.id.textFieldDate);
        genderInput = mContentView.findViewById(R.id.textFieldGender);
        radioMale = mContentView.findViewById(R.id.radioMale);
        radioFemale = mContentView.findViewById(R.id.radioFemale);
        radioGender = mContentView.findViewById(R.id.radioGender);
        registerProgress = mContentView.findViewById(R.id.registerProgress);


        registerButton = mContentView.findViewById(R.id.registerButton);
        registerButton.setEnabled(false);

        editDate.getEditText().setInputType(InputType.TYPE_NULL);
        editDate.getEditText().setEnabled(true);
        editDate.getEditText().setTextIsSelectable(true);
        editDate.getEditText().setFocusable(false);
        editDate.getEditText().setFocusableInTouchMode(false);
        editDate.getEditText().setText("  /  /    ");
        editDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerBirthDateFragment();
                datePicker.setTargetFragment(RegisterFragment.this, 0);
                datePicker.show(getFragmentManager(), "date picker");
            }
        });


        //Valida os inputs
        username.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isUsernameExists();
                checkUsername();
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        email.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEmail();
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkGender();
                checkAllFields();
            }
        });


        editDate.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkDate();
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
                checkAllFields();
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
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String regUsername = username.getEditText().getText().toString().trim();
                String regEmail = email.getEditText().getText().toString();
                String regPassword = password.getEditText().getText().toString();

                if (!isExistUsername) {

                    registerProgress.setVisibility(View.VISIBLE);
                    auth.createUserWithEmailAndPassword(regEmail, regPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser currentUser = task.getResult().getUser();
                                        User user = new User(regUsername, regEmail, gender, regBirthDate, 0, 0, 0, 0);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        db.collection("Users").document(FirebaseAuth.getInstance()
                                                .getCurrentUser().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(username.getEditText().getText().toString())
                                                            .build();

                                                    currentUser.updateProfile(profileUpdate);
                                                    currentUser.sendEmailVerification();

                                                    registerProgress.setVisibility(View.GONE);

                                                    Toast.makeText(getContext(), "Registado com sucesso", Toast.LENGTH_LONG).show();

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setMessage("Tem de verificar a sua conta no email");
                                                    AlertDialog dialog = builder.show();
                                                    TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
                                                    messageText.setGravity(Gravity.CENTER);
                                                    dialog.show();

                                                    getFragmentManager().popBackStack();

                                                } else {
                                                    registerProgress.setVisibility(View.GONE);
                                                    Toast.makeText(getContext(),
                                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    } else {
                                        registerProgress.setVisibility(View.GONE);
                                        if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                                            email.setError("Email já existe");
                                        } else {
                                            Toast.makeText(getContext(),
                                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                } else {
                    username.setError("Username já existe");
                }
            }
        });


        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (auth.getCurrentUser() != null) {

        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date date = c.getTime();

        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        regBirthDate = dateFormat.format(date);

        this.day = dayOfMonth;
        this.month = monthOfYear;
        this.year = year;

        editDate.getEditText().setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        } else {
            username.setError(null);
            isCheckUsername = true;
        }
    }


    private void checkEmail() {
        Matcher mEmail = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$").matcher(email.getEditText().getText().toString());
        if (email.getEditText().length() == 0) {
            email.setError("Campo Obrigatório");
            isCheckEmail = false;
        } else if (!mEmail.find()) {
            email.setError("Email inválido");
            isCheckEmail = false;
        } else {
            isCheckEmail = true;
            email.setError(null);
        }
    }

    private void checkGender() {
        if (radioMale.isChecked()) {
            gender = "Masculino";
            isCheckGender = true;
            genderInput.setError(null);
        }
        if (radioFemale.isChecked()) {
            gender = "Feminino";
            isCheckGender = true;
            genderInput.setError(null);
        }
    }

    private void checkDate() {
        if (editDate.getEditText().getText().toString().equals("  /  /    ")) {
            editDate.setError("Campo Obrigatório");
            isCheckDate = false;
        } else {
            isCheckDate = true;
            editDate.setError(null);
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


    private void checkAllFields() {
        if (isCheckUsername && isCheckEmail && isCheckGender && isCheckDate && isCheckPassword &&
                isCheckConfirmPassword) {
            isCheckAll = true;
            registerButton.setEnabled(true);
        } else {
            isCheckAll = false;
            registerButton.setEnabled(false);
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
}