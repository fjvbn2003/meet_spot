package com.example.youngju.meet_spot;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    Context context;
    List<Item> items;
    int item_layout;

    public RecyclerAdapter(Context context, List<Item> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Item item = items.get(position);
        DownloadImageTask task = new DownloadImageTask(holder.image);

        task.execute(item.getImage());
        if(position == 0){
            holder.name.setText("나의 위치");
        }else {
            holder.name.setText("친구"+ position);
        }
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDesc());
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        /*holder.cardview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                items.remove(item.getNum());
                return false;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView desc;
        CardView cardview;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.desc);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
        }
    }



    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private static final String LOG_E_TAG = "DownloadImageTask";
        private final WeakReference<ImageView> containerImageView;

        public DownloadImageTask(ImageView imageView) {
            this.containerImageView = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            Bitmap mBitmap;
            try {
                mBitmap = BitmapFactory
                        .decodeStream((InputStream) new URL(args[0])
                                .getContent());


            } catch (Exception e) {
                e.printStackTrace();
                return  null;
            }

            return mBitmap;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = this.containerImageView.get();
            if (imageView != null) {
                imageView.setBackground(new BitmapDrawable(context.getResources(), result));
            }
        }
    }

}


