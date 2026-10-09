package com.sports;

public class Attacker extends Player {

    public Attacker(int id, String name, int jerseyNumber, int[] ratings) {
        super(id, name, jerseyNumber, ratings);
    }

    @Override
    public String getRoleLabel() {
        return "Attacker";
    }
}