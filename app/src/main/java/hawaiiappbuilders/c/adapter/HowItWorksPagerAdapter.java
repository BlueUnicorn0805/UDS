package hawaiiappbuilders.c.adapter;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.fragments.BaseFragment;
import hawaiiappbuilders.c.fragments.IntroFragment;

public class HowItWorksPagerAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragments;

    public HowItWorksPagerAdapter(FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<>();
        //fragments.add(HowItWorks1Fragment.newInstance("Help1"));
        //fragments.add(HowItWorks2Fragment.newInstance("Help2"));
        //fragments.add(HowItWorks3Fragment.newInstance("Help3"));

        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_0));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_1));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_2));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_3));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_4));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_5));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_8));
        fragments.add(IntroFragment.newInstance(R.layout.intro_layout_9));
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        fragments.set(position, (BaseFragment) fragment);
        return fragment;
    }

    public void addCardFragment(BaseFragment fragment) {
        fragments.add(fragment);
    }

}
