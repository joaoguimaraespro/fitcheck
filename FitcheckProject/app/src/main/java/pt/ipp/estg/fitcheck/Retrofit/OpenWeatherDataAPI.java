package pt.ipp.estg.fitcheck.Retrofit;

import android.graphics.drawable.Icon;

import pt.ipp.estg.fitcheck.Models.CurrentWeatherResponse;
import pt.ipp.estg.fitcheck.Models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenWeatherDataAPI {
    @GET("/data/2.5/onecall")
    Call<CurrentWeatherResponse> getWeatherByLocation(@Query("lat") String lat, @Query("lon") String lon, @Query("lang") String lang, @Query("units") String units, @Query("appid") String appid);


}
