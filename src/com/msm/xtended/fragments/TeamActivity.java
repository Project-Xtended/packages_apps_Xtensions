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

        setTeamMember("Santosh Jha", getString(R.string.developer_title) + "/" + getString(R.string.maintainer_title), "SuperDroidBond",  R.drawable.bond);
        setTeamMember("Mukesh Singh", getString(R.string.developer_title) + "/" + getString(R.string.maintainer_title), "mukesh22584",  R.drawable.mukesh);
        setTeamMember("MadhuSudhan Sir", getString(R.string.developer_title), "mady51",  R.drawable.mady);
        setTeamMember("Anish Pratheepan", getString(R.string.developer_title) + "/" + getString(R.string.maintainer_title), "ultranoob-5",  R.drawable.anish);
        setTeamMember("xawlw", getString(R.string.developer_title) + "/" + getString(R.string.maintainer_title), "xawlw",  R.drawable.xawlw);
        setTeamMember("Ashwatthama", getString(R.string.developer_title) + "/" + getString(R.string.maintainer_title), "sai4041412",  R.drawable.ashw);
        setTeamMember("Dhanush Krishanan", getString(R.string.developer_title), "Destiny911gtr",  R.drawable.ded);
        setTeamMember("Danny", getString(R.string.developer_title), "mcdachpappe",  R.drawable.danny);
        setTeamMember("Honza", getString(R.string.designer_title), "jansvanda",  R.drawable.honza);
        setTeamMember("Roger Truttmann", getString(R.string.designer_title), "ROGERdotT",  R.drawable.roger);

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
