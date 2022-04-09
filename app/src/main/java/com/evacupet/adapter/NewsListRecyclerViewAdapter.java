package com.evacupet.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.model.NewsModel;
import com.evacupet.utility.BlogPostDialog;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsListRecyclerViewAdapter extends RecyclerView.Adapter<NewsListRecyclerViewAdapter.ViewHolder> {



    private NewsModel[] listdata;

    // RecyclerView recyclerView;
    public NewsListRecyclerViewAdapter(NewsModel[] listdata) {
        this.listdata = listdata;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.fragment_newslist, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final NewsModel myListData = listdata[position];
        holder.title.setText(listdata[position].title);
        holder.date.setText(listdata[position].date);
        holder.shortDescription.setText(listdata[position].shortDescription);

        try {
            Picasso.get().load(listdata[position].image).into(holder.imageView);
        }catch (Exception e){

        }
        //holder.imageView.setImageURI(new URL("").toURI());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(),"click on item: "+myListData.title,Toast.LENGTH_LONG).show();
                Activity activity = (Activity) view.getContext();
                new BlogPostDialog(activity,myListData).show();

            }
        });

    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title) TextView title;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.content) TextView shortDescription;
        @BindView(R.id.image) ImageView imageView;
        @BindView(R.id.linearLayout) LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

    }


}
