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

public class ThanksActivity extends Activity {

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

        setTeamMember("LineageOS", getString(R.string.team_1), "LineageOS",  R.drawable.los);
        setTeamMember("GZOSP", getString(R.string.team_2), "GZOSP",  R.drawable.gzosp);
        setTeamMember("Colt OS", getString(R.string.team_3), "Colt-Enigma",  R.drawable.colt);
        setTeamMember("Omni ROM", getString(R.string.team_4), "OmniROM",  R.drawable.omni);
        setTeamMember("Dirty Unicorns", getString(R.string.team_5), "DirtyUnicorns",  R.drawable.du);
        setTeamMember("Nitrogen OS", getString(R.string.team_6), "Nitrogen-Project",  R.drawable.nitrogen);
        setTeamMember("AICP", getString(R.string.team_7), "aicp",  R.drawable.aicp);
        setTeamMember("CrDroid Android", getString(R.string.team_8), "CrDroidAndroid",  R.drawable.crdroid);
        setTeamMember("Screwd AOSP", getString(R.string.team_9), "ScrewdAOSP",  R.drawable.screwd);
        setTeamMember("Cypher OS", getString(R.string.team_10), "CypherOS",  R.drawable.cypher);
        setTeamMember("Liquid Remix", getString(R.string.team_11), "LiquidRemix",  R.drawable.lr);
        setTeamMember("Benzo ROM", getString(R.string.team_12), "BenzoROM",  R.drawable.benzo);
        setTeamMember("BootLeggers Rom", getString(R.string.team_13), "BootLeggersROM",  R.drawable.blr);
        setTeamMember("Aosp Extended", getString(R.string.team_14), "AospExtended",  R.drawable.aex);
        setTeamMember("Resurrection Remix", getString(R.string.team_15), "ResurrectionRemix",  R.drawable.rr);
        setTeamMember("Havoc OS", getString(R.string.team_16), "Havoc-OS",  R.drawable.havoc);
        setTeamMember("Substratum Theme", getString(R.string.team_17), "Substratum",  R.drawable.subs);

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

