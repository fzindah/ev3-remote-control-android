package com.example.a09_bt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.example.a09_bt.databinding.FragmentMainBinding;

import java.util.Arrays;


public class MainFragment extends Fragment implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    private FragmentMainBinding binding;
    private EV3Service mref_ev3;

    // Motors
    private byte motorBC = 0x06;
    private byte motorA = 0x01;
    // Default Seekbar
    private int drivePower = 50;
    private int armPower = 50;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        mref_ev3 = (EV3Service) getArguments().getSerializable("ev3");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Need grant permission once per install
        Pair<String, String> p = mref_ev3.mf_checkBTPermissions((MainActivity) requireActivity());

        // Seekbar default values
        binding.vvDrivePowerSeekbar.setProgress(drivePower);
        binding.vvArmPowerSeekbar.setProgress(armPower);

        binding.firstMotorTextView.setText(""+ drivePower); //1st motor
        binding.secondMotorTextView.setText(""+ armPower); //2nd motor

        // button listeners
        binding.vvButtonUp.setOnTouchListener(this);
        binding.vvButtonDown.setOnTouchListener(this);
        binding.vvButtonLeft.setOnTouchListener(this);
        binding.vvButtonRight.setOnTouchListener(this);
        binding.vvButtonArmUp.setOnTouchListener(this);
        binding.vvButtonArmDown.setOnTouchListener(this);

        // seekbars
        binding.vvArmPowerSeekbar.setOnSeekBarChangeListener(this);
        binding.vvDrivePowerSeekbar.setOnSeekBarChangeListener(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private boolean buttonAction
            (ImageButton button, MotionEvent event, byte motors, int power, String direction){
        try {
            Log.i("EV3Data", "Motors: " + motors + " Power: " + power);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Pressed
                button.setPressed(true);
                if (direction.equals("right") || direction.equals("left"))
                    mref_ev3.mf_EV3SendNoReplyCmd("Turn", motors, (byte) power);
                else
                    mref_ev3.mf_EV3SendNoReplyCmd("StartMotor", (byte) power, motors);

                setButtonUI(button, direction);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Released
                button.setPressed(false);
                setButtonUI(button, direction + "2");

                if (direction.equals("right") || direction.equals("left"))
                    motors = motorBC;

                mref_ev3.mf_EV3SendNoReplyCmd("StopMotor", motors, (byte) 0);
            }
        }
        catch (Exception e) {
        }
        return true;
    }

    private void setButtonUI(ImageButton button, String direction) {
        int[] images = {R.drawable.u_pressed, R.drawable.down_pressed, R.drawable.r_pressed, R.drawable.l_pressed,
                R.drawable.u, R.drawable.d, R.drawable.r, R.drawable.l, R.drawable.u_pressed,
                R.drawable.down_pressed, R.drawable.arm_up, R.drawable.arm_down};
        String[] dirs = {"up", "down", "right", "left", "up2", "down2", "right2",
                "left2", "arm_up", "arm_down", "arm_up2", "arm_down2"};

        button.setImageResource(images[Arrays.asList(dirs).indexOf(direction)]);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageButton v1 = (ImageButton) v;
        try {
            switch (v.getId()) {
                case R.id.vv_buttonUp:
                    buttonAction(v1, event, motorBC, drivePower, "up");
                    break;
                case R.id.vv_buttonDown:
                    buttonAction(v1, event, motorBC,drivePower * -1, "down");
                    break;
                case R.id.vv_buttonLeft:
                    buttonAction(v1, event, (byte) drivePower, 0xCE, "left");
                    break;
                case R.id.vv_buttonRight:
                    buttonAction(v1, event, (byte)drivePower, 0x32, "right");
                    break;
                case R.id.vv_buttonArmUp:
                    buttonAction(v1, event, motorA, armPower, "arm_up");
                    break;
                case R.id.vv_buttonArmDown:
                    buttonAction(v1, event, motorA,armPower * -1, "arm_down");
                    break;
                default:break;
            }
            return true;
        }
        catch (Exception e) {return false;}
    }

    @SuppressLint("ResourceType")
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar.getId() == R.id.vv_arm_power_seekbar) {
            armPower = seekBar.getProgress();
            binding.secondMotorTextView.setText(""+ armPower);
        }
        else if (seekBar.getId() == R.id.vv_drive_power_seekbar) {
            drivePower = seekBar.getProgress();
            binding.firstMotorTextView.setText("" + drivePower);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}