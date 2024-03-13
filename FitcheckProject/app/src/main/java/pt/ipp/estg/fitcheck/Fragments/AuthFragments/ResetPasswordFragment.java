package pt.ipp.estg.fitcheck.Fragments.AuthFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.R;


public class ResetPasswordFragment extends Fragment {

    private FragmentChange context;

    private FirebaseAuth auth;

    private TextInputLayout email;

    private Button resetButton;

    private ProgressBar resetProgress;


    public ResetPasswordFragment(FragmentChange context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
    }

    public ResetPasswordFragment() {
        this.context = (FragmentChange) getContext();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");
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
        View context = inflater.inflate(R.layout.fragment_reset_password, container, false);

        email = context.findViewById(R.id.textFieldEmail);
        resetButton = context.findViewById(R.id.buttonReset);
        resetProgress = context.findViewById(R.id.resetProgress);

        resetButton.setEnabled(false);

        email.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEmail();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetProgress.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Foi-lhe enviado uma email para restaurar a palavra-passe", Toast.LENGTH_SHORT).show();

                            resetProgress.setVisibility(View.GONE);
                            getActivity().onBackPressed();
                        }else{
                            resetProgress.setVisibility(View.GONE);
                            if (task.getException().getMessage().equals("There is no user record corresponding to this identifier." +
                                    " The user may have been deleted.")) {
                                email.setError("Email inexistente");
                            }
                        }
                    }
                });
            }
        });


        return context;
    }


    private void checkEmail() {
        Matcher mEmail = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
                .matcher(email.getEditText().getText().toString());
        if (email.getEditText().length() == 0) {
            email.setError("Campo Obrigatório");
            resetButton.setEnabled(false);
        } else if (!mEmail.find()) {
            email.setError("Email inválido");
            resetButton.setEnabled(false);
        } else {
            email.setError(null);
            resetButton.setEnabled(true);
        }
    }
}