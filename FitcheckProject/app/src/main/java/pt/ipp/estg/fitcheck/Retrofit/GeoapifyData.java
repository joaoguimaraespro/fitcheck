package pt.ipp.estg.fitcheck.Retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeoapifyData {

    private GeoapifyDataAPI restInterface;

    public GeoapifyData(){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(0, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.geoapify.com/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        restInterface = retrofit.create(GeoapifyDataAPI.class);
    }

    public GeoapifyDataAPI getRestInterface() {
        return restInterface;
    }
}
