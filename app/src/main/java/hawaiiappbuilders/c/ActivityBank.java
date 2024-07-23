package hawaiiappbuilders.c;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.fragments.XTabEnterAcct;
import hawaiiappbuilders.c.fragments.XTabRequestFunds;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class ActivityBank extends BaseActivity {

    TabLayout tabLayout;
    ArrayList<String> tabTitles;
    ArrayList<Integer> tabIcons;

    ArrayList<Fragment> tabFragments;

    ViewPager pager;
    ScreenSlidePagerAdapter pagerAdapter;

    // 0= not seen anything and not setup
    // 1= not set account & routing
    // 2= needs to verify
    // 3= they are verified so stay on tab(1)
    public static final int TRANSFER_NOT_SEEN_SETUP = 0;
    public static final int TRANSFER_NOT_SETUP_ACCT = 1;
    public static final int TRANSFER_NEEDS_VERIFY = 20;
    public static final int TRANSFER_VERIFIED = 30;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Load your Account");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //initList();
        initViews();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        tabTitles = new ArrayList<>();
        tabIcons = new ArrayList<>();
        tabFragments = new ArrayList<>();

        tabTitles.add("     Request Funds    ");
        tabTitles.add("Enter Account for deposits");

        tabIcons.add(R.drawable.ic_nav_bank);
        tabIcons.add(R.drawable.ic_nav_localshop);

        tabFragments.add(new XTabRequestFunds());
        tabFragments.add(new XTabEnterAcct());

        pager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);

        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i) {
                if (i == 1) {
                    ((XTabEnterAcct)tabFragments.get(1)).getVerifyStatus(0, false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
        pager.setOffscreenPageLimit(pagerAdapter.getCount());
        tabLayout.setupWithViewPager(pager);
        setupTabIcons();

        if (tabLayout != null) {
            int tabIconColor = ContextCompat.getColor(mContext, R.color.white);
            int tabIconColorInactive = ContextCompat.getColor(mContext, /*R.color.bg_tab*/R.color.bg_tab_transparent);

            for (int i = 1; i < tabTitles.size(); i++) {
                tabLayout.getTabAt(i).getIcon().setColorFilter(tabIconColorInactive, PorterDuff.Mode.SRC_IN);
            }
            tabLayout.getTabAt(0).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        }

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(pager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(mContext, R.color.white);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//                        pager.setCurrentItem(tab.getPosition());
//                        TextView text = (TextView) tab.getCustomView();
//                        text.setTypeface(Utils.getBold(getActivity()));
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(mContext, /*R.color.bg_tab*/R.color.bg_tab_transparent);
                        //tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//                        pager.setCurrentItem(tab.getPosition());
//                        TextView text = (TextView) tab.getCustomView();
//                        text.setTypeface(Utils.getNormalFont(getActivity()));
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        changeTabsFont(tabLayout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAcctStatus();
            }
        }, 500);
    }

    private void checkAcctStatus() {
        if (appSettings.getTransMoneyStatus() == TRANSFER_NOT_SEEN_SETUP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

            builder.setMessage(getString(R.string.app_name) + " has funds transfer built in.\nThis great feature makes it easy to do the things you want. But to transfer to or from a bank, you need a bank setup.")
                    .setCancelable(false)
                    // Set the action buttons
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();

                            // Move to tab1
                            showTab(1);
                        }
                    });

            builder.create().show();
        } else if (appSettings.getTransMoneyStatus() != TRANSFER_VERIFIED) {
            showTab(1);
        }
    }


    public void showTab(int index) {
        pager.setCurrentItem(index, true);
    }

    private void setupTabIcons() {
        for (int i = 0; i < tabTitles.size(); i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons.get(i));
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {


        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            return tabTitles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }
    }

    private void changeTabsFont(TabLayout tabLayout) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(getNormalFont(mContext));
                }
            }
        }
    }

    public static Typeface getNormalFont(Context c) {
        try {
            return Typeface.createFromAsset(c.getAssets(), "fonts/OpenSans-Regular.ttf");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
