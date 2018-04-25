package com.example.pau.locationtracker;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivityFriends extends AppCompatActivity{

    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabsPagerAdapter;

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_friends);

        //Tabs for mainActivityFriends

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        mToolbar = (Toolbar) findViewById(R.id.mainActivity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");

        /*myViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("POSITION:: "+position);
                switch(position)
                {
                    case 1:
                        getSupportActionBar().setTitle("Friends");
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Requests");
                        break;
                    case 3:
                        getSupportActionBar().setTitle("Search Friends");
                        break;
                    default:
                        getSupportActionBar().setTitle("Friends");


                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

    }


   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        System.out.println("ON_MENUUUUUU "+ menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        final MenuItem item = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) item.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                return false;
            }
        });

        return true;
    }*/


}
