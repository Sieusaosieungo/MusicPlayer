package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    Field[] fields;
    int index;

    Button playButton;
    SeekBar progressBar;
    SeekBar soundBar;
    TextView playedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer player;
    int totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fields = R.raw.class.getFields();
        index = 0;

        setupView();
        try {
            initPlayer();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        initProgressBar();
        initSoundBar();
        initThread();

    }

    private void setupView() {
        playButton = (Button) findViewById(R.id.playButton);
        playedTimeLabel = (TextView) findViewById(R.id.playedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
    }

    private void initPlayer() throws IllegalAccessException {
        player = MediaPlayer.create(this, fields[index].getInt(fields[index]));
        player.setLooping(true);
        player.seekTo(0);
        player.setVolume(0.5f, 0.5f);
        totalTime = player.getDuration();
        setTitle(fields[index].getName());
    }

    private void initProgressBar() {
        progressBar = (SeekBar) findViewById(R.id.progressBar);
        progressBar.setMax(totalTime);
        progressBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        player.seekTo(progress);
                        progressBar.setProgress(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            }
        );
    }

    private void initSoundBar() {
        soundBar = (SeekBar) findViewById(R.id.soundBar);
        soundBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        player.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void initThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (player != null) {
                    try {
                        Message msg = new Message();
                        msg.what = player.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            progressBar.setProgress(currentPosition);

            // Update Labels.
            String playedTime = createTimeLabel(currentPosition);
            playedTimeLabel.setText(playedTime);

//            String remainingTime = createTimeLabel(totalTime-currentPosition);
//            remainingTimeLabel.setText("- " + remainingTime);
            String total = createTimeLabel(totalTime);
            remainingTimeLabel.setText(total);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void didTapPlayButton(View view) {
    play();


    }

    private void play() {
        if (!player.isPlaying()) {
            // Stopping
            player.start();
            playButton.setBackgroundResource(R.drawable.stop);

        } else {
            // Playing
            player.pause();
            playButton.setBackgroundResource(R.drawable.play);
        }
    }

    public void didTapPrevButton(View view) throws IllegalAccessException {
        player.stop();
        if (index == 0) {
            index = fields.length - 1;
        } else {
            index--;
        }
        updatePlayer();
        play();
    }

    public void didTapNextButton(View view) throws IllegalAccessException {
        player.stop();
        if (index == fields.length - 1) {
            index = 0;
        } else {
            index++;
        }
        updatePlayer();
        play();
    }

    private void updatePlayer() throws IllegalAccessException {
        initPlayer();
        initProgressBar();
    }

}
