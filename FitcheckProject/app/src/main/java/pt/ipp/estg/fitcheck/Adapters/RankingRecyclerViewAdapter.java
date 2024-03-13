package pt.ipp.estg.fitcheck.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ipp.estg.fitcheck.Models.User;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.databinding.FragmentRankingBinding;


public class RankingRecyclerViewAdapter extends RecyclerView.Adapter<RankingRecyclerViewAdapter.ViewHolder> {

    private final List<User> mValues;

    public RankingRecyclerViewAdapter(List<User> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentRankingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        int temp = position + 1;
        holder.mPosition.setText("" + temp);
        holder.mIdView.setText(mValues.get(position).username);
        Double distance = mValues.get(position).totalDistance / 1000.0;
        holder.mContentView.setText(String.format("%.2f",distance) + "km");
        switch (position){
            case 0:
                holder.mImageView.setVisibility(View.VISIBLE);
                holder.mImageView.setImageResource(R.drawable.ic__1nd_prize);
                break;
            case 1:
                holder.mImageView.setImageResource(R.drawable.ic__2nd_prize);
                break;
            case 2:
                holder.mImageView.setImageResource(R.drawable.ic__3rd_prize);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mPosition;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mImageView;
        public User mItem;

        public ViewHolder(FragmentRankingBinding binding) {
            super(binding.getRoot());
            mPosition = binding.rankPosition;
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mImageView = binding.icon;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}