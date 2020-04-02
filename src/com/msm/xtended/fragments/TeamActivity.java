/*
 * Copyright (C) 2020 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.msm.xtended.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends Activity {

    private List<DevInfoAdapter> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_recyclerview);

        initTeam();

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }
    private void initTeam(){
        RecyclerView mRecycleview = findViewById(R.id.listView);

        setTeamMember("SuperDroidBond", getString(R.string.developer_title), "SuperDroidBond",  R.drawable.bond);

        ListAdapter mAdapter = new ListAdapter(mList);
        mRecycleview.setAdapter(mAdapter);
        mRecycleview.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.notifyDataSetChanged();
    }

    private void setTeamMember(String devName, String devTitle,
                               String githubLink, int devImage) {
        DevInfoAdapter adapter;

        adapter = new DevInfoAdapter();
        adapter.setImage(devImage);
        adapter.setDevName(devName);
        adapter.setDevTitle(devTitle);
        adapter.setGithubName(githubLink);
        mList.add(adapter);
    }
}
