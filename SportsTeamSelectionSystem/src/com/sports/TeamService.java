package com.sports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamService {

    private List<Player> players;
    private Set<Integer> jerseyNumbers;
    private PracticeGroup practiceGroup;
    private int nextPlayerId;

    public TeamService() {
        players = new ArrayList<>();
        jerseyNumbers = new HashSet<>();
        practiceGroup = new PracticeGroup();
        nextPlayerId = 1;
    }

    // --------------------------------------------------
    // Player Registration
    // --------------------------------------------------

    public Player registerPlayer(
            String type,
            String name,
            int jerseyNumber,
            int[] ratings) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (name.contains("|")) {
            throw new IllegalArgumentException(
                    "Name cannot contain '|'."
            );
        }

        if (jerseyNumber < 1 || jerseyNumber > 99) {
            throw new IllegalArgumentException(
                    "Jersey number must be between 1 and 99."
            );
        }

        if (jerseyNumbers.contains(jerseyNumber)) {
            throw new IllegalArgumentException(
                    "Jersey number already exists."
            );
        }

        validateRatings(ratings);

        Player player;

        if ("Attacker".equalsIgnoreCase(type)) {
            player = new Attacker(
                    nextPlayerId,
                    name.trim(),
                    jerseyNumber,
                    ratings
            );
        } else if ("Defender".equalsIgnoreCase(type)) {
            player = new Defender(
                    nextPlayerId,
                    name.trim(),
                    jerseyNumber,
                    ratings
            );
        } else {
            throw new IllegalArgumentException(
                    "Invalid player type."
            );
        }

        players.add(player);
        jerseyNumbers.add(jerseyNumber);
        nextPlayerId++;

        return player;
    }

    // --------------------------------------------------
    // Validation
    // --------------------------------------------------

    public void validateRatings(int[] ratings) {

        if (ratings == null || ratings.length != 3) {
            throw new IllegalArgumentException(
                    "Exactly 3 ratings are required."
            );
        }

        for (int rating : ratings) {
            if (rating < 0 || rating > 10) {
                throw new IllegalArgumentException(
                        "Each rating must be between 0 and 10."
                );
            }
        }
    }

    // --------------------------------------------------
    // Find Player
    // --------------------------------------------------

    public Player findPlayerById(int id) {

        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }

        return null;
    }

    // --------------------------------------------------
    // View Players
    // --------------------------------------------------

    public void viewPlayers() {

        if (players.isEmpty()) {
            System.out.println("\nNo players registered.");
            return;
        }

        System.out.println("\n========== PLAYERS ==========");

        for (Player player : players) {

            int[] ratings = player.getRatings();

            System.out.printf(
                    "ID: %d | %s | Name: %s | Jersey: %d | "
                            + "Fitness: %d | Speed: %d | Teamwork: %d | Total: %d%n",
                    player.getId(),
                    player.getRoleLabel(),
                    player.getName(),
                    player.getJerseyNumber(),
                    ratings[0],
                    ratings[1],
                    ratings[2],
                    player.totalRating()
            );
        }
    }

    // --------------------------------------------------
    // Update Ratings
    // --------------------------------------------------

    public void updateRatings(int playerId, int[] newRatings) {

        Player player = findPlayerById(playerId);

        if (player == null) {
            throw new IllegalArgumentException(
                    "Player ID not found."
            );
        }

        validateRatings(newRatings);

        player.setRatings(newRatings);
    }

    // --------------------------------------------------
    // Practice Group
    // --------------------------------------------------

    public void assignSlot(int slot, int playerId) {

        if (findPlayerById(playerId) == null) {
            throw new IllegalArgumentException(
                    "Player ID not found."
            );
        }

        if (!practiceGroup.assign(slot, playerId)) {
            throw new IllegalArgumentException(
                    "Player is already assigned to another slot."
            );
        }
    }

    public void clearSlot(int slot) {

        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException(
                    "Slot must be between 1 and 5."
            );
        }

        practiceGroup.clear(slot);
    }

    public int[] getGroupPlayerIds() {
        return practiceGroup.getPlayerIds();
    }

    public void loadGroup(int[] ids) {
        practiceGroup.loadPlayerIds(ids);
    }

    // --------------------------------------------------
    // Data Access / Loading Support
    // --------------------------------------------------

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public void replacePlayers(List<Player> loadedPlayers) {

        players = new ArrayList<>();
        jerseyNumbers = new HashSet<>();

        int largestId = 0;

        for (Player player : loadedPlayers) {

            if (player == null) {
                throw new IllegalArgumentException(
                        "Player cannot be null."
                );
            }

            if (findPlayerInListById(players, player.getId()) != null) {
                throw new IllegalArgumentException(
                        "Duplicate player ID: " + player.getId()
                );
            }

            if (jerseyNumbers.contains(player.getJerseyNumber())) {
                throw new IllegalArgumentException(
                        "Duplicate jersey number: "
                                + player.getJerseyNumber()
                );
            }

            players.add(player);
            jerseyNumbers.add(player.getJerseyNumber());

            if (player.getId() > largestId) {
                largestId = player.getId();
            }
        }

        nextPlayerId = largestId + 1;
    }

    private Player findPlayerInListById(
            List<Player> list,
            int id) {

        for (Player player : list) {
            if (player.getId() == id) {
                return player;
            }
        }

        return null;
    }

    // --------------------------------------------------
    // Group Report
    // --------------------------------------------------

    public void showReport() {

        int[] ids = practiceGroup.getPlayerIds();

        int count = 0;
        int total = 0;

        System.out.println("\n========== PRACTICE GROUP ==========");

        for (int i = 0; i < ids.length; i++) {

            int playerId = ids[i];

            if (playerId == 0) {
                System.out.println(
                        "Slot " + (i + 1) + ": Empty"
                );
            } else {

                Player player = findPlayerById(playerId);

                if (player == null) {
                    System.out.println(
                            "Slot " + (i + 1)
                                    + ": Invalid player reference"
                    );
                } else {

                    int playerTotal = player.totalRating();

                    System.out.printf(
                            "Slot %d: %s | %s | Total: %d%n",
                            i + 1,
                            player.getName(),
                            player.getRoleLabel(),
                            playerTotal
                    );

                    count++;
                    total += playerTotal;
                }
            }
        }

        System.out.println("------------------------------------");
        System.out.println("Selected players: " + count);
        System.out.println("Group total rating: " + total);

        if (count > 0) {
            double average = (double) total / count;
            System.out.printf(
                    "Group average rating: %.2f%n",
                    average
            );
        } else {
            System.out.println("No players selected.");
        }
    }

    public int getSelectedPlayerCount() {

        int count = 0;

        for (int id : practiceGroup.getPlayerIds()) {
            if (id != 0 && findPlayerById(id) != null) {
                count++;
            }
        }

        return count;
    }

    public int getGroupTotalRating() {

        int total = 0;

        for (int id : practiceGroup.getPlayerIds()) {

            if (id != 0) {

                Player player = findPlayerById(id);

                if (player != null) {
                    total += player.totalRating();
                }
            }
        }

        return total;
    }

    public double getGroupAverage() {

        int count = getSelectedPlayerCount();

        if (count == 0) {
            return 0.0;
        }

        return (double) getGroupTotalRating() / count;
    }
}