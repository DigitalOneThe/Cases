package dev.r1nex.cases.data;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GroupData {

    private final ItemStack itemStack;
    private final String displayName;
    private List<String> action;
    private int chance;


    public GroupData(ItemStack itemStack, String displayName, int chance, List<String> action) {
        this.itemStack = itemStack;
        this.displayName = displayName;
        this.chance = chance;
        this.action = action;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }
}
