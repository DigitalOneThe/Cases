package dev.r1nex.cases;

import com.comphenix.protocol.wrappers.BlockPosition;
import dev.r1nex.cases.animations.EffectorCube;
import dev.r1nex.cases.animations.WheelAnimation;
import dev.r1nex.cases.animations.interfaces.Animation;
import dev.r1nex.cases.animations.interfaces.Effector;
import dev.r1nex.cases.command.CaseCommand;
import dev.r1nex.cases.config.YamlConfig;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.data.HistoryData;
import dev.r1nex.cases.listeners.Listeners;
import dev.r1nex.cases.wrappers.WrapperPlayServerBlockAction;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Cases extends JavaPlugin {
    private final HashMap<Block, BlockData> blocks = new HashMap<>();
    private static Cases instance;
    private YamlConfig yamlConfig;

    @Override
    public void onEnable() {

        new BukkitRunnable() {
            int ticks = 72_000;

            @Override
            public void run() {
                ticks --;
                if (ticks <= 0) {
                    blocks.forEach((block, blockData) -> blockData.getHistory().clear());
                    ticks = 72_000;
                }
            }
        }.runTaskTimerAsynchronously(this, 1L, 1L);

        // Plugin startup logic
        instance = this;
        new CaseCommand(this);
        yamlConfig = new YamlConfig(this);
        yamlConfig.loadYaml("other-configs/donate-case.yml");
        yamlConfig.loadYaml("other-configs/vault-case.yml");

        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        List<String> points = getConfig().getStringList("opening-points");
        points.forEach(string -> {
            String[] split = string.split(";");
            String world = split[0];
            double x = Double.parseDouble(split[1]);
            double y = Double.parseDouble(split[2]);
            double z = Double.parseDouble(split[3]);
            String boxName = split[4];
            String effectorName = split[5];

            Location location = new Location(Bukkit.getWorld(world), x, y, z);
            Block block = location.getBlock();

            Effector effector = null;

            switch (effectorName) {
                case "NONE": {
                    break;
                }

                case "effector-cube": {
                    effector = new EffectorCube();
                }

                default:
                    break;
            }

            Animation animation = new WheelAnimation(this);
            YamlConfiguration yaml = yamlConfig.getYaml(boxName);
            if (yaml == null) {
                getLogger().config("File: " + boxName + " not loading. \n" +
                        "This file does not exist in the 'other-configs' directory"
                );
                return;
            }

            List<GroupData> groupData = new ArrayList<>();
            ConfigurationSection section = yaml.getConfigurationSection("items");

            assert section != null;

            Location hologramLoc = location.clone();
            hologramLoc.setY(location.getY() + 3.0);

            Hologram hologram = DHAPI.createHologram(
                    UUID.randomUUID().toString(),
                    hologramLoc
            );
            List<String> about = yaml.getStringList("case-name");
            about.forEach(s ->
                    DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', s))
            );

            for (String key : section.getKeys(false)) {
                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString(key + ".item")));
                String displayName = section.getString(key + ".display-name");
                List<String> action = section.getStringList(key + ".action");
                int chance = section.getInt(key + ".chance");

                GroupData data = new GroupData(itemStack, displayName, chance, action);
                groupData.add(data);
            }

            BlockData blockData = new BlockData(block, location, effector, animation, boxName, groupData, hologram);
            blockData.setHistory(new ArrayList<>());
            blocks.put(block, blockData);

            location.setX(location.getX());
            location.setY(location.getY());
            location.setZ(location.getZ());
            location.setYaw(-90);
            location.setPitch(90);

            if (effector != null) {
                effector.setEffectRadius(1.5);
                effector.start(location);
            }
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Cases getInstance() {
        return instance;
    }

    public HashMap<Block, BlockData> getBlocks() {
        return blocks;
    }

    public YamlConfig getConfigs() {
        return yamlConfig;
    }

    public void setChestOpened(Block block, boolean opened) {
        WrapperPlayServerBlockAction packet = new WrapperPlayServerBlockAction();
        packet.setLocation(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        packet.setByte1(1);
        packet.setByte2(opened ? 1 : 0);
        packet.setBlockType(block.getType());
        int distanceSquared = 64 * 64;
        Location loc = block.getLocation();
        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) < distanceSquared) {
                packet.sendPacket(player);
            }
        }
    }
}
