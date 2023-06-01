package com.example.a09_bt;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a09_bt.databinding.ActivitySoundBinding;
import com.example.a09_bt.databinding.FragmentBluetoothBinding;
import com.example.a09_bt.databinding.FragmentMovementBinding;

public class MovementFragment extends Fragment {

    private EV3Service mref_ev3;

    private FragmentMovementBinding binding;

    public MovementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMovementBinding.inflate(inflater, container, false);
        mref_ev3 = (EV3Service) getArguments().getSerializable("ev3");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mref_ev3.mf_EV3SendNoReplyCmd("StartMotor", (byte) 50, (byte) 0x06);
                    mref_ev3.mf_EV3SendReplyCmd("detectWallProgram");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    binding.vvTvOut1.setText(mref_ev3.mf_EV3SendReplyCmd("GetBattery") + "%");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    };


}