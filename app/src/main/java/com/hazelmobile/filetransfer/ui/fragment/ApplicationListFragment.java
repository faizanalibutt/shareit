package com.hazelmobile.filetransfer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.SelectionCallbackGlobal;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.pictures.EditableListAdapter;
import com.hazelmobile.filetransfer.pictures.EditableListFragment;
import com.hazelmobile.filetransfer.ui.adapter.ApplicationListAdapter;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/*import com.hazelmobile.filetransfer.model.PackageHolder;*/

public class ApplicationListFragment
        extends EditableListFragment<ApplicationListAdapter.PackageHolder, EditableListAdapter.EditableViewHolder, ApplicationListAdapter>
        implements TitleSupport {

    private TextView appsSize;
    private CheckBox selectAll;

    public CheckBox getChckBox() {
        return selectAll;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilteringSupported(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultViewingGridSize(4, 8);
        setUseDefaultPaddingDecoration(false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyImage(R.drawable.ic_android_head_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyApp));
        appsSize = view.findViewById(R.id.myAppsText);
        selectAll = view.findViewById(R.id.selectAll);

        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new SelectionCallback<>(isChecked, ApplicationListFragment.this);
                if (isChecked) {
                    SelectionCallbackGlobal.setColor(true);
                }
            }
        });


        final Observer<Boolean> selectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean select) {
                if (select != null && !select) {
                    selectAll.setChecked(false);
                }
            }
        };
        SelectionCallbackGlobal.getColor().observe(ApplicationListFragment.this, selectObserver);
    }

    @Override
    public boolean onSetListAdapter(ApplicationListAdapter adapter) {
        if (super.onSetListAdapter(adapter)) {
            appsSize.setText(getApplicationListSize(adapter));
            return true;
        } else
            return false;
    }

    private String getApplicationListSize(ApplicationListAdapter adapter) {
        return "My Apps ( " + adapter.onLoad().size() + " )";
    }

    @Override
    public ApplicationListAdapter onAdapter() {
        final AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> quickActions = new AppUtils.QuickActions<EditableListAdapter.EditableViewHolder>() {
            @Override
            public void onQuickActions(final EditableListAdapter.EditableViewHolder clazz) {
                registerLayoutViewClicks(clazz);

                clazz.getView().findViewById(R.id.selector).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getSelectionConnection() != null) {
                                    SelectionCallbackGlobal.setColor(true);
                                    getSelectionConnection().setSelected(clazz.getAdapterPosition());
                                }
                            }
                        });
            }
        };

        return new ApplicationListAdapter(getActivity(), AppUtils.getDefaultPreferences(getContext())) {
            @NonNull
            @Override
            public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        };
    }

    @Override
    public boolean onDefaultClickAction(EditableListAdapter.EditableViewHolder holder) {
        if (getSelectionConnection() != null) {
            SelectionCallbackGlobal.setColor(true);
            return getSelectionConnection().setSelected(holder);
        } else {
            SelectionCallbackGlobal.setColor(false);
            return performLayoutClickOpen(holder);
        }
    }


    @Override
    protected RecyclerView onListView(View mainContainer, ViewGroup listViewContainer) {

        View adaptedView = getLayoutInflater().inflate(R.layout.fragment_application_list, null, false);
        listViewContainer.addView(adaptedView);

        return super.onListView(mainContainer, (ViewGroup) adaptedView.findViewById(R.id.appsList));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_application, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_system_apps) {
            boolean isShowingSystem = !AppUtils.getDefaultPreferences(getContext()).getBoolean("show_system_apps", false);

            AppUtils.getDefaultPreferences(getContext()).edit()
                    .putBoolean("show_system_apps", isShowingSystem)
                    .apply();

            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuSystemApps = menu.findItem(R.id.show_system_apps);
        menuSystemApps.setChecked(AppUtils.getDefaultPreferences(getContext()).getBoolean("show_system_apps", false));
    }

    @NotNull
    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_application);
    }

    @Override
    public boolean performLayoutClickOpen(EditableListAdapter.EditableViewHolder holder) {
        try {
            final ApplicationListAdapter.PackageHolder appInfo = getAdapter().getItem(holder);
            final Intent launchIntent = Objects.requireNonNull(getActivity()).getPackageManager().getLaunchIntentForPackage(appInfo.friendlyName);

            if (launchIntent != null) {
                /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                dialogBuilder.setMessage(R.string.ques_launchApplication);
                dialogBuilder.setNegativeButton(R.string.butn_cancel, null);
                dialogBuilder.setPositiveButton(R.string.butn_appLaunch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(launchIntent);
                    }
                });

                dialogBuilder.show();*/
            } else
                //Toast.makeText(getActivity(), R.string.mesg_launchApplicationError, Toast.LENGTH_SHORT).show();

                return true;
        } catch (Exception e) {
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SelectionCallbackGlobal.setColor(false);
    }
}
