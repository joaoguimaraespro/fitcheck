package pt.ipp.estg.fitcheck.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pt.ipp.estg.fitcheck.Activities.MenuActivity;
import pt.ipp.estg.fitcheck.Models.CurrentWeatherResponse;
import pt.ipp.estg.fitcheck.Models.ListResponse;
import pt.ipp.estg.fitcheck.Models.Response;
import pt.ipp.estg.fitcheck.Models.WeatherResponse;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.Retrofit.GeoapifyData;
import pt.ipp.estg.fitcheck.Retrofit.OpenWeatherData;
import retrofit2.Call;
import retrofit2.Callback;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeoapifyData geoApi;
    private LatLng latLng;
    private List<Response> locals;
    private GoogleMap mMap;
    private OpenWeatherData openWeatherData;

    private String[] categories;

    private ArrayAdapter adapter;

    private AutoCompleteTextView autoCompleteTextView;
    private Slider slider;

    private ImageView iconWeather;
    private TextView temperature, description, textViewRadius, textMaps;
    private String category;
    private float radius;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        geoApi = new GeoapifyData();
        openWeatherData = new OpenWeatherData();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.fragment_maps, container, false);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        iconWeather = contextView.findViewById(R.id.iconWeather);
        description = contextView.findViewById(R.id.description);
        temperature = contextView.findViewById(R.id.temprature);
        autoCompleteTextView = contextView.findViewById(R.id.autoComplete);
        categories = getResources().getStringArray(R.array.places_category);
        adapter = new ArrayAdapter(getActivity(), R.layout.drop_menu_item, categories);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setText(adapter.getItem(2).toString(), false);
        slider = contextView.findViewById(R.id.slider);
        textViewRadius = contextView.findViewById(R.id.radius);
        textMaps = contextView.findViewById(R.id.textMaps);
        category = "sport.fitness";
        textMaps.setText("Ginásios perto de si onde pode melhorar a sua forma física");

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        category = "building.sport";
                        textMaps.setText("Edifícios Desportivos possíveis de alugar para a " +
                                "prática de exercício físico");
                        break;
                    case 1:
                        category = "sport.track";
                        textMaps.setText("Pistas Desportivas onde pode fazer uma corrida ou caminhada");
                        break;
                    case 2:
                        category = "sport.fitness";
                        textMaps.setText("Ginásios perto de si onde pode melhorar a sua forma física");
                        break;
                    case 3:
                        category = "activity.sport_club";
                        textMaps.setText("Associações onde se pode afiliar como atleta e elevar o nível " +
                                "da sua forma física com alguma competitividade");
                        break;
                    case 4:
                        category = "national_park";
                        textMaps.setText("Parques Naturais perto de si para praticar execício físico em " +
                                "conjunto com a Natureza");
                        break;
                }
                if (mMap != null) {
                    mMap.clear();
                }
                updateMap();
            }
        });

        slider.setValue(30);
        textViewRadius.setText("Raio: 30km");
        radius = 30000;
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                radius = value * 1000;
                if (mMap != null) {
                    mMap.clear();
                }
                textViewRadius.setText("Raio: " + (int) value + "km");
                updateMap();
            }
        });


        return contextView;
    }

    public void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    supportMapFragment.getMapAsync(MapsFragment.this);
                    updateMap();
                    getWeather();
                }
            }
        });
    }

    private void getWeather() {
        openWeatherData.getApi().
                getWeatherByLocation(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude), "pt"
                        , "metric", getResources().getString(R.string.apiKeyWeather)).enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, retrofit2.Response<CurrentWeatherResponse> response) {
                if (response.body() == null) {
                    return;
                }

                CurrentWeatherResponse currentWeatherResponse = response.body();
                WeatherResponse weatherResponse = currentWeatherResponse.current;
                List<WeatherResponse.Weather> weatherList = weatherResponse.weather;
                Glide.with(requireContext()).load("https://openweathermap.org/img/wn/" + weatherList.get(0).icon +
                        "@2x.png").into(iconWeather);

                String d = weatherList.get(0).description.substring(0, 1).toUpperCase(Locale.ROOT) + weatherList.get(0).description.substring(1);

                description.setText(d);
                temperature.setText(currentWeatherResponse.current.temp + "ºC");
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {

            }
        });
    }


    public void updateMap() {
        String filter = java.net.URLDecoder.decode("circle:" + latLng.longitude + "," +
                latLng.latitude + "," + radius);
        String bias = java.net.URLDecoder.decode("proximity:" + latLng.longitude + "," +
                latLng.latitude);
        geoApi.getRestInterface().getLocals(category, filter, bias, 100, getResources().getString(R.string.apiKeyGeo)).enqueue(new Callback<ListResponse<Response>>() {
            @Override
            public void onResponse(Call<ListResponse<Response>> call, retrofit2.Response<ListResponse<Response>> response) {
                if (response.body() == null) {
                    return;
                }

                if (mMap == null) {
                    return;
                }

                locals = response.body().features;


                MarkerOptions options = new MarkerOptions().position(latLng).title("Situa-se aqui")
                        .icon(bitmapDescriptor(getContext()));
                mMap.addMarker(options).showInfoWindow();

                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(latLng);
                circleOptions.radius(radius);
                circleOptions.fillColor(0x30606060);
                circleOptions.strokeWidth(2);
                circleOptions.strokeColor(R.color.gray);


                mMap.addCircle(circleOptions);


                for (Response localTrain : locals) {
                    if (localTrain.properties.lat != 0 && localTrain.properties.lon != 0) {
                        LatLng marker = new LatLng(localTrain.properties.lat, localTrain.properties.lon);
                        MarkerOptions marker1;
                        if (localTrain.properties.name == null) {
                            marker1 = new MarkerOptions().position(marker).title("Prática de Desporto")
                                    .snippet(localTrain.properties.address_line2 + "\n" + localTrain.properties.address_line1 +
                                            "\nDistância: " + (localTrain.properties.distance / 1000) + "km");

                        } else {
                            marker1 = new MarkerOptions().position(marker).title(localTrain.properties.name)
                                    .snippet(localTrain.properties.address_line2 + "\n" + localTrain.properties.address_line1);

                        }
                        switch (category) {
                            case "building.sport":
                                marker1.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_edificios_1));
                                break;
                            case "sport.track":
                                marker1.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_pista_de_corrida));
                                break;
                            case "sport.fitness":
                                marker1.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_market_sport));
                                break;
                            case "activity.sport_club":
                                marker1.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_distintivo_1));
                                break;
                            case "national_park":
                                marker1.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_natureza));
                                break;
                        }
                        mMap.addMarker(marker1);
                    }
                }


                if (radius == 10000 || radius == 15000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.5F));
                }
                if (radius == 20000 || radius == 25000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.7F));
                }
                if (radius == 30000 || radius == 35000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.3F));
                }
                if (radius == 40000 || radius == 45000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9F));
                }
                if (radius == 50000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.7F));
                }


            }

            @Override
            public void onFailure(Call<ListResponse<Response>> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Erro a carregar", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
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

        MarkerOptions options = new MarkerOptions().position(latLng).title("Situa-se aqui")
                .icon(bitmapDescriptor(getContext()));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View myContentView = getLayoutInflater().inflate(
                        R.layout.fragment_custom_info_window_adapter, null);
                TextView tvTitle = ((TextView) myContentView
                        .findViewById(R.id.title));
                tvTitle.setText(marker.getTitle());
                TextView tvSnippet = ((TextView) myContentView
                        .findViewById(R.id.snippet));
                if (marker.getSnippet() != null) {
                    tvSnippet.setText(marker.getSnippet());
                } else {
                    tvSnippet.setVisibility(View.GONE);
                }
                return myContentView;
            }
        });

        mMap.addMarker(options).showInfoWindow();

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_locmarket);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor bitmapDescriptor(Context context) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_personmarker);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}

