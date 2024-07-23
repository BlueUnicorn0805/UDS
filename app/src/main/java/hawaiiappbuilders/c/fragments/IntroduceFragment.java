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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hawaiiappbuilders.c.FragmentFolderActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.RegisterServiceActivity;


public class IntroduceFragment extends BaseFragment implements View.OnClickListener {

    EditText eLink;

    public static IntroduceFragment newInstance(String text) {
        IntroduceFragment mFragment = new IntroduceFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_introduce, container, false);

        init(getArguments());

        eLink = rootView.findViewById(R.id.eLink);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateFields() {
        super.updateFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        // Parse Company Information
        fillEditTextWithValue(eLink, newSaleActivity.restoreValue("ulink"));
    }

    @Override
    public void saveFields() {
        super.saveFields();

//        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;
//        newSaleActivity.saveValue("ulink", eLink.getText().toString().trim());
    }

    @Override
    public boolean isAllValidField() {
//        String videoLink = eLink.getText().toString().trim();
        /*if (TextUtils.isEmpty(videoLink)) {
            showToastMessage("Please input valid ID");
            return false;
        } else if (videoLink.toLowerCase().startsWith("http")) {
            showToastMessage("Please input only Youtube ID, not full url");
            return false;
        } else {
            return true;
        }*/

        String videoLink = eLink.getText().toString().trim();
        if (!TextUtils.isEmpty(videoLink)) {
            if (videoLink.toLowerCase().startsWith("http")
                    || videoLink.toLowerCase().startsWith("www")
                    || videoLink.toLowerCase().startsWith("youtu")) {
                showToastMessage("Please input only Youtube ID, not full url");
                return false;
            }
            if (videoLink.length() > 11) {
                videoLink = videoLink.substring(videoLink.length() - 11);
            }
            FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;
            newSaleActivity.saveValue("ulink", videoLink);
            hideKeyboard(eLink);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnPublish) {
            RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;
            newSaleActivity.showNextFragment();
        }
    }
}
