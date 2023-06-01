package com.example.a09_bt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a09_bt.databinding.ActivitySoundBinding;

public class SoundFragment extends Fragment implements View.OnClickListener {

    private EV3Service mref_ev3;

    private ActivitySoundBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySoundBinding.inflate(inflater, container, false);
        mref_ev3 = (EV3Service) getArguments().getSerializable("ev3");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}