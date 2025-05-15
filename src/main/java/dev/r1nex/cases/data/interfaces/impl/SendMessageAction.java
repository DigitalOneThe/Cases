package dev.r1nex.cases.data.interfaces.impl;

import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.data.interfaces.IAction;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SendMessageAction implements IAction {

    @Override
    public void execute(Player player, GroupData groupData) {
        List<String> messages = parseMessage(groupData.getActionStrings());
        messages.forEach(s ->
                player.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', s)))
        );
    }

    private List<String> parseMessage(List<String> actionData) {
        List<String> messages = new ArrayList<>();

        for (String string : actionData) {
            String message = string.replace("[message]", "");
            messages.add(message);
        }

        return messages;
    }
}
