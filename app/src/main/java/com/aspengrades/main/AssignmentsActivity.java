package com.aspengrades.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspengrades.data.AspenTaskStatus;
import com.aspengrades.data.ClassInfo;
import com.aspengrades.data.ClassInfoListener;
import com.aspengrades.data.Cookies;

import static com.aspengrades.data.AspenTaskStatus.ASPEN_UNAVAILABLE;
import static com.aspengrades.data.AspenTaskStatus.PARSING_ERROR;
import static com.aspengrades.data.AspenTaskStatus.SESSION_EXPIRED;

public class AssignmentsActivity extends AppCompatActivity implements ClassInfoListener {

    @Override
    public void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.activity_assignments);
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(intent.getStringExtra(getString(R.string.extra_class_description)));
        String id = intent.getStringExtra(getString(R.string.extra_class_id));
        String token = intent.getStringExtra(getString(R.string.extra_token));
        int term = intent.getIntExtra(getString(R.string.extra_term), 0);
        String[] keys = intent.getStringArrayExtra(getString(R.string.extra_cookie_keys));
        String[] values = intent.getStringArrayExtra(getString(R.string.extra_cookie_values));
        Cookies cookies = Cookies.from(keys, values);
        ClassInfo.readClassInfo(this, term, id, token, cookies);
    }

    @Override
    public void onClassInfoRead(ClassInfo classInfo) {
        ProgressBar progressBar = findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.GONE);
        AspenTaskStatus status  = classInfo.getStatus();

        if(status == SESSION_EXPIRED)
            AlertUtil.showSessionExpiredAlert(this);
        else if(status == ASPEN_UNAVAILABLE)
            showErrorMessage(getString(R.string.text_network_error));
        else if(status == PARSING_ERROR)
            showErrorMessage(getString(R.string.text_parsing_error));
        else setupRecyclerView(classInfo);
    }

    private void setupRecyclerView(ClassInfo classInfo){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CategoryAdapter adapter = new CategoryAdapter(classInfo, this);
        recyclerView.setAdapter(adapter);
    }

    private void showErrorMessage(String text){
        TextView textError = findViewById(R.id.text_error);
        textError.setText(text);
        textError.setVisibility(View.VISIBLE);
    }
}