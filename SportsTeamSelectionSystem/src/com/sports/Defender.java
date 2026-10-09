package com.sports;

public class Defender extends Player {

    public Defender(int id, String name, int jerseyNumber, int[] ratings) {
        super(id, name, jerseyNumber, ratings);
    }

    @Override
    public String getRoleLabel() {
        return "Defender";
    }
}