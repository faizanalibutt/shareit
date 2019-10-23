package com.hazelmobile.filetransfer.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.library.RippleBackground;

public class ReceiverActivity extends BaseActivity
        implements SnackbarSupport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        final ImageView back =findViewById(R.id.receiver_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final RippleBackground pulse = findViewById(R.id.content);
        pulse.startRippleAnimation();
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return null;
    }
}
