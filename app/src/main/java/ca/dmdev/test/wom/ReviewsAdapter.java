package ca.dmdev.test.wom;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.ExceptionCatchingInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Doug on 2016-03-06.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        ImageView photo;
        TextView title;
        TextView description;
        public ReviewViewHolder(View v) {
            super(v);
            cardView = (CardView) v.findViewById(R.id.review_card);
            photo = (ImageView) v.findViewById(R.id.review_photo);
            title = (TextView) v.findViewById(R.id.review_title);
            description = (TextView) v.findViewById(R.id.review_description);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReviewsAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_review_card, parent, false);
        context = parent.getContext();
        // set the view's size, margins, paddings and layout parameters

        ReviewViewHolder rvh = new ReviewViewHolder(v);
        return rvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReviewViewHolder rvh, int position) {
        rvh.title.setText(reviews.get(position).getTitle() + " TEST TITLE");
        rvh.description.setText(reviews.get(position).getDescription() + " TEST DESC");

        //get facebook user photo
        Glide
            .with(context)
            .load("https://graph.facebook.com/" + reviews.get(position).getOwnerId() + "/picture?width=150&height=150")
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .crossFade()
            .into(rvh.photo);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateData(List<Review> reviews){
        this.reviews = reviews;
        notifyDataSetChanged();
    }
}
