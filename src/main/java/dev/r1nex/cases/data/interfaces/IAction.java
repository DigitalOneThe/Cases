package dev.r1nex.cases.data.interfaces;

import dev.r1nex.cases.data.GroupData;
import org.bukkit.entity.Player;


public interface IAction {
    void execute(Player player, GroupData groupData);
}
