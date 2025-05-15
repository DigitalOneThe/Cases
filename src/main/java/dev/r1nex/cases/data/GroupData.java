package dev.r1nex.cases.data;

import dev.r1nex.cases.data.interfaces.IAction;
import dev.r1nex.cases.data.interfaces.impl.GiveItemAction;
import dev.r1nex.cases.data.interfaces.impl.SendMessageAction;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GroupData {

    private final ItemStack itemStack;
    private final String displayName;
    private List<String> strings;
    private final List<IAction> actions;
    private double chance;


    public GroupData(ItemStack itemStack, String displayName, double chance, List<String> strings) {
        this.itemStack = itemStack;
        this.displayName = displayName;
        this.chance = chance;
        this.strings = strings;
        this.actions = createActions(strings);
    }

    private List<IAction> createActions(List<String> strings) {
        List<IAction> iActions = new ArrayList<>();
        for (String action : strings) {
            if (action.contains("[item]")) {
                iActions.add(new GiveItemAction());
            }

            if (action.contains("[message]")) {
                iActions.add(new SendMessageAction());
            }
        }

        return iActions;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public List<String> getActionStrings() {
        return strings;
    }

    public void setAction(List<String> strings) {
        this.strings = strings;
    }

    public List<IAction> getActions() {
        return actions;
    }
}
