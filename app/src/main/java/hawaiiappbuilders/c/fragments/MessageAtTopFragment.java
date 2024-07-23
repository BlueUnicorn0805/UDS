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

import hawaiiappbuilders.c.FragmentFolderActivity;
import hawaiiappbuilders.c.R;

public class MessageAtTopFragment extends BaseFragment implements View.OnClickListener {

    public static MessageAtTopFragment newInstance(String text) {
        MessageAtTopFragment mFragment = new MessageAtTopFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    EditText etComment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_message_attop, container, false);

        init(getArguments());

        etComment = rootView.findViewById(R.id.etComment);
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

        // Show saved Message
        fillEditTextWithValue(etComment, newSaleActivity.restoreValue("welcomemsg"));
    }

    @Override
    public void saveFields() {
        super.saveFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;
        newSaleActivity.saveValue("welcomemsg", etComment.getText().toString().trim());
    }

    @Override
    public boolean isAllValidField() {

        String custommsg = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(custommsg)) {
            showToastMessage("Please input company message.");
            return false;
        } else {
            hideKeyboard(etComment);
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        /*if (viewId == 0) {
            //parentActivity.showFragment(MainActivity.FRAGMENT_SENDER_NEW_DELIVERY);
            //startActivity(new Intent(mContext, NewSaleActivity.class));
        }*/
    }
}
