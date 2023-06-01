package com.example.a09_bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.a09_bt.databinding.FragmentBluetoothBinding;

public class BluetoothFragment extends Fragment {

    private FragmentBluetoothBinding binding;
    private EV3Service mref_ev3;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        mref_ev3 = (EV3Service) getArguments().getSerializable("ev3");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Need grant permission once per install
        Pair<String, String> p = mref_ev3.mf_checkBTPermissions((MainActivity) requireActivity());
        binding.vvTvOut1.setText(p.first);
        ListView listview = (ListView) binding.bluetoothList;

        binding.bluetoothList.setVisibility(View.GONE);

        binding.bluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFromList = (String) (adapterView.getItemAtPosition(i));
                binding.vvTvOut2.setText(mref_ev3.mf_connectToEV3(selectedFromList));
            }
        });

        binding.vvButtonRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mref_ev3.mf_requestBTPermissions((MainActivity) requireActivity());
            }
        });

        binding.vvButtonLocatePaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.bluetoothList.setVisibility(View.VISIBLE);
                binding.vvTvOut2.setText(mref_ev3.mf_locateInPairedBTList(binding));
            }
        });

        binding.vvButtonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.vvTvOut2.setText(mref_ev3.mf_disconnFromEV3());
            }
        });
    }
}
