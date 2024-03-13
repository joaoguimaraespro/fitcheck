package pt.ipp.estg.fitcheck.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import pt.ipp.estg.fitcheck.Models.LatLng;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.Retrofit.GeoapifyData;
import pt.ipp.estg.fitcheck.Retrofit.OpenWeatherData;


public class TrainingRouteMapFragment extends Fragment implements OnMapReadyCallback {

    private List<pt.ipp.estg.fitcheck.Models.LatLng> percurso;


    private GoogleMap mMap;

    private SupportMapFragment supportMapFragment;
    private PolylineOptions polylineOptions;

    public TrainingRouteMapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            percurso = (List<LatLng>) getArguments().getSerializable("trainingRoute");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map1);
        supportMapFragment.getMapAsync(TrainingRouteMapFragment.this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_training_route_map, container, false);
        // Inflate the layout for this fragment

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                mMap.setMapStyle(new MapStyleOptions(getResources()
                        .getString(R.string.style_json)));
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        PolylineOptions p = new PolylineOptions().clickable(true);

        for (pt.ipp.estg.fitcheck.Models.LatLng l : percurso) {
            com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(l.latitude, l.longitude);
            p.add(latLng);
        }

        mMap.addPolyline(p);

        com.google.android.gms.maps.model.LatLng lat1 = new com.google.android.gms.maps.model.LatLng(percurso.get(0).getLatitude(), percurso.get(0).getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat1, 12));

    }
}