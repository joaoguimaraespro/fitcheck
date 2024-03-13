package pt.ipp.estg.fitcheck.Retrofit;

import pt.ipp.estg.fitcheck.Models.ListResponse;
import pt.ipp.estg.fitcheck.Models.FitnessLocation;
import pt.ipp.estg.fitcheck.Models.Response;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeoapifyDataAPI {

    @GET("places?")
    Call<ListResponse<Response>> getLocals(@Query("categories") String categories,
                                           @Query("filter") String filter,
                                           @Query("bias") String bias,
                                           @Query("limit") int limit,
                                           @Query("apiKey") String apiKey);
}
