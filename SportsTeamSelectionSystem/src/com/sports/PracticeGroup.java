package com.sports;

import java.util.Arrays;

public class PracticeGroup {

    private static final int SLOT_COUNT = 5;

    private int[] playerIds;

    public PracticeGroup() {
        playerIds = new int[SLOT_COUNT];
    }

    public int[] getPlayerIds() {
        return playerIds.clone();
    }

    public void loadPlayerIds(int[] ids) {
        if (ids == null || ids.length != SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Practice group must contain exactly 5 slots."
            );
        }

        this.playerIds = ids.clone();
    }

    public boolean assign(int slot, int playerId) {

        if (slot < 1 || slot > SLOT_COUNT) {
            return false;
        }

        if (playerId <= 0) {
            return false;
        }

        int index = slot - 1;

        // Same player already in this slot
        if (playerIds[index] == playerId) {
            return true;
        }

        // Player cannot occupy another slot
        for (int i = 0; i < playerIds.length; i++) {
            if (i != index && playerIds[i] == playerId) {
                return false;
            }
        }

        playerIds[index] = playerId;
        return true;
    }

    public boolean clear(int slot) {

        if (slot < 1 || slot > SLOT_COUNT) {
            return false;
        }

        playerIds[slot - 1] = 0;
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(playerIds);
    }
}