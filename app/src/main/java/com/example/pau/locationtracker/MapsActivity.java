package com.example.pau.locationtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private double lat;
    private double lon;
    private boolean isMaptouched;
    MarkerOptions mo;
    LocationManager locationManager;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My current Location");

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();

        if (!isLocationEnabled())
            showAlert(1);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mDatabase.child("locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if(dataSnapshot1.hasChildren()) {
                        final String key = dataSnapshot1.getKey();
                        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                    if (key.equals(dataSnapshot2.getKey())) {
                                        Double lat = (Double) dataSnapshot1.child("loc").child("latitude").getValue();
                                        Double lon = (Double) dataSnapshot1.child("loc").child("longitude").getValue();
                                        LatLng coord = new LatLng(lat, lon);
                                        mo = new MarkerOptions().position(coord).title("User " + dataSnapshot2.child("email").child("email").getValue());

                                        if (!mMarkers.containsKey(key)) {
                                            mMarkers.put(key, mMap.addMarker(mo));
                                        } else {
                                            mMarkers.get(key).setPosition(coord);
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onLocationChanged(final Location location) {
        final LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason ==REASON_GESTURE) {
                    isMaptouched = true;
                }else if (reason ==REASON_API_ANIMATION) {
                    isMaptouched = false;
                } else if (reason ==REASON_DEVELOPER_ANIMATION) {
                    isMaptouched = false;
                }
            }
        });

        if(isMaptouched){
            Button button = (Button) findViewById(R.id.centrate);
            button.setVisibility(View.VISIBLE);
        }

        final Button button = findViewById(R.id.centrate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocation(location);
                button.setVisibility(View.INVISIBLE);
            }
        });

        mDatabase.child("locations").child(FirebaseAuth.getInstance().getCurrentUser().
                getUid()).child("loc").setValue(myCoordinates);
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);


    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isPermissionGranted() {
        if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        }else{
            Log.v("mylog","Permission not granted");
            return false;
        }
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                    "use this app";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    public void searchLocation(Location location){
        final LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        final float zoomLevel = 17.0f;
        final DatabaseReference m = mDatabase.child("locations").child(FirebaseAuth.getInstance().getCurrentUser().
                getUid());
        m.child("loc").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Button button = (Button) findViewById(R.id.centrate);

                        if (dataSnapshot.hasChildren()) {
                            Double lat = (Double) dataSnapshot.child("latitude").getValue();
                            Double lon = (Double) dataSnapshot.child("longitude").getValue();
                            LatLng latlon = new LatLng(lat, lon);
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoomLevel), 6000, null);
                            if(!button.isShown()) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoomLevel));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f), 2000, null);
                            }
                        } else {
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel), 6000, null);
                            if(!button.isShown()) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f), 2000, null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );

    }

}
