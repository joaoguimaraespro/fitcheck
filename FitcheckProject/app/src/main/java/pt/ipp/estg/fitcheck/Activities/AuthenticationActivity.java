package pt.ipp.estg.fitcheck.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Fragments.AuthFragments.LoginFragment;
import pt.ipp.estg.fitcheck.R;

public class AuthenticationActivity extends AppCompatActivity implements FragmentChange {

    private FirebaseAuth auth;

    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt");


        loginFragment = new LoginFragment(this);
        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.replace(R.id.ContainerFragment, loginFragment);
        tr.addToBackStack(null);
        tr.commit();


    }

    @Override
    public void exchangeFrag(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction tr = fragmentManager.beginTransaction();

        tr.replace(R.id.ContainerFragment, fragment);
        tr.addToBackStack(null);
        tr.commit();
    }

    @Override
    public void onBackPressed() {
        if (loginFragment.isVisible()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(intent);
            finish();
        }
    }
}