package pt.ipp.estg.fitcheck.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pt.ipp.estg.fitcheck.Adapters.PageAdapter;
import pt.ipp.estg.fitcheck.R;


public class StatisticsFragment extends Fragment {

    private static final int[] TABSICONS = {R.drawable.ic_walking, R.drawable.ic_run,
            R.drawable.ic_cycling};
    private static final String[] TABS = {"Caminhada", "Corrida",
            "Ciclismo"};

    PageAdapter pageAdapter;
    ViewPager2 viewPager;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pageAdapter = new PageAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(pageAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    tab.setText(TABS[position]);
                    tab.setIcon(TABSICONS[position]);
            }
        }).attach();
    }
}