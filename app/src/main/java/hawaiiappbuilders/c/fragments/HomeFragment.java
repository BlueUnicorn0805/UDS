/*
 * Copyright 2018 ShineStar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hawaiiappbuilders.c.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hawaiiappbuilders.c.ActivityEditProfile;
import hawaiiappbuilders.c.ActivityShowQR;
import hawaiiappbuilders.c.ActivityTransIntro;
import hawaiiappbuilders.c.MainActivity;
import hawaiiappbuilders.c.R;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    public static HomeFragment newInstance(String text) {
        HomeFragment mFragment = new HomeFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        init(getArguments());

        rootView.findViewById(R.id.btnNewDel).setOnClickListener(this);
        rootView.findViewById(R.id.btnZinta).setOnClickListener(this);
        rootView.findViewById(R.id.btnShowQRAsID).setOnClickListener(this);
        rootView.findViewById(R.id.btnMyProfile).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnNewDel) {
            ((MainActivity)parentActivity).showFragment(MainActivity.FRAGMENT_DRIVER_DELIVERY_MAP);
        } else if(viewId == R.id.btnZinta) {
            startActivity(new Intent(mContext, ActivityTransIntro.class));
        } else if(viewId == R.id.btnShowQRAsID) {
            startActivity(new Intent(mContext, ActivityShowQR.class));
        } else if(viewId == R.id.btnMyProfile) {
            //parentActivity.showFragment(MainActivity.FRAGMENT_SENDER_DELIVERY_MAP);
            //startActivity(new Intent(mContext, ProfileActivity.class));
            startActivity(new Intent(mContext, ActivityEditProfile.class));

            //startActivity(new Intent(mContext, TransparentActivity.class));
        }
    }
}
