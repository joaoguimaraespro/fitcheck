package pt.ipp.estg.fitcheck.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import pt.ipp.estg.fitcheck.Adapters.MyTreinoListRecyclerViewAdapter;
import pt.ipp.estg.fitcheck.DataBases.TrainingDB;
import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.R;

/**
 * A fragment representing a list of Items.
 */
public class TrainingListFragment extends Fragment {

    private FragmentChange mContext;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static final String ARG_OBJECT = "tipo";
    private String tipo;
    private TrainingDB treinoDB;
    private List<Training> userTreinos;
    private RecyclerView recyclerView;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;


    public TrainingListFragment() {
        this.mContext = (FragmentChange) getContext();
    }


    public static TrainingListFragment newInstance(int columnCount) {
        TrainingListFragment fragment = new TrainingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        treinoDB = TrainingDB.getInstance(getActivity().getApplicationContext());

        Bundle args = getArguments();
        tipo = args.getString(ARG_OBJECT);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treino_item_list, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = this.getView();
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        treinoDB.daoAccess().findTreinoByUserAndType(user.getUid(),tipo).observe(getViewLifecycleOwner(), treinos -> {
            userTreinos = treinos;
            recyclerView.setAdapter(new MyTreinoListRecyclerViewAdapter(userTreinos,getActivity()));
        });

    }



}