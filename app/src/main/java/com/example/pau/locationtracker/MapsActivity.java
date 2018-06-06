package com.example.pau.locationtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity  implements OnMapReadyCallback, LocationListener,GoogleMap.OnCameraMoveStartedListener {

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private boolean isMaptouched;
    DatabaseReference groupMembersReference;
    MarkerOptions mo;
    LocationManager locationManager;
    private DatabaseReference mDatabase;
    ArrayList<String> mUsers;
    private FirebaseAuth mAuth;
    String id;
    StorageReference storageRef;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My current Location");
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getUid();
        Button button = (Button) findViewById(R.id.centrate);
        button.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();

        if (!isLocationEnabled())
            showAlert(1);

        groupMembersReference = FirebaseDatabase.getInstance().getReference().child("groups");
        groupMembersReference.keepSynced(true);
        key = (String) getIntent().getExtras().get("GROUPKEY");
        mUsers = new ArrayList<>();

        storageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        groupMembersReference.child(key).child("usersId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d2 : dataSnapshot.getChildren()){
                    for(DataSnapshot d3 : d2.getChildren()){
                        System.out.println("D3 :: "+d3.getKey() + " "+d3.getValue());
                        if(d3.getKey().equals("visibility")){
                            if(d3.getValue().equals(true)){
                                mUsers.add((String) d2.getKey());
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot1.hasChildren()) {
                        final String key = dataSnapshot1.getKey();
                        System.out.println("KEYs:: "+dataSnapshot1.getKey());
                        System.out.println("mUSERSS AND KEY:: "+mUsers + " "+key);
                        if(mUsers.contains(key)){
                            mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(final DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                        if (key.equals(dataSnapshot2.getKey())) {
                                            Double lat = (Double) dataSnapshot1.child("loc").child("latitude").getValue();
                                            Double lon = (Double) dataSnapshot1.child("loc").child("longitude").getValue();
                                            final LatLng coord = new LatLng(lat, lon);


                                            StorageReference filePath = storageRef.child(key+".jpg");
                                            System.out.println("FILEPATH ::"+filePath.getName());

                                            //user sense foto de perfil
                                            filePath.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                                            R.drawable.ic_shortcut_perm_identity);

                                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(icon, 130, 130, false);

                                                    mo = new MarkerOptions().position(coord).title((String) dataSnapshot2.child("username")
                                                            .getValue()).snippet((String) dataSnapshot2.child("fullname").
                                                            getValue()).icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

                                                    if (!mMarkers.containsKey(key)) {
                                                        mMarkers.put(key, mMap.addMarker(mo));
                                                    } else {
                                                        System.out.println(key);
                                                        mMarkers.get(key).setPosition(coord);
                                                    }
                                                }
                                            });

                                            //user amb foto de perfil
                                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(MapsActivity.this, R.color.colorPrimaryDark));
                                                    Glide.with(getApplicationContext())
                                                            .load(uri.toString())
                                                            .asBitmap()
                                                            .listener(new RequestListener<String, Bitmap>() {
                                                                @Override
                                                                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                                                    System.out.println("Entra error");
                                                                    return false;
                                                                }

                                                                @Override
                                                                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                                    System.out.println("Entra ok");
                                                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, 110, 110, false);
                                                                    Bitmap circle = getCroppedBitmap(resizedBitmap);
                                                                    mo = (new MarkerOptions()
                                                                            .position(coord)
                                                                            .title((String) dataSnapshot2.child("username").getValue())
                                                                            .snippet((String) dataSnapshot2.child("fullname").getValue())
                                                                            .icon(BitmapDescriptorFactory.fromBitmap(circle))
                                                                    );


                                                                    if (!mMarkers.containsKey(key)) {
                                                                        mMarkers.put(key, mMap.addMarker(mo));
                                                                    } else {
                                                                        System.out.println(key);
                                                                        mMarkers.get(key).setPosition(coord);
                                                                    }
                                                                    return true;
                                                                }
                                                            })
                                                            .placeholder(cd)
                                                            .centerCrop()
                                                            .preload();
                                                }
                                            });

                                           /* mo = new MarkerOptions().position(coord).title((String) dataSnapshot2.child("username")
                                                .getValue()).snippet((String) dataSnapshot2.child("fullname").getValue());
                                            if (!mMarkers.containsKey(key)) {
                                                mMarkers.put(key, mMap.addMarker(mo));
                                            } else {
                                                System.out.println(key);
                                                mMarkers.get(key).setPosition(coord);
                                            }*/
                                        }
                                    }
                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



        /*mDatabase.child("locations").addValueEventListener(new ValueEventListener() {
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
                                            System.out.println(key);
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
        });*/

        //per quan carregui de principi que es possi en l'última posició.
        final DatabaseReference m = mDatabase.child("locations").child(id);
        m.child("loc").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Location loc = new Location("Location");
                        if (dataSnapshot.hasChildren()) {
                            Double lat = (Double) dataSnapshot.child("latitude").getValue();
                            Double lon = (Double) dataSnapshot.child("longitude").getValue();
                            loc.setLatitude(lat);
                            loc.setLongitude(lon);
                            searchLocation(loc);
                        } else{
                            //PUT SPINNER!!
                            Toast.makeText(MapsActivity.this, "LOADING CURRENT LOCATION...", Toast.LENGTH_LONG).show();
                            searchLocation(loc);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker m) {
                boolean ap = false;
                if(mMap.getCameraPosition().zoom < 17.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 17.0f), 3000, null);
                    ap = true;
                }else if(mMap.getCameraPosition().zoom >= 17.0f){
                    ap = false;
                }
                return ap;
            }

        });
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }


    @Override
    public void onLocationChanged(final Location location) {
        final LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        mDatabase.child("locations").child(id).child("loc").setValue(myCoordinates);

        mMap.setOnCameraMoveStartedListener(this);

        final Button button = findViewById(R.id.centrate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocation(location);
                button.setVisibility(View.INVISIBLE);
            }
        });
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

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            isMaptouched = true;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            isMaptouched = false;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            isMaptouched = false;
        }
        if(isMaptouched){
            Button button = (Button) findViewById(R.id.centrate);
            button.setVisibility(View.VISIBLE);
        }
    }
}




