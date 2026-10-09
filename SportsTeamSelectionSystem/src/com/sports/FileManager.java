package com.sports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {

    private static final Path DATA_DIR =
            Path.of("data");

    private static final Path PLAYERS_FILE =
            DATA_DIR.resolve("players.txt");

    private static final Path TEAM_FILE =
            DATA_DIR.resolve("team.txt");

    private static final Path REPORT_FILE =
            DATA_DIR.resolve("report.txt");

    // --------------------------------------------------
    // Create data directory
    // --------------------------------------------------

    public static void initializeDataDirectory()
            throws IOException {

        Files.createDirectories(DATA_DIR);
    }

    // --------------------------------------------------
    // Load Players
    // --------------------------------------------------

    public static List<Player> loadPlayers()
            throws IOException {

        List<Player> loadedPlayers = new ArrayList<>();

        if (!Files.exists(PLAYERS_FILE)) {
            return loadedPlayers;
        }

        Set<Integer> ids = new HashSet<>();
        Set<Integer> jerseys = new HashSet<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             PLAYERS_FILE,
                             StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length != 7) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Expected 7 fields."
                    );
                }

                int id;
                int jersey;
                int fitness;
                int speed;
                int teamwork;

                try {

                    id = Integer.parseInt(parts[0].trim());
                    jersey = Integer.parseInt(parts[3].trim());
                    fitness = Integer.parseInt(parts[4].trim());
                    speed = Integer.parseInt(parts[5].trim());
                    teamwork = Integer.parseInt(parts[6].trim());

                } catch (NumberFormatException e) {

                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Numeric field is invalid."
                    );
                }

                if (id <= 0) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Player ID must be positive."
                    );
                }

                if (jersey < 1 || jersey > 99) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Jersey number must be between 1 and 99."
                    );
                }

                if (!ids.add(id)) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Duplicate player ID: " + id
                    );
                }

                if (!jerseys.add(jersey)) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Duplicate jersey number: " + jersey
                    );
                }

                if (parts[2].trim().isEmpty()) {
                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            "Player name cannot be empty."
                    );
                }

                int[] ratings = {
                        fitness,
                        speed,
                        teamwork
                };

                try {

                    Player player;

                    if ("Attacker".equalsIgnoreCase(parts[1].trim())) {

                        player = new Attacker(
                                id,
                                parts[2].trim(),
                                jersey,
                                ratings
                        );

                    } else if ("Defender".equalsIgnoreCase(parts[1].trim())) {

                        player = new Defender(
                                id,
                                parts[2].trim(),
                                jersey,
                                ratings
                        );

                    } else {

                        throw fileError(
                                PLAYERS_FILE,
                                lineNumber,
                                "Invalid player type: "
                                        + parts[1]
                        );
                    }

                    loadedPlayers.add(player);

                } catch (IllegalArgumentException e) {

                    throw fileError(
                            PLAYERS_FILE,
                            lineNumber,
                            e.getMessage()
                    );
                }
            }
        }

        return loadedPlayers;
    }

    // --------------------------------------------------
    // Save Players
    // --------------------------------------------------

    public static void savePlayers(
            List<Player> players)
            throws IOException {

        StringBuilder content = new StringBuilder();

        for (Player player : players) {

            int[] ratings = player.getRatings();

            content.append(player.getId())
                    .append("|")
                    .append(player.getRoleLabel())
                    .append("|")
                    .append(player.getName())
                    .append("|")
                    .append(player.getJerseyNumber())
                    .append("|")
                    .append(ratings[0])
                    .append("|")
                    .append(ratings[1])
                    .append("|")
                    .append(ratings[2])
                    .append(System.lineSeparator());
        }

        writeAtomically(PLAYERS_FILE, content.toString());
    }

    // --------------------------------------------------
    // Load Team
    // --------------------------------------------------

    public static int[] loadTeam(
            TeamService service)
            throws IOException {

        int[] ids = new int[5];

        if (!Files.exists(TEAM_FILE)) {
            return ids;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             TEAM_FILE,
                             StandardCharsets.UTF_8)) {

            String line = reader.readLine();

            if (line == null) {
                return ids;
            }

            if (reader.readLine() != null) {
                throw fileError(
                        TEAM_FILE,
                        2,
                        "Expected exactly one line."
                );
            }

            String[] parts = line.split(",", -1);

            if (parts.length != 5) {
                throw fileError(
                        TEAM_FILE,
                        1,
                        "Expected exactly 5 slot values."
                );
            }

            Set<Integer> seen = new HashSet<>();

            for (int i = 0; i < 5; i++) {

                int id;

                try {
                    id = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException e) {
                    throw fileError(
                            TEAM_FILE,
                            1,
                            "Invalid player ID in slot "
                                    + (i + 1)
                    );
                }

                if (id < 0) {
                    throw fileError(
                            TEAM_FILE,
                            1,
                            "Player ID cannot be negative."
                    );
                }

                if (id != 0) {

                    if (!seen.add(id)) {
                        throw fileError(
                                TEAM_FILE,
                                1,
                                "Duplicate player ID: " + id
                        );
                    }

                    if (service.findPlayerById(id) == null) {
                        throw fileError(
                                TEAM_FILE,
                                1,
                                "Player ID does not exist: " + id
                        );
                    }
                }

                ids[i] = id;
            }
        }

        return ids;
    }

    // --------------------------------------------------
    // Save Team
    // --------------------------------------------------

    public static void saveTeam(
            int[] ids)
            throws IOException {

        if (ids == null || ids.length != 5) {
            throw new IllegalArgumentException(
                    "Team must contain exactly 5 slots."
            );
        }

        StringBuilder content = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {

            if (i > 0) {
                content.append(",");
            }

            content.append(ids[i]);
        }

        content.append(System.lineSeparator());

        writeAtomically(TEAM_FILE, content.toString());
    }

    // --------------------------------------------------
    // Save Report
    // --------------------------------------------------

    public static void saveReport(
            TeamService service)
            throws IOException {

        StringBuilder content = new StringBuilder();

        int[] ids = service.getGroupPlayerIds();

        int count = 0;
        int total = 0;

        content.append("PRACTICE GROUP REPORT")
                .append(System.lineSeparator())
                .append("=====================")
                .append(System.lineSeparator());

        for (int i = 0; i < ids.length; i++) {

            int playerId = ids[i];

            if (playerId == 0) {

                content.append("Slot ")
                        .append(i + 1)
                        .append(": Empty")
                        .append(System.lineSeparator());

            } else {

                Player player =
                        service.findPlayerById(playerId);

                if (player == null) {

                    content.append("Slot ")
                            .append(i + 1)
                            .append(": Invalid player reference")
                            .append(System.lineSeparator());

                } else {

                    int playerTotal =
                            player.totalRating();

                    content.append("Slot ")
                            .append(i + 1)
                            .append(": ")
                            .append(player.getName())
                            .append(" | ")
                            .append(player.getRoleLabel())
                            .append(" | Total: ")
                            .append(playerTotal)
                            .append(System.lineSeparator());

                    count++;
                    total += playerTotal;
                }
            }
        }

        content.append("---------------------")
                .append(System.lineSeparator());

        content.append("Selected players: ")
                .append(count)
                .append(System.lineSeparator());

        content.append("Group total rating: ")
                .append(total)
                .append(System.lineSeparator());

        if (count > 0) {

            double average =
                    (double) total / count;

            content.append(
                    String.format(
                            java.util.Locale.ROOT,
                            "Group average rating: %.2f",
                            average
                    )
            ).append(System.lineSeparator());

        } else {

            content.append("No players selected.")
                    .append(System.lineSeparator());
        }

        writeAtomically(
                REPORT_FILE,
                content.toString()
        );
    }

    // --------------------------------------------------
    // Atomic File Write
    // --------------------------------------------------

    private static void writeAtomically(
            Path target,
            String content)
            throws IOException {

        Files.createDirectories(
                target.getParent()
        );

        Path temp = target.resolveSibling(
                target.getFileName() + ".tmp"
        );

        try {

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temp,
                                 StandardCharsets.UTF_8)) {

                writer.write(content);
            }

            Files.move(
                    temp,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }

            throw new IOException(
                    "Unable to save file '"
                            + target
                            + "': "
                            + e.getMessage(),
                    e
            );
        }
    }

    // --------------------------------------------------
    // File Error Helper
    // --------------------------------------------------

    private static IOException fileError(
            Path file,
            int line,
            String message) {

        return new IOException(
                "File error in "
                        + file
                        + " at line "
                        + line
                        + ": "
                        + message
        );
    }
}