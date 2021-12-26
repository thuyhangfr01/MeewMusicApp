package com.elevenzon.mediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.viewHolder> {

    Context context;
    ArrayList<ModelAudio> audioArrayList;
    public OnItemClickListener onItemClickListener;
    int lastPosition = -1;

    public AudioAdapter(Context context, ArrayList<ModelAudio> audioArrayList) {
        this.context = context;
        this.audioArrayList = audioArrayList;
    }

    @Override
    public AudioAdapter.viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_list, viewGroup, false);
        return new viewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(final AudioAdapter.viewHolder holder, final int i) {
            holder.title.setText(audioArrayList.get(i).getaudioTitle());
            holder.artist.setText(audioArrayList.get(i).getaudioArtist());
    }

    @Override
    public int getItemCount() {
        return audioArrayList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView menu, edit;
        public viewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            menu = (ImageView) itemView.findViewById(R.id.menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition(), v);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    PopupMenu popupMenu = new PopupMenu(menu.getContext(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                    popupMenu.show();
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }
}