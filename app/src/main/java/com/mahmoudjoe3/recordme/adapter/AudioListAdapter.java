package com.mahmoudjoe3.recordme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mahmoudjoe3.recordme.pojo.Audio;
import com.mahmoudjoe3.recordme.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.VH> {
    private List<Audio> audioFiles;
    private onPlayClickListener onPlayClickListener;
    private onItemLongClickListener onItemLongClickListener;
    private Boolean isSelectedMode = false;
    private List<Audio> selectedFiles = new ArrayList<>();


    public void setAudioFiles(List<Audio> audioFiles) {
        if (audioFiles != null)
            this.audioFiles = audioFiles;
        else this.audioFiles = new ArrayList<>();
    }

    public void delete(Audio audio) {
        int pos = audioFiles.indexOf(audio);
        audioFiles.remove(audio);
        notifyItemRemoved(pos);
    }

    public List<Audio> getSelectedFiles() {
        return selectedFiles;
    }

    public void setOnPlayClickListener(AudioListAdapter.onPlayClickListener onItemClickListener) {
        this.onPlayClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(AudioListAdapter.onItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public VH onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull AudioListAdapter.VH holder, int position) {
        Audio audio = audioFiles.get(position);
        holder.name.setText(audio.getName().split("\\.")[0]);
        holder.time.setText(audio.getTime());
        holder.createdDate.setText(audio.getCreatedDate());
        holder.size.setText(audio.getSpace());
        holder.play_pause_audio.setOnClickListener(v -> {
            if (holder.play_pause_audio.getTag() == "1") {
                holder.play_pause_audio.setTag("0");
                holder.play_pause_audio.setImageResource(R.drawable.ic__play_circle);
                onPlayClickListener.onStop();
            } else {
                holder.play_pause_audio.setTag("1");
                holder.play_pause_audio.setImageResource(R.drawable.ic__pause_circle);
                onPlayClickListener.onPlay(audio, holder.play_pause_audio);
            }

        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectedMode) {
                isSelectedMode = true;
                onItemLongClickListener.onHave();
                if (selectedFiles.contains(audio)) {
                    holder.checkView.setVisibility(View.GONE);
                    selectedFiles.remove(audio);
                } else {
                    holder.checkView.setVisibility(View.VISIBLE);
                    selectedFiles.add(audio);
                }
                if (selectedFiles.size() == 0) {
                    isSelectedMode = false;
                    onItemLongClickListener.onDidnt();
                }
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectedMode) {
                if (selectedFiles.contains(audio)) {
                    holder.checkView.setVisibility(View.GONE);
                    selectedFiles.remove(audio);
                } else {
                    holder.checkView.setVisibility(View.VISIBLE);
                    selectedFiles.add(audio);
                }

                if (selectedFiles.size() == 0) {
                    onItemLongClickListener.onDidnt();
                    isSelectedMode = false;
                }

            } else {

            }
        });


    }

    @Override
    public int getItemCount() {
        return audioFiles.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        ImageButton play_pause_audio;
        TextView name, createdDate, time, size;
        CardView checkView;

        public VH(@NotNull View itemView) {
            super(itemView);
            play_pause_audio = itemView.findViewById(R.id.playAudio);
            name = itemView.findViewById(R.id.audio_name);
            createdDate = itemView.findViewById(R.id.audio_created_date);
            time = itemView.findViewById(R.id.audio_time);
            size = itemView.findViewById(R.id.audio_size);
            checkView = itemView.findViewById(R.id.checkView);
        }
    }

    public interface onPlayClickListener {
        void onPlay(Audio audio,ImageButton play_pause_audio);

        void onStop();
    }

    public interface onItemLongClickListener {
        void onDidnt();

        void onHave();
    }

}
