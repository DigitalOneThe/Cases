package dev.r1nex.cases.data;

import dev.r1nex.cases.animations.interfaces.Animation;
import dev.r1nex.cases.animations.interfaces.Effector;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class BlockData {

    private final Block block;
    private final Location location;
    private Effector effector;
    private Animation animation;
    private final String name;
    private boolean isOpen = false;
    private List<GroupData> groupData;
    private Hologram hologram;
    private List<HistoryData> history;

    public BlockData(Block block, Location location, Effector effector, Animation animation, String name, List<GroupData> groupData, Hologram hologram) {
        this.block = block;
        this.location = location;
        this.effector = effector;
        this.animation = animation;
        this.name = name;
        this.groupData = groupData;
        this.hologram = hologram;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public Effector getEffector() {
        return effector;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation() {
        return location;
    }

    public void setEffector(Effector effector) {
        this.effector = effector;
    }

    public String getName() {
        return name;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public List<GroupData> getGroupData() {
        return groupData;
    }

    public void setGroupData(List<GroupData> groupData) {
        this.groupData = groupData;
    }

    public List<HistoryData> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryData> history) {
        this.history = history;
    }
}
