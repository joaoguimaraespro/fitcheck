package pt.ipp.estg.fitcheck.DataBases;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import pt.ipp.estg.fitcheck.Models.LatLng;

public class Converters {
    @TypeConverter
    public static List<pt.ipp.estg.fitcheck.Models.LatLng> fromString(String value) {
        Type listType = new TypeToken<List<pt.ipp.estg.fitcheck.Models.LatLng>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(List<LatLng> list){
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
