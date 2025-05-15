package dev.r1nex.cases.animations;

import dev.r1nex.cases.Cases;
import dev.r1nex.cases.animations.interfaces.Animation;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WheelAnimation implements Animation {

    private final HashMap<Hologram, GroupData> holograms = new HashMap<>();
    private double radius = 0.0;
    private double rotationSpeed = 0.0;

    private final Cases plugin;

    public WheelAnimation(Cases plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setRadius(double value) {
        radius = value;
    }

    private GroupData getGroup(List<GroupData> groupData) {
        double totalChance = 0.0;
        for (GroupData group : groupData) {
            totalChance += group.getChance();
        }

        double randomChance = ThreadLocalRandom.current().nextDouble() * totalChance;
        double cumulativeChance = 0.0;
        for (GroupData group : groupData) {
            cumulativeChance += group.getChance();
            if (randomChance <= cumulativeChance) {
                return group;
            }
        }

        return groupData.get(0);
    }

    private void sync(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
    }

    @Override
    public void start(BlockData blockData, Player player, Location location, Block block, int points, List<GroupData> groups) {
        for (int i = 0; i < points; i++) {
            GroupData group = getGroup(groups);
            ItemStack itemStack = group.getItemStack();
            String displayName = group.getDisplayName();

            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location.clone());
            DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', displayName));
            DHAPI.addHologramLine(hologram, itemStack);
            holograms.put(hologram, group);
        }

        Location locationVertex = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
        locationVertex.setY(locationVertex.getY() + 1.5);

        final int totalTicks = 20 * 10;
        final Location center = location.clone();
        center.setYaw(180.0f);
        center.setPitch(-180.0f);
        final World world = center.getWorld(); // Кэшируем мир
        final double angleIncrement = 2 * Math.PI / holograms.size();
        final Queue<Hologram> hologramQueue = new LinkedList<>(holograms.keySet());

        new BukkitRunnable() {
            int calculateTicksAnimate = 0;
            int removeDelay = 0;
            double angleBase = 0.0;

            @Override
            public void run() {
                calculateTicksAnimate++;

                if (radius < 2.0) {
                    radius += 0.05;
                }

                if (!(calculateTicksAnimate >= totalTicks)) {
                    rotationSpeed = Math.min(0.1, rotationSpeed + 0.0005);
                }

                Color color = Color.fromRGB(
                        (int) (Math.sin(calculateTicksAnimate * 0.1) * 127 + 128),
                        (int) (Math.sin(calculateTicksAnimate * 0.1 + 2) * 127 + 128),
                        (int) (Math.sin(calculateTicksAnimate * 0.1 + 4) * 127 + 128)
                );

                angleBase += rotationSpeed;
                double angle = angleBase;

                for (Map.Entry<Hologram, GroupData> entry : holograms.entrySet()) {
                    final double x = radius * Math.cos(angle);
                    final double y = radius * Math.sin(angle);

                    Location target = center.clone().add(x, y + 0.1, 0);
                    target.setYaw(0.0f);
                    target.setPitch(-90.0f);

                    DHAPI.moveHologram(entry.getKey(), target);
                    if (calculateTicksAnimate % 4 == 0) {
                        world.spawnParticle(Particle.REDSTONE, target, 0, new Particle.DustOptions(color, 1));
                    }

                    angle += angleIncrement;
                }

                if (calculateTicksAnimate >= totalTicks) {
                    removeDelay ++;
                    if (removeDelay == 5) {
                        removeDelay = 0;
                        ProcessDeleteItems(hologramQueue);
                    }

                    if (hologramQueue.size() == 1) {
                        plugin.setChestOpened(block, true);
                        MoveHologramToCenter();

                        if (radius <= 0.0) {
                            sync(() -> ProcessReward(player, hologramQueue.element()));
                            completionOfAnimation(blockData, hologramQueue.poll());
                            cancel();
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    public void ProcessDeleteItems(Queue<Hologram> queue) {
        rotationSpeed = Math.max(0.0, rotationSpeed - 0.005);
        if (queue.size() != 1) {
            Hologram hologram = queue.poll();
            if (hologram == null) return;

            DHAPI.removeHologram(hologram.getName());
        }
    }

    public void MoveHologramToCenter() {
        radius = Math.max(0.0, radius - 0.1);
    }

    public void ProcessReward(Player player, Hologram lastHologram) {
        if (lastHologram == null) return;

        GroupData group = holograms.get(DHAPI.getHologram(lastHologram.getName()));
        if (group == null) return;
        for (String action : group.getActionStrings()) {
            if (action.contains("[message]")) {
                String result = action.replace("%player%", player.getName())
                        .replace("[message]", "").trim();
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', result)
                    );
                }
                continue;
            }

            String result = action.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    result
            );
        }
    }

    public void completionOfAnimation(BlockData blockData, Hologram lastHologram) {
        if (lastHologram == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DHAPI.removeHologram(lastHologram.getName());
            plugin.setChestOpened(blockData.getBlock(), false);
            blockData.setOpen(false);

            holograms.clear();
        }, 15L);
    }
}