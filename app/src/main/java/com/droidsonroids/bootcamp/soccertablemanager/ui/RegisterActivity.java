package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.api.model.Constants;
import com.droidsonroids.bootcamp.soccertablemanager.api.model.Table;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterResponseEvent;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RegisterActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.table_list_view)
    ListView mTableListView;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private int mUserId;
    public SharedPreferences mSharedPreferences;
    private TableListAdapter mAdapter;
    private ArrayList<Table> mTableList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setSwipeRefreshLayout();
        setDefaultSettings();
        getTables();
        onItemClick();
        onLongItemClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDefaultSettings();
        refreshListOfTables();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void setSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListOfTables();
            }
        });
    }

    private void refreshListOfTables() {
        EventBus.getDefault().post(new GetTablesRequestEvent());
    }

    private void setDefaultSettings() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserId = mSharedPreferences.getInt(Constants.KEY_ID, 0);
        mToolbar.setTitle(mSharedPreferences.getString(Constants.KEY_USERNAME, ""));
    }

    private void registerUser(String username) {
        mToolbar.setTitle(username);
        EventBus.getDefault().post(new RegisterRequestEvent(username));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.floating_action_button)
    public void onClickFloatingActionButton(View view) {
        showCreatingTableDialog();
    }

    private void showCreatingTableDialog() {
        final EditText timeEditText = new EditText(RegisterActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        timeEditText.setLayoutParams(lp);
        timeEditText.setInputType(InputType.TYPE_CLASS_DATETIME);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String time = timeEditText.getText().toString();
                        createTable(time);
                        refreshListOfTables();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.table)
                .setMessage(R.string.time)
                .setView(timeEditText)
                .setPositiveButton(R.string.create, dialogClickListener)
                .setNegativeButton(R.string.cancel, dialogClickListener).show();
    }

    public void createTable(String time) {
        mUserId = mSharedPreferences.getInt(Constants.KEY_ID, -1);
        if (mUserId != -1) {
            EventBus.getDefault().post(new CreateTableRequestEvent(time, mUserId));
        } else {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void getTables() {
        refreshListOfTables();
        mAdapter = new TableListAdapter(getApplicationContext(), mTableList);
        mTableListView.setAdapter(mAdapter);
    }

    private void onItemClick() {
        mTableListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                joinToTable(position);
            }
        });
    }

    private void joinToTable(int position) {
        Table table = mTableList.get(position);
        EventBus.getDefault().post(new JoinRequestEvent(table.getTableId(), mUserId));
    }

    private void onLongItemClick() {
        mTableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                leaveTheTable(position);
                return true;
            }
        });
    }

    private void leaveTheTable(int position) {
        Table table = mTableList.get(position);
        EventBus.getDefault().post(new LeaveRequestEvent(table.getTableId(), mUserId));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RegisterResponseEvent registerResponseEvent) {
        if (registerResponseEvent.getApiError() == null) {
            mUserId = registerResponseEvent.getUserId();
            mSharedPreferences.edit().putInt(Constants.KEY_ID, mUserId).putString(Constants.KEY_USERNAME, mToolbar.getTitle().toString()).apply();
            Toast.makeText(getApplicationContext(), "Your id is: " + registerResponseEvent.getUserId(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), registerResponseEvent.getApiError().getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CreateTableResponseEvent createTableResponseEvent) {
        if (createTableResponseEvent.getApiError() != null) {
            if (createTableResponseEvent.getApiError().getErrorCode() == "422") {
                Toast.makeText(getApplicationContext(), R.string.table_already_taken, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        } else {
            String tableNumberText = getString(R.string.table_number) + createTableResponseEvent.getTableId();
            Toast.makeText(getApplicationContext(), tableNumberText, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GetTablesResponseEvent tablesResponseEvent) {
        if (tablesResponseEvent.getApiError() == null) {
            mTableList.clear();
            mTableList.addAll(tablesResponseEvent.getTables());
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            Toast.makeText(getApplicationContext(), tablesResponseEvent.getApiError().getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(JoinResponseEvent joinResponseEvent) {
        if (joinResponseEvent.getApiError() == null) {
            refreshListOfTables();
        } else {
            Toast.makeText(getApplicationContext(), R.string.cannot_join, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LeaveResponseEvent leaveResponseEvent) {
        if (leaveResponseEvent.getApiError() == null) {
            refreshListOfTables();
        } else {
            Toast.makeText(getApplicationContext(), leaveResponseEvent.getApiError().getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRegistrationDialog() {
        final EditText usernameEditText = new EditText(RegisterActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        usernameEditText.setLayoutParams(lp);
        usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String username = usernameEditText.getText().toString();
                        registerUser(username);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.registration)
                .setMessage(R.string.username)
                .setView(usernameEditText)
                .setPositiveButton(R.string.register, dialogClickListener)
                .setNegativeButton(R.string.cancel, dialogClickListener).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_register) {
            showRegistrationDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
