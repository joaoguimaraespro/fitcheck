package pt.ipp.estg.fitcheck.Models;

import java.util.Date;

public class User {
    public String username;

    public String email;

    public String gender;

    public String birthDate;

    public int height;

    public int weight;

    public long dailyObjective;

    public double totalDistance;

    public User(String username, String email, String gender, String birthDate, int height, int weight, long dailyObjective, double totalDistance) {
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.dailyObjective = dailyObjective;
        this.totalDistance = totalDistance;
    }

    public User() {
    }
}
