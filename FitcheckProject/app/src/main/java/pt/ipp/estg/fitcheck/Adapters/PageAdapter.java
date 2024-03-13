package pt.ipp.estg.fitcheck.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pt.ipp.estg.fitcheck.Fragments.TrainingListFragment;

public class PageAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 3;
    public PageAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new TrainingListFragment();
        Bundle args = new Bundle();
        switch (position){
            case 0:
                args.putString(TrainingListFragment.ARG_OBJECT, "caminhada");
                fragment.setArguments(args);
                return fragment;
            case 1:
                args.putString(TrainingListFragment.ARG_OBJECT, "corrida");
                fragment.setArguments(args);
                return fragment;
        }
        args.putString(TrainingListFragment.ARG_OBJECT, "bicicleta");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
