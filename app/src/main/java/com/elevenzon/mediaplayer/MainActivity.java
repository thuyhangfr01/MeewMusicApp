package com.elevenzon.mediaplayer;

import static android.nfc.NdefRecord.createUri;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Timer
    private CountDownTimer countDownTimer;
    private boolean mTimerRunning;
    private long mStartTimeMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private ImageView btnTimerSleep;
    private String[] listItems;
    long timeSleep = 0;

    private ArrayList<ModelAudio> audioArrayList;
    private RecyclerView recyclerView;
    private MediaPlayer mediaPlayer;
    private double current_pos, total_duration;
    private TextView current, total,audio_name, txtTimer;
    private ImageView prev, next, pause, shuffle, repeatOne;
    private SeekBar seekBar;
    private int audio_index = 0;
    public static final int PERMISSION_READ = 0;
    private Boolean shuffleFlag = false;
    private Boolean repeatFlag = false;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            setAudio();
        }

        btnTimerSleep = findViewById(R.id.btnTimerSleep);
        btnTimerSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimerSleep();
            }
        });
        txtTimer = findViewById(R.id.txtTimer);
    }

    public void setTimerSleep(){
        listItems = new String[]{"1 phút", "5 phút", "10 phút", "15 phút", "30 phút", "1 tiếng"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Dừng âm thanh trong");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        timeSleep = 60000;
                        break;
                    case 1:
                        timeSleep = 300000;
                        break;
                    case 2:
                        timeSleep = 600000;
                        break;
                    case 3:
                        timeSleep = 900000;
                        break;
                    case 4:
                        timeSleep = 1800000;
                        break;
                    case 5:
                        timeSleep = 3600000;
                        break;
                }
                setCountDownTimer(timeSleep);
                dialogInterface.dismiss();
            }
        });
        mBuilder.show();
    }

    public void setCountDownTimer(long timeSleep){
        new CountDownTimer(timeSleep, 1000){
            @Override
            public void onTick(long l) {
                txtTimer.setText("" + l/1000);
            }

            @Override
            public void onFinish() {
                txtTimer.setVisibility(View.GONE);
                mediaPlayer.pause();
                pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
            }
        }.start();
    }


    public void setAudio() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        current = (TextView) findViewById(R.id.current);
        total = (TextView) findViewById(R.id.total);
        audio_name = (TextView) findViewById(R.id.audio_name);
        prev = (ImageView) findViewById(R.id.prev);
        next = (ImageView) findViewById(R.id.next);
        pause = (ImageView) findViewById(R.id.pause);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        shuffle = (ImageView) findViewById(R.id.suffle);
        repeatOne = (ImageView) findViewById(R.id.repeatOnce);

        audioArrayList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        getAudioFiles();

        //seekbar change listner
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audio_index++;
                if (audio_index < (audioArrayList.size())) {
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }

            }
        });

        if (!audioArrayList.isEmpty()) {
            playAudio(audio_index);
            prevAudio();
            nextAudio();
            setPause();
            setSuffle();
            setRepeatOne(audio_index);
        }
    }

    //play audio file
    public void playAudio(int pos) {
        try  {
            mediaPlayer.reset();
            if(shuffleFlag && !repeatFlag){
                pos = getRandom(audioArrayList.size() - 1);
            }
            else if (!shuffleFlag && !repeatFlag){
                pos = ((pos) % audioArrayList.size());
            }
            //set file path
            mediaPlayer.setDataSource(this, audioArrayList.get(pos).getaudioUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
            audio_name.setText(audioArrayList.get(pos).getaudioTitle());
            audio_index=pos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        setAudioProgress();
    }

    public int getRandom(int i){
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    //set audio progress
    public void setAudioProgress() {
        //get the audio duration
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        //display the audio duration
        total.setText(timerConversion((long) total_duration));
        current.setText(timerConversion((long) current_pos));
        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = mediaPlayer.getCurrentPosition();
                    current.setText(timerConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    //play previous audio
    public void prevAudio() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleFlag && !repeatFlag){
                    audio_index = getRandom(audioArrayList.size() - 1);
                }
                else if (!shuffleFlag && !repeatFlag){
                    audio_index = ((audio_index - 1) < 0 ? (audioArrayList.size() - 1) : (audio_index -1));
                }
                playAudio(audio_index);
            }
        });
    }

    //play next audio
    public void nextAudio() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleFlag && !repeatFlag){
                    audio_index = getRandom(audioArrayList.size() - 1);
                }
                else if (!shuffleFlag && !repeatFlag){
                    audio_index = ((audio_index - 1) < (audioArrayList.size()-1) ? (audio_index + 1) : 0);
                }
                playAudio(audio_index);
            }
        });
    }

    //pause audio
    public void setPause() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
            } else {
                mediaPlayer.start();
                pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
            }
            }
        });
    }

    //suffle audio
    public void setSuffle(){
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleFlag){
                    shuffleFlag = false;
                    shuffle.setImageResource(R.drawable.suffle);
                }
                else{
                    shuffleFlag = true;
                    shuffle.setImageResource(R.drawable.suffle_checked);
                }
            }
        });


    }

    //repeat audio
    public void setRepeatOne(final int pos){
        repeatOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatFlag){
                    repeatFlag = false;
                    repeatOne.setImageResource(R.drawable.repeatonce);

                }
                else{
                    repeatFlag = true;
                    repeatOne.setImageResource(R.drawable.repeatonce_checked);
                }
            }
        });
    }

    //time conversion
    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }

    //fetch the audio files from storage
    public void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ModelAudio modelAudio = new ModelAudio();
                modelAudio.setaudioTitle(title);
                modelAudio.setaudioArtist(artist);
                modelAudio.setaudioUri(Uri.parse(url));
                modelAudio.setaudioDuration(duration);
                audioArrayList.add(modelAudio);

            } while (cursor.moveToNext());
        }

        AudioAdapter adapter = new AudioAdapter(this, audioArrayList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playAudio(pos);
            }
        });
    }

    //runtime storage permission
    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case  PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                    } else {
                        setAudio();
                    }
                }
            }
        }
    }

    //release mediaplayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.release();
        }
    }
}