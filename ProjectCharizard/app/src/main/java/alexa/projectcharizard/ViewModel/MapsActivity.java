package alexa.projectcharizard.ViewModel;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import alexa.projectcharizard.Model.Database;
import alexa.projectcharizard.Model.Spot;
import alexa.projectcharizard.R;

/**
 * The activity for the MapView
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Database database = Database.getInstance();
    // The GoogleMap instance
    protected GoogleMap mMap;
    // All spots that will be added upon map refresh
    // The button for redirecting to Add Spot Activity
    private ImageButton plsBtn;

    final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 47;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connect to layout file
        contentView();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initPlsBtn();

        // Create temporary initial spot
        Spot spot = new Spot("Äppelträd i stan", 57.72, 11.98,
                "Gives red apples", true);
        database.getSpots().add(spot);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // Initializes the plus button to redirect to the AddSpotActivity
    protected void initPlsBtn() {
        // Find the plus button
        plsBtn = (ImageButton) findViewById(R.id.plsbtn);
        // Set a listener
        plsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, AddSpotActivity.class));
            }
        });
    }

    protected float initZoom(){
        return 10.0f;
    }

    protected void contentView(){
        setContentView(R.layout.activity_maps);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker on every 'spot', and center on Gothenburg
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        float initialZoomLevel = initZoom();
        LatLng initialLocation = new LatLng(57.7,11.96);

        mMap.moveCamera(CameraUpdateFactory.
                newLatLngZoom(initialLocation, initialZoomLevel));
        showUserLocation();

        database.getDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                database.getSpots().clear();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    Spot spot = data.getValue(Spot.class);
                    database.getSpots().add(spot);
                }
                updateMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Add marker on all 'spot's in spots
        updateMarkers();

    }

    protected void showUserLocation() {
        // Check if app has permission to access fine location
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            // If not request permission to access fine location
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION );
        } else {
            mMap.setMyLocationEnabled(true);
            //mMap.setOnMyLocationButtonClickListener(this);
            //mMap.setOnMyLocationClickListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    private void updateMarkers(){
        for (Spot spot : database.getSpots()) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(spot.getLatitude(), spot.getLongitude()))
                    .title(spot.getName())
                    .snippet(spot.getDescription())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        }
    }
}
