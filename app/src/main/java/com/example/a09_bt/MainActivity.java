package com.example.a09_bt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import com.example.a09_bt.databinding.*;

import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mPager;
    private ViewPager2Adapter viewPager2Adapter;
    private List<Fragment> fragmentList;
    private ActivityMainBinding binding;
    private EV3Service mref_ev3 = new EV3Service();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = new Bundle();
        bundle.putSerializable("ev3", (Serializable) mref_ev3);

        mPager = binding.viewPager;
        fragmentList = new ArrayList<>();

        fragmentList.add(new BluetoothFragment());

        fragmentList.add(new MainFragment());

        fragmentList.add(new SoundFragment());

        fragmentList.add(new MovementFragment());

        for (Fragment f : fragmentList)
            f.setArguments(bundle);

        viewPager2Adapter = new ViewPager2Adapter(this, fragmentList);
        mPager.setAdapter(viewPager2Adapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(broadcastReceiver, filter);
    }
    // bluetooth receiver
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        BluetoothDevice device;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is disconnected",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}