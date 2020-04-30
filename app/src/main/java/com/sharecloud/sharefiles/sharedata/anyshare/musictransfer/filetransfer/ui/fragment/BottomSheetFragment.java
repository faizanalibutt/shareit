package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetBehavior standardBottomSheetBehavior;
    private TextView sheetText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout standardBottomSheet = view.findViewById(R.id.standardBottomSheet);
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet);

        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        sheetText = view.findViewById(R.id.sheetText);
        sheetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    sheetText.setText("Close sheet");
                } else {
                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    sheetText.setText("Expand sheet");
                }
            }
        });
    }

}
