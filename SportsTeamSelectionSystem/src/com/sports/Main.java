package com.sports;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner =
            new Scanner(System.in);

    public static void main(String[] args) {

        TeamService service = new TeamService();

        // --------------------------------------------
        // Startup loading
        // --------------------------------------------

        try {

            FileManager.initializeDataDirectory();

            List<Player> players =
                    FileManager.loadPlayers();

            service.replacePlayers(players);

            int[] group =
                    FileManager.loadTeam(service);

            service.loadGroup(group);

            System.out.println(
                    "Sports Team Selection System started."
            );

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "Startup error: " + e.getMessage()
            );

            System.out.println(
                    "Application will start with available empty data."
            );
        }

        // --------------------------------------------
        // Main Menu
        // --------------------------------------------

        while (true) {

            printMenu();

            int choice = readInt(
                    "Enter choice: "
            );

            switch (choice) {

                case 1:
                    registerPlayer(service);
                    break;

                case 2:
                    service.viewPlayers();
                    break;

                case 3:
                    updateRatings(service);
                    break;

                case 4:
                    assignGroupSlot(service);
                    break;

                case 5:
                    clearGroupSlot(service);
                    break;

                case 6:
                    viewAndSaveReport(service);
                    break;

                case 0:
                    System.out.println(
                            "Exiting application..."
                    );
                    scanner.close();
                    return;

                default:
                    System.out.println(
                            "Invalid menu choice."
                    );
            }
        }
    }

    // ==================================================
    // MENU
    // ==================================================

    private static void printMenu() {

        System.out.println();
        System.out.println(
                "========== SPORTS TEAM SELECTION SYSTEM =========="
        );
        System.out.println("1. Register player");
        System.out.println("2. View players");
        System.out.println("3. Update player ratings");
        System.out.println("4. Assign group slot");
        System.out.println("5. Clear group slot");
        System.out.println("6. View group and report");
        System.out.println("0. Exit");
        System.out.println(
                "==================================================="
        );
    }

    // ==================================================
    // REGISTER PLAYER
    // ==================================================

    private static void registerPlayer(
            TeamService service) {

        System.out.println(
                "\n========== REGISTER PLAYER =========="
        );

        String type;

        while (true) {

            System.out.println("1. Attacker");
            System.out.println("2. Defender");

            int typeChoice =
                    readInt("Select type: ");

            if (typeChoice == 1) {
                type = "Attacker";
                break;
            }

            if (typeChoice == 2) {
                type = "Defender";
                break;
            }

            System.out.println(
                    "Invalid player type."
            );
        }

        String name =
                readString("Enter player name: ");

        int jersey =
                readInt("Enter jersey number (1-99): ");

        int fitness =
                readRating("Enter fitness rating (0-10): ");

        int speed =
                readRating("Enter speed rating (0-10): ");

        int teamwork =
                readRating(
                        "Enter teamwork rating (0-10): "
                );

        int[] ratings = {
                fitness,
                speed,
                teamwork
        };

        // Snapshot for rollback
        List<Player> oldPlayers =
                service.getPlayers();

        try {

            Player player =
                    service.registerPlayer(
                            type,
                            name,
                            jersey,
                            ratings
                    );

            // Save BEFORE confirming success
            FileManager.savePlayers(
                    service.getPlayers()
            );

            System.out.println(
                    "Player registered successfully."
            );

            System.out.println(
                    "Player ID: " + player.getId()
            );

        } catch (IllegalArgumentException | IOException e) {

            // Roll back registration if saving fails
            service.replacePlayers(oldPlayers);

            System.out.println(
                    "Registration failed: "
                            + e.getMessage()
            );
        }
    }

    // ==================================================
    // UPDATE RATINGS
    // ==================================================

    private static void updateRatings(
            TeamService service) {

        System.out.println(
                "\n========== UPDATE RATINGS =========="
        );

        int id =
                readInt("Enter player ID: ");

        Player player =
                service.findPlayerById(id);

        if (player == null) {

            System.out.println(
                    "Player ID not found."
            );

            return;
        }

        int[] oldRatings =
                player.getRatings();

        int fitness =
                readRating(
                        "Enter new fitness rating (0-10): "
                );

        int speed =
                readRating(
                        "Enter new speed rating (0-10): "
                );

        int teamwork =
                readRating(
                        "Enter new teamwork rating (0-10): "
                );

        int[] newRatings = {
                fitness,
                speed,
                teamwork
        };

        try {

            service.updateRatings(
                    id,
                    newRatings
            );

            FileManager.savePlayers(
                    service.getPlayers()
            );

            System.out.println(
                    "Ratings updated successfully."
            );

        } catch (IllegalArgumentException | IOException e) {

            // Roll back rating change
            player.setRatings(oldRatings);

            System.out.println(
                    "Rating update failed: "
                            + e.getMessage()
            );
        }
    }

    // ==================================================
    // ASSIGN GROUP SLOT
    // ==================================================

    private static void assignGroupSlot(
            TeamService service) {

        System.out.println(
                "\n========== ASSIGN GROUP SLOT =========="
        );

        int slot =
                readInt("Enter slot number (1-5): ");

        int playerId =
                readInt("Enter player ID: ");

        int[] oldGroup =
                service.getGroupPlayerIds();

        try {

            service.assignSlot(
                    slot,
                    playerId
            );

            FileManager.saveTeam(
                    service.getGroupPlayerIds()
            );

            System.out.println(
                    "Player assigned successfully."
            );

        } catch (IllegalArgumentException | IOException e) {

            // Roll back group change
            service.loadGroup(oldGroup);

            System.out.println(
                    "Assignment failed: "
                            + e.getMessage()
            );
        }
    }

    // ==================================================
    // CLEAR GROUP SLOT
    // ==================================================

    private static void clearGroupSlot(
            TeamService service) {

        System.out.println(
                "\n========== CLEAR GROUP SLOT =========="
        );

        int slot =
                readInt("Enter slot number (1-5): ");

        int[] oldGroup =
                service.getGroupPlayerIds();

        try {

            service.clearSlot(slot);

            FileManager.saveTeam(
                    service.getGroupPlayerIds()
            );

            System.out.println(
                    "Group slot cleared successfully."
            );

        } catch (IllegalArgumentException | IOException e) {

            // Roll back
            service.loadGroup(oldGroup);

            System.out.println(
                    "Unable to clear slot: "
                            + e.getMessage()
            );
        }
    }

    // ==================================================
    // REPORT
    // ==================================================

    private static void viewAndSaveReport(
            TeamService service) {

        System.out.println(
                "\n========== GROUP REPORT =========="
        );

        service.showReport();

        try {

            FileManager.saveReport(service);

            System.out.println(
                    "Report saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to save report: "
                            + e.getMessage()
            );
        }
    }

    // ==================================================
    // INPUT METHODS
    // ==================================================

    private static int readInt(
            String message) {

        while (true) {

            System.out.print(message);

            String input =
                    scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid integer."
                );
            }
        }
    }

    private static String readString(
            String message) {

        while (true) {

            System.out.print(message);

            String input =
                    scanner.nextLine().trim();

            if (input.isEmpty()) {

                System.out.println(
                        "Input cannot be empty."
                );

                continue;
            }

            if (input.contains("|")) {

                System.out.println(
                        "Input cannot contain '|'."
                );

                continue;
            }

            return input;
        }
    }

    private static int readRating(
            String message) {

        while (true) {

            int value =
                    readInt(message);

            if (value >= 0 && value <= 10) {
                return value;
            }

            System.out.println(
                    "Rating must be between 0 and 10."
            );
        }
    }
}