package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.fragments.BaseFragment;
import hawaiiappbuilders.c.fragments.BizHandleFragment;
import hawaiiappbuilders.c.fragments.CheckUsernameFragment;
import hawaiiappbuilders.c.fragments.ChooseLocationFragment;
import hawaiiappbuilders.c.fragments.CompanyInfoFragment;
import hawaiiappbuilders.c.fragments.GetPublishedNowFragment;
import hawaiiappbuilders.c.fragments.IntroduceFragment;
import hawaiiappbuilders.c.fragments.MessageAtTopFragment;
import hawaiiappbuilders.c.fragments.OwnerInfoFragment;

public class RegisterServiceActivity extends FragmentFolderActivity implements
        View.OnClickListener {

    BaseFragment currentFragment;
    public static final int FRAGMENT_P1 = 1;
    public static final int FRAGMENT_P2 = 2;
    public static final int FRAGMENT_P3 = 3;
    public static final int FRAGMENT_P4 = 4;
    public static final int FRAGMENT_P5 = 5;
    public static final int FRAGMENT_P6 = 6;
    public static final int FRAGMENT_P7 = 7;
    public static final int FRAGMENT_P8 = 8;

    CheckUsernameFragment checkUsernameFragment;
    OwnerInfoFragment ownerInfoFragment;
    BizHandleFragment bizHandleFragment;
    CompanyInfoFragment companyInfoFragment;
    MessageAtTopFragment messageAtTopFragment;
    ChooseLocationFragment chooseLocationFragment;
    IntroduceFragment introduceFragment;
    GetPublishedNowFragment getPublishedNowFragment;

    private int currentFragmentID = -1;

    View panelNavigation;
    View tabBack;
    View tabNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragmentfolder);


        // Get Data
        Intent intent = getIntent();

        // Init UI Elements
        panelNavigation = findViewById(R.id.panelNavigation);
        tabBack = findViewById(R.id.tabBack);
        tabNext = findViewById(R.id.tabNext);

        checkUsernameFragment = CheckUsernameFragment.newInstance("P1");
        ownerInfoFragment = OwnerInfoFragment.newInstance("P2");
        bizHandleFragment = BizHandleFragment.newInstance("P3");
        companyInfoFragment = CompanyInfoFragment.newInstance("P4");
        messageAtTopFragment = MessageAtTopFragment.newInstance("P5");
        chooseLocationFragment = ChooseLocationFragment.newInstance("P6");
        introduceFragment = IntroduceFragment.newInstance("P7");
        getPublishedNowFragment = GetPublishedNowFragment.newInstance("P8");

        tabBack.setOnClickListener(this);
        tabNext.setOnClickListener(this);

        findViewById(R.id.btnHelp).setOnClickListener(this);

        // Set Init values
        saveValue("firstname", appSettings.getFN());
        saveValue("lastname", appSettings.getLN());
        saveValue("email", appSettings.getEmail());
        saveValue("phone", String.format("%s", appSettings.getCP()));
        saveValue("city", appSettings.getCity());
        saveValue("state", appSettings.getSt());
        saveValue("zip", appSettings.getZip());
        saveValue("street_address", appSettings.getStreet());

        // Show first fragment
        showFragment(FRAGMENT_P1, true);
        //getCameraImage();

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tabBack) {
            showPrevFragment();
        } else if (viewId == R.id.tabNext) {

            if (currentFragment != null) {
                // Check current Data inputs
                if (currentFragment.isAllValidField()) {
                    currentFragment.saveFields();

                    if (currentFragment instanceof OwnerInfoFragment) {
                        ((OwnerInfoFragment) currentFragment).checkPINCode();
                    } /*else if (currentFragment instanceof CompanyInfoFragment) {
                        ((CompanyInfoFragment) currentFragment).inputSecurityCode();
                    } */ else {
                        showNextFragment();
                    }
                }
            } else {
                showNextFragment();
            }
        } else if (viewId == R.id.btnHelp) {
            showAlert("This is help descriptions.");
        }
    }

    public void showPrevFragment() {
        showFragment(currentFragmentID - 1, false);
    }

    public void showNextFragment() {
        showFragment(currentFragmentID + 1, true);
    }

    public void showFragment(int fragmentID, boolean progressive) {

        // Create fragment and give it an argument specifying the article it should show
        BaseFragment newFragment = null;
        if (fragmentID == FRAGMENT_P1) {
            if (currentFragment != null && currentFragment instanceof CheckUsernameFragment) {
                return;
            }
            newFragment = checkUsernameFragment;
        } else if (fragmentID == FRAGMENT_P2) {
            if (currentFragment != null && currentFragment instanceof OwnerInfoFragment) {
                return;
            }
            newFragment = ownerInfoFragment;
        } else if (fragmentID == FRAGMENT_P3) {
            if (currentFragment != null && currentFragment instanceof BizHandleFragment) {
                return;
            }
            newFragment = bizHandleFragment;
        } else if (fragmentID == FRAGMENT_P4) {
            if (currentFragment != null && currentFragment instanceof CompanyInfoFragment) {
                return;
            }
            newFragment = companyInfoFragment;
        } else if (fragmentID == FRAGMENT_P5) {
            if (currentFragment != null && currentFragment instanceof MessageAtTopFragment) {
                return;
            }
            newFragment = messageAtTopFragment;
        } else if (fragmentID == FRAGMENT_P6) {
            if (currentFragment != null && currentFragment instanceof ChooseLocationFragment) {
                return;
            }
            newFragment = chooseLocationFragment;
        } else if (fragmentID == FRAGMENT_P7) {
            if (currentFragment != null && currentFragment instanceof IntroduceFragment) {
                return;
            }
            newFragment = introduceFragment;
        } else if (fragmentID == FRAGMENT_P8) {
            if (currentFragment != null && currentFragment instanceof GetPublishedNowFragment) {
                return;
            }
            newFragment = getPublishedNowFragment;
        }

        if (newFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (progressive) {
                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                transaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out);
            }

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            //transaction.commit();
            transaction.commitAllowingStateLoss();
            currentFragment = newFragment;
            currentFragmentID = fragmentID;

            if (currentFragmentID == FRAGMENT_P1) {
                panelNavigation.setVisibility(View.GONE);
            } else if (currentFragmentID == FRAGMENT_P3) {
                panelNavigation.setVisibility(View.GONE);
            } else if (currentFragmentID >= FRAGMENT_P8) {
                panelNavigation.setVisibility(View.GONE);
            } else if (currentFragment instanceof GetPublishedNowFragment) {
                panelNavigation.setVisibility(View.VISIBLE);
                tabNext.setVisibility(View.INVISIBLE);
            } else {
                panelNavigation.setVisibility(View.VISIBLE);
                tabNext.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "OnActivityResult" + resultCode + requestCode);

        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (currentFragment != null) {
            currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
