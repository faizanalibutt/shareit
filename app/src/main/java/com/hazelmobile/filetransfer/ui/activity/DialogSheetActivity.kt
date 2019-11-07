package com.hazelmobile.filetransfer.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hazelmobile.filetransfer.R
import kotlinx.android.synthetic.main.activity_dialog_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*


class DialogSheetActivity : AppCompatActivity() {

    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_sheet)

        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        val bottomSheetCallbackList = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            @SuppressLint("SetTextI18n")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        sheetText.text = "Hidden Sheet"
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        sheetText.text = "Close Sheet"
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        sheetText.text = "Expand Sheet"
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        sheetText.text = "Dragging Sheet"
                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                        sheetText.text = "Settling Sheet"
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        sheetText.text = "Half Expand Sheet"
                    }

                }
            }

        }

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallbackList)

        sheetText.setOnClickListener {
            if (standardBottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheetText.text = "Close sheet"
            } else {
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                sheetText.text = "Expand sheet"
            }
        }

    }

}
