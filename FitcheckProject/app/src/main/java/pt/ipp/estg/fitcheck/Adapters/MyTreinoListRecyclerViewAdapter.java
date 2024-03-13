package pt.ipp.estg.fitcheck.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.Fragments.TrainingDetailsFragment;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.databinding.FragmentTreinoListBinding;


public class MyTreinoListRecyclerViewAdapter extends RecyclerView.Adapter<MyTreinoListRecyclerViewAdapter.ViewHolder> {

    private final List<Training> mValues;
    private Context mContext;

    public MyTreinoListRecyclerViewAdapter(List<Training> items, Context context) {
        mValues = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentTreinoListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int temp = mValues.get(position).duracao;
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).data);
        holder.mContentView.setText(String.format("%02dh %02dm %02ds", temp / 3600000, (temp / 60000) % 60, (temp / 1000) % 60));
        holder.mSeeDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putSerializable("trainingDetails", (Serializable) mValues.get(position));
                TrainingDetailsFragment tr = new TrainingDetailsFragment();
                tr.setArguments(args);
                ((MenuActivity) mContext).exchangeFrag(tr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public final Button mSeeDetailsButton;
        public Training mItem;

        public ViewHolder(FragmentTreinoListBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mSeeDetailsButton = binding.details;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}