package dev.r1nex.cases.data;

import org.bukkit.entity.Player;

public class HistoryData {
    private final Player player;
    private final GroupData group;

    public HistoryData(Player player, GroupData group) {
        this.player = player;
        this.group = group;
    }

    public Player getPlayer() {
        return player;
    }

    public GroupData getGroup() {
        return group;
    }
}
