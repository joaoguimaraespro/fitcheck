package pt.ipp.estg.fitcheck.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pt.ipp.estg.fitcheck.Adapters.RankingRecyclerViewAdapter;
import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.R;

/**
 * A fragment representing a list of Items.
 */
public class RankingFragment extends Fragment {


    private User user1, user2;
    private List<User> userList, example;
    private FirebaseFirestore db;

    public RankingFragment() {
    }

    public static RankingFragment newInstance(int columnCount) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_list, container, false);

        db.collection("Users").orderBy("totalDistance", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()){
                        User user = document.toObject(User.class);

                        Log.d("Users", "" + user.username);
                        userList.add(user);
                    }
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_ranking);
                    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    recyclerView.setAdapter(new RankingRecyclerViewAdapter(userList));
                }
            }
        });

        return view;
    }
}