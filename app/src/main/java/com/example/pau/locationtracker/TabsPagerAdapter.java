package com.example.pau.locationtracker;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by pau on 09/04/2018.
 */


class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        System.out.println("POSITION:: "+position);
        switch(position)
        {
            case 0:
                return new FriendsFragment();
            case 1:
                return new RequestFragment();
            case 2:
                return new AddFriendFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Friends";
            case 1:
                return "Requests";
            case 2:
                return "Search Friends";
            default:
                return null;
        }
    }
}
