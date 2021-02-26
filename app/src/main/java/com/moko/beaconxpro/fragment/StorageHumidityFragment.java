package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.activity.THDataActivity;
import com.moko.beaconxpro.dialog.BottomDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageHumidityFragment extends Fragment {

    private static final String TAG = "StorageHumidityFragment";
    @BindView(R.id.tv_storage_humidity_only)
    TextView tvStorageHumidityOnly;
    @BindView(R.id.tv_humidity_only_tips)
    TextView tvHumidityOnlyTips;
    private ArrayList<String> mDatas;

    private THDataActivity activity;


    public StorageHumidityFragment() {
    }

    public static StorageHumidityFragment newInstance() {
        StorageHumidityFragment fragment = new StorageHumidityFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_storage_humidity, container, false);
        ButterKnife.bind(this, view);
        activity = (THDataActivity) getActivity();
        mDatas = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            mDatas.add(i + "");
        }
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick(R.id.tv_storage_humidity_only)
    public void onViewClicked() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mDatas, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            if (mSelected == 0) {
                tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
            } else {
                tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, value));
            }
            tvStorageHumidityOnly.setText(String.valueOf(value));
            activity.setSelectedHumidity(value);
        });
    }

    private int mSelected;

    public void setHumidityData(int data) {
        mSelected = data / 10;
        if (mSelected == 0) {
            tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
        } else {
            tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, mSelected));
        }
        tvStorageHumidityOnly.setText(mSelected + "");
    }
}
