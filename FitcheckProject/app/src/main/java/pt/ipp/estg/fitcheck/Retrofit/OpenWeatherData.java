package pt.ipp.estg.fitcheck.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenWeatherData {

    private OpenWeatherDataAPI restInterface;


    public OpenWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restInterface = retrofit.create(OpenWeatherDataAPI.class);
    }

    public OpenWeatherDataAPI getApi() {
        return restInterface;
    }

}
