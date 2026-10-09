package com.sports;

public abstract class Player {

    private int id;
    private int jerseyNumber;
    private String name;
    private int[] ratings;

    public Player(int id, String name, int jerseyNumber, int[] ratings) {
        this.id = id;
        this.name = name;
        this.jerseyNumber = jerseyNumber;
        setRatings(ratings);
    }

    public int getId() {
        return id;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public String getName() {
        return name;
    }

    public int[] getRatings() {
        return ratings.clone();
    }

    public void setRatings(int[] ratings) {
        if (ratings == null || ratings.length != 3) {
            throw new IllegalArgumentException(
                    "Exactly 3 ratings are required."
            );
        }

        for (int rating : ratings) {
            if (rating < 0 || rating > 10) {
                throw new IllegalArgumentException(
                        "Ratings must be between 0 and 10."
                );
            }
        }

        this.ratings = ratings.clone();
    }

    public int totalRating() {
        return ratings[0] + ratings[1] + ratings[2];
    }

    public abstract String getRoleLabel();
}