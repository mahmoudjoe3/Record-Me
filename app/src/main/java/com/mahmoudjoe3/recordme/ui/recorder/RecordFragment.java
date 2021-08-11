package com.mahmoudjoe3.recordme.ui.recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieAnimationView;
import com.mahmoudjoe3.recordme.R;
import com.mahmoudjoe3.recordme.globalVar.Global;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RecordFragment";
    private NavController navController;
    private ImageButton listBTN, saveBTN, deleteBTN;
    private CardView saveCard, deleteCard;
    private LottieAnimationView record_lottie;
    private LottieAnimationView recording_lottie;
    private Chronometer chronometer;
    private TextView hint;
    private String fileFullPath, fileName;


    private boolean isRecording = false;
    private MediaRecorder mediaRecorder = null;
    long timeWhenStopped = 0;


    public RecordFragment() {
        // Required empty public constructor
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @org.jetbrains.annotations.NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        findView(view);
        ClickEvent();
    }

    private void ClickEvent() {
        listBTN.setOnClickListener(this);
        record_lottie.setOnClickListener(this);
        saveBTN.setOnClickListener(this);
        deleteBTN.setOnClickListener(this);
    }

    private void findView(@NotNull View view) {
        listBTN = view.findViewById(R.id.listBTN);
        record_lottie = view.findViewById(R.id.recordBTN);
        recording_lottie = view.findViewById(R.id.recording);
        chronometer = view.findViewById(R.id.counter);
        hint = view.findViewById(R.id.hint);

        saveBTN = view.findViewById(R.id.saveBTN);
        saveCard = view.findViewById(R.id.saveCard);
        deleteBTN = view.findViewById(R.id.deleteBTN);
        deleteCard = view.findViewById(R.id.deleteCard);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listBTN:
                goToRecordsList();
                break;
            case R.id.recordBTN:
                if (!isRecording) {
                    if (checkPermission()) {
                        isRecording = true;
                        record_lottie.playAnimation();
                        recording_lottie.setVisibility(View.VISIBLE);
                        recording_lottie.playAnimation();
                        saveCard.setVisibility(View.GONE);
                        deleteCard.setVisibility(View.GONE);

                        if (timeWhenStopped != 0)
                            continueRecording();
                        else
                            startRecording();
                    }
                } else {
                    stopRecording();
                }
                break;
            case R.id.saveBTN:
                save_delete_Recording("save");
                break;
            case R.id.deleteBTN:
                save_delete_Recording("delete");
                break;


        }
    }

    private void goToRecordsList() {
        if(!hint.getText().toString().equalsIgnoreCase(getString(R.string.record_hint))) {
            stopRecording();
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
                            .setCancelable(false).setTitle("Close Recording window")
                            .setMessage("Do you want to save the record or delete it")
                            .setPositiveButton("Save", (dialog, which) -> {
                                save_delete_Recording("save");
                                navController.navigate(R.id.action_recordFragment_to_listFragment);
                            })
                            .setNegativeButton("Cancel",(dialog, which) -> dialog.dismiss());
            builder.show();
        }else
            navController.navigate(R.id.action_recordFragment_to_listFragment);
    }

    private void startRecording() {
        startTimer();

        String root = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        fileName = "RECORD_ME_" + format.format(new Date()) + ".3gp";
        fileFullPath = root + "/" + fileName;
        hint.setText(getString(R.string.recording_file_name) + fileName);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileFullPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        isRecording = false;
        record_lottie.pauseAnimation();
        recording_lottie.setVisibility(View.INVISIBLE);
        recording_lottie.pauseAnimation();
        saveCard.setVisibility(View.VISIBLE);
        deleteCard.setVisibility(View.VISIBLE);

        hint.setText("Recording Stopped,\n " + fileName);
        stopTimer();
        mediaRecorder.pause();
    }

    private void continueRecording() {
        startTimer();
        mediaRecorder.resume();
    }

    private void save_delete_Recording(String s) {
        saveCard.setVisibility(View.GONE);
        deleteCard.setVisibility(View.GONE);
        record_lottie.setProgress(0f);
        resetTimer();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        hint.setText(getString(R.string.record_hint));
        if (s.equalsIgnoreCase("save"))
            saveFileDialog();
        else {
            deleteFile(fileFullPath);
        }
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.delete())
            Toast.makeText(getActivity(), "File deleted Successfully", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "Error when deleting th file", Toast.LENGTH_SHORT).show();
    }

    private void saveFileDialog() {
        final EditText edittext = new EditText(getActivity());
        edittext.setText(fileName);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
                        .setCancelable(false).setTitle("Save Recording Audio")
                        .setMessage("Enter Audio Name ")
                        .setView(edittext)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String newName=edittext.getText().toString();
                            if(newName.isEmpty())
                                Toast.makeText(getActivity(), "File saved as: "+fileName, Toast.LENGTH_SHORT).show();
                            else {
                                if(!newName.contains(".3gp"))
                                    newName+=".3gp";
                                renameFile(fileName,newName);
                                Toast.makeText(getActivity(), "File Saved as: "+edittext.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Delete",(dialog, which) -> deleteFile(fileFullPath));
        builder.show();
    }

    private void renameFile(String oldFileName, String newFileName) {
        File directory = new File(getActivity().getExternalFilesDir("/").getAbsolutePath());
        File from      = new File(directory, oldFileName);
        File to        = new File(directory, newFileName);
        from.renameTo(to);
    }


    private void stopTimer() {
        timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();
    }

    private void startTimer() {
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronometer.start();
    }

    private void resetTimer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        timeWhenStopped = 0;
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) return true;
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO}, Global.RECORD_AUDIO_REQ_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording) {
            deleteFile(fileFullPath);
            stopRecording();
        }
    }
}