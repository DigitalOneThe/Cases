package dev.r1nex.cases.command;

import dev.r1nex.cases.Cases;
import dev.r1nex.cases.animations.WheelAnimation;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.gui.Gui;
import dev.r1nex.cases.animations.interfaces.Animation;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CaseCommand extends AbstractCommand {

    private final Cases plugin;

    public CaseCommand(Cases plugin) {
        super(plugin, "cases");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) {
            String message = plugin.getConfig().getString("system-messages.use-console");
            assert message != null;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (args.length < 1) {
            List<String> message = plugin.getConfig().getStringList("available-commands");
            message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            return;
        }

        switch (args[0]) {
            case "start": {
                List<String> message = plugin.getConfig().getStringList("available-commands");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                return;
            }

            case "editor": {
                Gui gui = new Gui(plugin, 6, "&6Коробки");
                gui.open(((Player) sender).getPlayer());
                return;
            }

            case "select-mode": {
                if (args.length < 2) {
                    List<String> message = plugin.getConfig().getStringList("available-commands");
                    message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
                    return;
                }

                if (args[1].equalsIgnoreCase("point-open")) {
                    if (args.length < 3) {
                        List<String> message = plugin.getConfig().getStringList("available-commands");
                        message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                s))
                        );
                        return;
                    }

                    String result = String.join("", args[2].split(" "));
                    Block block = Objects.requireNonNull(((Player) sender).getPlayer()).getTargetBlock(50);

                    assert block != null;
                    Location location = block.getLocation();
                    Animation animation = new WheelAnimation(plugin);

                    YamlConfiguration yaml = plugin.getConfigs().getYaml(result);
                    if (yaml == null) {
                        String error = plugin.getConfig().getString("system-messages.error");
                        assert error != null;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', error));

                        plugin.getLogger().config("File: " + result + " not loading. \n" +
                                "This file does not exist in the 'other-configs' directory"
                        );
                        return;
                    }

                    List<GroupData> groupData = new ArrayList<>();
                    ConfigurationSection section = yaml.getConfigurationSection("items");

                    assert section != null;

                    for (String key : section.getKeys(false)) {
                        ItemStack itemStack = new ItemStack(Material.valueOf(section.getString(key + ".item")));
                        String displayName = section.getString(key + ".display-name");
                        List<String> action = section.getStringList(key + ".action");
                        double chance = section.getDouble(key + ".chance");

                        GroupData data = new GroupData(itemStack, displayName, chance, action);
                        groupData.add(data);
                    }

                    Location hologramLoc = new Location(((Player) sender).getWorld(), location.getX() + 0.5, location.getY() + 3.5, location.getZ() + 0.5);

                    Hologram hologram = DHAPI.createHologram(
                            UUID.randomUUID().toString(),
                            hologramLoc
                    );

                    List<String> about = yaml.getStringList("case-name");
                    about.forEach(s ->
                            DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', s))
                    );

                    BlockData blockData = new BlockData(
                            block, location, null, animation, result, groupData, hologram
                    );
                    blockData.setHistory(new ArrayList<>());

                    plugin.getBlocks().put(block, blockData);
                    location.setX(location.getX() + 0.5);
                    location.setY(location.getY() + 0.5);
                    location.setZ(location.getZ() + 0.5);

                    String message = plugin.getConfig().getString("messages.point-create");
                    assert message != null;
                    ((Player) sender).getPlayer().sendMessage(
                            ChatColor.translateAlternateColorCodes('&', message)
                    );

                    List<String> openingPoints = plugin.getConfig().getStringList("opening-points");
                    String newPoint = block.getWorld().getName() + ";"
                            + location.getX() + ";"
                            + location.getY() + ";"
                            + location.getZ() + ";"
                            + result + ";"
                            + "NONE";

                    openingPoints.add(newPoint);

                    plugin.getConfig().set("opening-points", openingPoints);
                    plugin.saveConfig();
                }
                return;
            }

            default: {
                List<String> message = plugin.getConfig().getStringList("available-commands");
                message.forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            }
        }
    }

    @Override
    public List<String> completer(CommandSender sender, Command command, String[] args) {
        if (args.length <= 1) {
            return Arrays.asList("start", "select-mode point-open|editor");
        }

        return null;
    }
}
