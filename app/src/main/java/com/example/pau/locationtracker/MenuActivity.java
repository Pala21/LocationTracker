package com.example.pau.locationtracker;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;

public class MenuActivity extends FragmentActivity {

    private ImageButton btnSeeMap, btnSettings, btnFriends;
    private AlphaAnimation buttonClick = new AlphaAnimation(10F, 0.8F);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //findViewById(R.id.menu).getBackground().setAlpha(190);

        btnSeeMap = (ImageButton) findViewById(R.id.LocationButton);
        btnSeeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                MenuActivity.this.startActivity(intent);
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.SettingsButton);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                MenuActivity.this.startActivity(intent);
            }
        });

        btnFriends = (ImageButton) findViewById(R.id.FriendsButton);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                Intent intent = new Intent(MenuActivity.this, FriendsActivity.class);
                MenuActivity.this.startActivity(intent);
            }
        });
    }
}


