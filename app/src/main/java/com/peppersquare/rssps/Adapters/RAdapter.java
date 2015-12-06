package com.peppersquare.rssps.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.peppersquare.rssps.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sai on 28-11-2015.
 */
public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    private ArrayList<HashMap<String, String>> mDataset = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title, tv_category, tv_description, tv_date;
        public ImageView iv_image, iv_fav, iv_share;
        public CardView cardView;


        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.title);
            tv_category = (TextView) v.findViewById(R.id.category);
            tv_description = (TextView) v.findViewById(R.id.description);
            tv_date = (TextView) v.findViewById(R.id.date);

            iv_image = (ImageView) v.findViewById(R.id.image);
            iv_fav = (ImageView) v.findViewById(R.id.fav);
            iv_share = (ImageView) v.findViewById(R.id.share);

            cardView = (CardView) v.findViewById(R.id.card_view);

        }
    }

    public RAdapter(Context context, ArrayList<HashMap<String, String>> dataset) {
        mContext = context;
        mDataset = dataset;
    }

    @Override
    public RAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // position should never be final

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        HashMap<String, String> map = mDataset.get(position);

        String imageurl = map.get("imageUrl");

        Picasso.with(mContext)
                .load(imageurl)
                .placeholder(R.mipmap.placeholde)
                .resize(dpToPx(128), dpToPx(128))
                .centerInside()
                .error(R.mipmap.placeholde)
                .into(holder.iv_image);

        holder.tv_title.setText(map.get("title"));

        String fromHtml = Html.fromHtml(map.get("description")).toString();
        String lastStr = fromHtml.substring(2, fromHtml.length() - 1);

        holder.tv_description.setText(lastStr.trim());
        holder.tv_date.setText(map.get("pubDate"));


        holder.cardView.setOnClickListener(new onCardClick(holder.getAdapterPosition()));

        holder.iv_share.setOnClickListener(new onPostShare(holder.getAdapterPosition()));

        holder.iv_fav.setOnClickListener(new onAddfav(holder.getAdapterPosition()));

    }


    class onCardClick implements View.OnClickListener {
        int position;

        public onCardClick(int adapterPosition) {
            this.position = adapterPosition;
        }

        @Override
        public void onClick(View view) {
            HashMap<String, String> map = mDataset.get(position);
            Log.d("Position", map.get("origLink"));
            Uri uri = Uri.parse(map.get("origLink"));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        }
    }

    class onPostShare implements View.OnClickListener {
        int position;

        public onPostShare(int adapterPosition) {
            this.position = adapterPosition;
        }

        @Override
        public void onClick(View view) {
            HashMap<String, String> map = mDataset.get(position);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    map.get("title") + " \n" + map.get("origLink"));
            sendIntent.setType("text/plain");
            mContext.startActivity(Intent.createChooser(sendIntent, "Share this post"));
        }
    }

    class onAddfav implements View.OnClickListener {
        int position;
        public onAddfav(int adapterPosition) {
            this.position = adapterPosition;
        }
        @Override
        public void onClick(View view) {
            Toast.makeText(mContext, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources()
                .getDisplayMetrics();
        return Math.round(dp
                * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}