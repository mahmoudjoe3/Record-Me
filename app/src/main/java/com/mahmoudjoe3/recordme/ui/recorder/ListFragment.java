package com.mahmoudjoe3.recordme.ui.recorder;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mahmoudjoe3.recordme.pojo.Audio;
import com.mahmoudjoe3.recordme.R;
import com.mahmoudjoe3.recordme.adapter.AudioListAdapter;
import com.mahmoudjoe3.recordme.useCase.TimeAgo;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ListFragment extends Fragment implements View.OnClickListener {
    private LinearLayout playerSheet;
    private BottomSheetBehavior behavior;
    private RecyclerView audioRecycleView;
    private AudioListAdapter audioAdapter;
    private ImageButton deleteAudios, backBTN;
    private ImageButton play_pause_audio_item;

    private MediaPlayer mediaPlayer;
    private Audio playingAudio;
    private Boolean isPlaying = false;

    //player sheet
    private ImageButton sheet_play, sheet_forward, sheet_backward;
    private TextView sheet_playingStatus, sheet_file_name;
    private SeekBar sheet_seekBar;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        setupPlayerSheet(view);
        setupAdapter();
        clickListener();
    }

    private void clickListener() {
        deleteAudios.setOnClickListener(this);
        backBTN.setOnClickListener(this);
        sheet_backward.setOnClickListener(this);
        sheet_play.setOnClickListener(this);
        sheet_forward.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteAudios:
                deleteAudios();
                break;
            case R.id.backBTN:
                requireActivity().onBackPressed();
                break;
            case R.id.playBTN:
                if(isPlaying)
                    pauseAudio();
                else if(playingAudio!=null)
                    resumeAudio();
                break;
            case R.id.backwardBTN:
                if(sheet_seekBar.getProgress()-5000>0) {
                    mediaPlayer.seekTo(sheet_seekBar.getProgress() - 5000);
                    sheet_seekBar.setProgress(sheet_seekBar.getProgress() - 5000);
                }
                break;
            case R.id.forwardBTN:
                if(sheet_seekBar.getProgress()+5000<sheet_seekBar.getMax()) {
                    mediaPlayer.seekTo(sheet_seekBar.getProgress() + 5000);
                    sheet_seekBar.setProgress(sheet_seekBar.getProgress() + 5000);
                }
                break;
        }
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        sheet_play.setImageResource(R.drawable.play);
        isPlaying=false;
    }

    private void resumeAudio() {
        if(mediaPlayer.getCurrentPosition()==sheet_seekBar.getMax()){
            playAudio(playingAudio);
        }
        mediaPlayer.start();
        sheet_play.setImageResource(R.drawable.ic_pause);
        isPlaying=true;
    }


    private void deleteAudios() {
        audioAdapter.getSelectedFiles().forEach(audio -> {
            if (!new File(audio.getPath()).delete())
                Toast.makeText(getActivity(), "error when deleting..", Toast.LENGTH_SHORT).show();
            else
                audioAdapter.delete(audio);
        });
    }

    private void setupAdapter() {
        audioRecycleView.setHasFixedSize(true);
        audioAdapter = new AudioListAdapter();
        audioAdapter.setAudioFiles(getAudioFiles());
        audioRecycleView.setAdapter(audioAdapter);
        audioAdapter.setOnPlayClickListener(new AudioListAdapter.onPlayClickListener() {
            @Override
            public void onPlay(Audio audio, ImageButton play_pause_audio) {
                if(isPlaying)
                    stopAudio();
                playingAudio=audio;
                play_pause_audio_item=play_pause_audio;
                playAudio(playingAudio);
            }

            @Override
            public void onStop() {
                stopAudio();
            }
        });

        audioAdapter.setOnItemLongClickListener(new AudioListAdapter.onItemLongClickListener() {
            @Override
            public void onDidnt() {
                deleteAudios.setVisibility(View.GONE);
            }

            @Override
            public void onHave() {
                deleteAudios.setVisibility(View.VISIBLE);
            }
        });
    }

    private void playAudio(Audio audio) {
        if(!isPlaying) {
            isPlaying = true;
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audio.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.setOnCompletionListener(mp -> {
                play_pause_audio_item.setImageResource(R.drawable.ic__play_circle);
                play_pause_audio_item.setTag("0");
                isPlaying = false;
                sheet_playingStatus.setText("Stopped");
                sheet_play.setImageResource(R.drawable.play);
                sheet_playingStatus.setText("Finished");
            });
            sheet_playingStatus.setText("Playing...");
            sheet_file_name.setText(audio.getName().split("\\.")[0]);
            sheet_play.setImageResource(R.drawable.ic_pause);

            sheet_seekBar.setMax(mediaPlayer.getDuration());
            Handler handler=new Handler();
            Runnable runnable =new Runnable() {
                @Override
                public void run() {
                    sheet_seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this,0);
                }
            };
            handler.postDelayed(runnable,0);
        }
    }

    private void stopAudio() {
        if(isPlaying) {
            mediaPlayer.stop();
            play_pause_audio_item.setImageResource(R.drawable.ic__play_circle);
            play_pause_audio_item.setTag("0");
            isPlaying = false;
            sheet_playingStatus.setText("Stopped");
            sheet_play.setImageResource(R.drawable.play);
        }
    }

    private void setupPlayerSheet(@NotNull View view) {
        behavior = BottomSheetBehavior.from(playerSheet);
        playerSheet.setOnClickListener(v -> {
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
        );
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {

            }
        });

        sheet_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(playingAudio!=null)
                    pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(playingAudio!=null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    resumeAudio();
                }
            }
        });
    }

    private void findView(@NotNull View view) {
        playerSheet = view.findViewById(R.id.player_sheet);
        audioRecycleView = view.findViewById(R.id.audio_list);
        deleteAudios = view.findViewById(R.id.deleteAudios);
        backBTN = view.findViewById(R.id.backBTN);
        sheet_backward = view.findViewById(R.id.backwardBTN);
        sheet_play = view.findViewById(R.id.playBTN);
        sheet_forward = view.findViewById(R.id.forwardBTN);
        sheet_file_name = view.findViewById(R.id.file_name);
        sheet_playingStatus = view.findViewById(R.id.playing_state);
        sheet_seekBar = view.findViewById(R.id.seekBar);
    }

    private List<Audio> getAudioFiles() {
        List<Audio> audios = new ArrayList<>();
        String root = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(root);
        List<File> files ;
        files = Arrays.asList(Objects.requireNonNull(directory.listFiles()));
        files.forEach(file -> {
            Audio audio = new Audio();
            audio.setName(file.getName());
            audio.setPath(file.getAbsolutePath());
            audio.setCreatedDate(TimeAgo.getTimeAgo(file.lastModified()));
            audio.setTime(getDuration(file));
            audio.setSpace(Formatter.formatShortFileSize(getActivity(), file.length()));
            audios.add(audio);
        });
        return audios;
    }

    private String getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return TimeAgo.formatMilliSecond(Long.parseLong(durationStr));
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying)
            stopAudio();
    }
}
