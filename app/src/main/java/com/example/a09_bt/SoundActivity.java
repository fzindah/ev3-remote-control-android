package com.example.a09_bt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager2.widget.ViewPager2;

import com.example.a09_bt.databinding.ActivitySoundBinding;

import java.io.Serializable;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener {

    private EV3Service mref_ev3;

    private AppBarConfiguration appBarConfiguration;
    private ActivitySoundBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySoundBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Making "piano keys" white
        binding.button1.setBackgroundColor(Color.WHITE);
        binding.button2.setBackgroundColor(Color.WHITE);
        binding.button3.setBackgroundColor(Color.WHITE);
        binding.button4.setBackgroundColor(Color.WHITE);
        binding.button5.setBackgroundColor(Color.WHITE);
        binding.button6.setBackgroundColor(Color.WHITE);
        binding.button7.setBackgroundColor(Color.WHITE);

        // playJingle button
        binding.playJingle.setBackgroundColor(Color.LTGRAY);

        // set button listeners
        binding.button1.setOnClickListener(this);
        binding.button2.setOnClickListener(this);
        binding.button3.setOnClickListener(this);
        binding.button4.setOnClickListener(this);
        binding.button5.setOnClickListener(this);
        binding.button6.setOnClickListener(this);
        binding.button7.setOnClickListener(this);
        binding.playJingle.setOnClickListener(this);
        binding.sayHello.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String letter = "";

        try {
            switch (v.getId()) {
                case R.id.button1: letter = "C"; break;
                case R.id.button2: letter = "D"; break;
                case R.id.button3: letter = "E"; break;
                case R.id.button4: letter = "F"; break;
                case R.id.button5: letter = "G"; break;
                case R.id.button6: letter = "A"; break;
                case R.id.button7: letter = "B"; break;
                case R.id.playJingle:
                    mref_ev3.mf_EV3SendNoReplyCmd("PlayJingle");
                    break;
                case R.id.sayHello:
                    mref_ev3.mf_EV3SendNoReplyCmd("SayHello");
            }

            if (letter != "")
                mref_ev3.mf_EV3SendNoReplyCmd("PlayTone", letter, 600, false);
        }
        catch (Exception e) {}
    }
}