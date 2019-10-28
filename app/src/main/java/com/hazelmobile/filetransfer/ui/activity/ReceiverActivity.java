package com.hazelmobile.filetransfer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.ui.fragment.HotspotManagerFragment;

import static com.hazelmobile.filetransfer.ui.activity.PermissionsActivity.EXTRA_CLOSE_PERMISSION_SCREEN;

public class ReceiverActivity extends Activity
        implements SnackbarSupport {

    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String RECEIVE = "receive";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    private RequestType mRequestType = RequestType.RETURN_RESULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        if (getIntent() != null) {
            if (getIntent().hasExtra(ConnectionManagerActivityDemo.RECEIVE) &&
                    getIntent().getBooleanExtra(ConnectionManagerActivityDemo.RECEIVE, false)) {
                getSupportFragmentManager().beginTransaction().add
                        (R.id.activity_connection_establishing_content_view, new HotspotManagerFragment()).commit();
            }
        }

        final ImageView back = findViewById(R.id.receiver_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_CLOSE_PERMISSION_SCREEN, true));
        finish();
        super.onBackPressed();
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return null;
    }

    public enum RequestType {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }
}
