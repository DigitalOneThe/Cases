package dev.r1nex.cases.data.interfaces.impl;

import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.data.interfaces.IAction;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveItemAction implements IAction {

    @Override
    public void execute(Player player, GroupData groupData) {
        List<ItemStack> parseItems = parseItem(groupData.getActionStrings());
        parseItems.forEach(itemStack -> player.getInventory().addItem(itemStack));
    }

    private List<ItemStack> parseItem(List<String> actionData) {
        List<ItemStack> items = new ArrayList<>();

        for (String string : actionData) {
            String parseString = string.replace("[item]", "").trim();
            Bukkit.broadcast(Component.text(parseString));
            items.add(new ItemStack(Material.valueOf(parseString), 1));
        }

        return items;
    }
}
