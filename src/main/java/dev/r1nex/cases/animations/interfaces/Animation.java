package dev.r1nex.cases.animations.interfaces;

import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface Animation {

    void setSpeed(double value);
    void setRadius(double value);
    double getSpeed();
    void start(BlockData blockData, Player player, Location location, Block block, int points, List<GroupData> groups);
}
