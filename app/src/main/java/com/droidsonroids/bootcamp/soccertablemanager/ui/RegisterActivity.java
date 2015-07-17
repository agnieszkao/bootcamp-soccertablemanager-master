package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterResponseEvent;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RegisterActivity extends AppCompatActivity {

    @Bind(R.id.username_edit_text)
    EditText mUsernameEditText;
    @Bind(R.id.time_edit_text)
    EditText mTimeEditText;
    @Bind(R.id.table_list_view)
    ListView mTableListView;
    private int mUserId;
    private SharedPreferences mSharedPreferences;
    private TableListAdapter mAdapter;
    private ArrayList<Table> mTableList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        setDefaultSettings();
        getTables();
        onItemClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences.getInt(Constants.KEY_ID, 0);
    }

    private void setDefaultSettings() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserId = mSharedPreferences.getInt(Constants.KEY_ID, 0);
        mUsernameEditText.setText(mSharedPreferences.getString(Constants.KEY_USERNAME, ""));
        mUsernameEditText.setSelection(mUsernameEditText.getText().length());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.register_button)
    public void onClickRegisterButton() {
        registerUser();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.create_table_button)
    public void onClickCreateTableButton() {
        createTable();
        EventBus.getDefault().post(new GetTablesRequestEvent());
    }

    private void registerUser() {
        String username = mUsernameEditText.getText().toString();
        EventBus.getDefault().post(new RegisterRequestEvent(username));
    }

    private void createTable() {
        String time = mTimeEditText.getText().toString();
        mUserId = mSharedPreferences.getInt(Constants.KEY_ID, -1);
        EventBus.getDefault().post(new CreateTableRequestEvent(time, mUserId));
    }

    private void getTables() {
        EventBus.getDefault().post(new GetTablesRequestEvent());
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

    @SuppressWarnings("unused")
    public void onEventMainThread(RegisterResponseEvent registerResponseEvent) {
        if (registerResponseEvent.getApiError() == null) {
            mUserId = registerResponseEvent.getUserId();
            mSharedPreferences.edit().putInt(Constants.KEY_ID, mUserId).putString(Constants.KEY_USERNAME, mUsernameEditText.getText().toString()).apply();
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
        } else {
            Toast.makeText(getApplicationContext(), tablesResponseEvent.getApiError().getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(JoinResponseEvent joinResponseEvent) {
        if (joinResponseEvent.getApiError() == null) {
            EventBus.getDefault().post(new GetTablesRequestEvent());
        } else {
            Toast.makeText(getApplicationContext(), joinResponseEvent.getApiError().getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
