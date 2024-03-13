package pt.ipp.estg.fitcheck.Models;

import java.util.List;

public class WeatherResponse {


    public List<Weather> weather;

    public double temp;


    public class Weather{

        public int id;

        public String main;

        public String description;

        public String icon;
    }
}
