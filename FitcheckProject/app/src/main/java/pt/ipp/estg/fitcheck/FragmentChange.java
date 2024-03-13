package pt.ipp.estg.fitcheck;

import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface FragmentChange extends Serializable {

    public void exchangeFrag(Fragment fragment);
}
