package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

public class Player_Activity extends AppCompatActivity {
    ImageView play, next, prev , fl, fr;
    TextView name, start, stop;
    SeekBar seekBar;
    BarVisualizer barVisualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> my_songs;
    Thread update_seek_bar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        if (barVisualizer!=null)
        {
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

     //   getSupportActionBar().setTitle("Playing");
     //   getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     //   getSupportActionBar().setDisplayShowHomeEnabled(true);

        play= findViewById(R.id.play_btn_id);
        next= findViewById(R.id.btn_next_id);
        prev= findViewById(R.id.btn_prev_id);
        fl= findViewById(R.id.btn_fl_id);
        fr= findViewById(R.id.btn_fr_id);
        name= findViewById(R.id.txt_song_id);
        start= findViewById(R.id.text_start_id);
        stop= findViewById(R.id.text_stop_id);
        seekBar= findViewById(R.id.seekbar_id);
        barVisualizer= findViewById(R.id.blast);
        imageView= findViewById(R.id.image_view_id);

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i= getIntent();
        Bundle bundle = i.getExtras();

        my_songs=(ArrayList) bundle.getParcelableArrayList("songs");
        String song_name= i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        name.setSelected(true);
        Uri uri = Uri.parse(my_songs.get(position).toString());
        sname= my_songs.get(position).getName();
        name.setText(sname);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        update_seek_bar = new Thread(){
            public void run(){
                int total_duration = mediaPlayer.getDuration();
                int current_position = 0;

                while(current_position<total_duration)
                {
                     try{
                         sleep(500);
                         current_position= mediaPlayer.getCurrentPosition();
                         seekBar.setProgress(current_position);
                     }catch (InterruptedException | IllegalStateException e)
                    {
                         e.printStackTrace();
                    }

                }
            }
        };

      seekBar.setMax(mediaPlayer.getDuration());
        update_seek_bar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.av_red), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.av_dark_blue) ,PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });
        String end_time= crete_time(mediaPlayer.getDuration());
       stop.setText(end_time);

        final Handler handler = new Handler();
        final int delayy= 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currTime = crete_time(mediaPlayer.getCurrentPosition());
                start.setText(currTime);
                handler.postDelayed(this , delayy);
            }
        }, delayy);






        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    play.setBackgroundResource(R.drawable.middle);
                    mediaPlayer.pause();
                }else
                {
                    play.setBackgroundResource(R.drawable.pause);
                    mediaPlayer.start();
                }
            }
        });
        //next song automatically
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

        int audio_ses_id = mediaPlayer.getAudioSessionId();
        if (audio_ses_id != -1)
        {
            barVisualizer.setAudioSessionId(audio_ses_id);
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)% my_songs.size());
                Uri uri1 = Uri.parse(my_songs.get(position).toString());
                mediaPlayer= MediaPlayer.create(getApplicationContext(), uri1);
                sname= my_songs.get(position).getName();
                name.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.pause);
                start_animation(imageView);
                int audio_ses_id = mediaPlayer.getAudioSessionId();
                if (audio_ses_id != -1)
                {
                    barVisualizer.setAudioSessionId(audio_ses_id);
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position-1)<0)?(my_songs.size()-1):(position-1);
                Uri uri1 = Uri.parse(my_songs.get(position).toString());
                mediaPlayer= MediaPlayer.create(getApplicationContext(), uri1);
                sname= my_songs.get(position).getName();
                name.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.pause);
                start_animation(imageView);
                int audio_ses_id = mediaPlayer.getAudioSessionId();
                if (audio_ses_id != -1)
                {
                    barVisualizer.setAudioSessionId(audio_ses_id);
                }
            }
        });

        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+20000);
                }
            }
        });

        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-20000);
                }
            }
        });
    }

    public void start_animation(View v){
        ObjectAnimator animator= ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet= new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }

    public String crete_time(int duration)
    {
        String time = "";
        int mini = duration/1000/60;
        int sec = duration/1000%60;

        time+=mini+ ":";

        if (sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }
}