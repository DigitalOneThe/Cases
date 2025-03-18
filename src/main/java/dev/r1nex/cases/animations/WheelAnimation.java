package dev.r1nex.cases.animations;

import dev.r1nex.cases.Cases;
import dev.r1nex.cases.animations.interfaces.Animation;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.data.HistoryData;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WheelAnimation implements Animation {

    private final HashMap<Hologram, GroupData> holograms = new HashMap<>();
    private double speed = 0.0;
    private double radius = 0.0;

    private final Cases plugin;

    public WheelAnimation(Cases plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setSpeed(double value) {
        speed = value;
    }

    @Override
    public void setRadius(double value) {
        radius = value;
    }

    @Override
    public double getSpeed() {
        return speed;
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

    @Override
    public void start(BlockData blockData, Player player, Location location, Block block, int points, List<GroupData> groups) {
        // Инициализация голограмм (оставляем как есть)
        for (int i = 0; i < points; i++) {
            GroupData group = getGroup(groups);
            ItemStack itemStack = group.getItemStack();
            String displayName = group.getDisplayName();

            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location.clone());
            DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', displayName));
            DHAPI.addHologramLine(hologram, itemStack);
            holograms.put(hologram, group);
        }

        Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
        loc.setY(loc.getY() + 1.5);

        final int totalTicks = 20 * 10; // 10 секунд
        final Location center = location.clone();
        final double angleIncrement = 2 * Math.PI / holograms.size();
        final Queue<Hologram> hologramQueue = new LinkedList<>(holograms.keySet());

        new BukkitRunnable() {
            int ticks = 0;
            double radius = 0.0;

            @Override
            public void run() {
                if (ticks < totalTicks) {
                    // Фаза вращения
                    if (radius < 2.0) radius += 0.05;

                    double angle = (ticks * speed / 20.0) % 360;
                    angle = Math.toRadians(angle);

                    for (Hologram hologram : holograms.keySet()) {
                        double x = radius * Math.cos(angle);
                        double y = radius * Math.sin(angle);
                        Location target = center.clone().add(x, 0, y);
                        DHAPI.moveHologram(hologram, target);

                        // Частицы (оставляем как есть)
                        Color color = Color.fromRGB(
                                (int) (Math.sin(ticks * 0.1) * 127 + 128),
                                (int) (Math.sin(ticks * 0.1 + 2) * 127 + 128),
                                (int) (Math.sin(ticks * 0.1 + 4) * 127 + 128)
                        );
                        center.getWorld().spawnParticle(
                                Particle.REDSTONE,
                                target,
                                0,
                                new Particle.DustOptions(color, 1)
                        );

                        angle += angleIncrement;
                    }
                    ticks++;
                } else {
                    // Фаза притягивания
                    Bukkit.getScheduler().runTaskTimer(Cases.getInstance(), task -> {
                        if (hologramQueue.isEmpty()) {
                            task.cancel();
                            return;
                        }

                        Hologram hologram = hologramQueue.peek();
                        Location hologramLoc = hologram.getLocation();
                        double distanceToCenter = hologramLoc.distance(center);

                        if (distanceToCenter < 0.05) {
                            // Завершение анимации
                            finishAnimation(blockData, player, block, hologram, hologramQueue, task);
                        } else {
                            // Плавное притягивание
                            Vector direction = center.toVector().subtract(hologramLoc.toVector()).normalize();
                            double pullDistance = distanceToCenter; // Расстояние до центра

                            // Нелинейная скорость: чем ближе к центру, тем медленнее
                            double pullSpeed = Math.min(0.1, 0.05 * pullDistance * pullDistance); // Квадратичное замедление
                            hologramLoc.add(direction.multiply(pullSpeed));
                            DHAPI.moveHologram(hologram, hologramLoc);
                        }
                    }, 0L, 1L);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void finishAnimation(BlockData blockData, Player player, Block block, Hologram hologram, Queue<Hologram> hologramQueue, BukkitTask task) {
        GroupData group = holograms.get(hologram);
        if (group != null) {
            for (String action : group.getAction()) {
                if (action.contains("[message]")) {
                    String result = action.replace("%player%", player.getName()).replace("[message]", "").trim();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
                    }
                } else {
                    String result = action.replace("%player%", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), result);
                }
            }
        }

        DHAPI.removeHologram(hologram.getName());
        hologramQueue.poll();
        holograms.remove(hologram);

        if (blockData != null) {
            if (blockData.getEffector() != null) blockData.getEffector().setPause(false);
            blockData.setOpen(false);
            plugin.setChestOpened(block, false);
            blockData.getHologram().enable();
            blockData.getHistory().add(new HistoryData(player, group));
        }

        if (hologramQueue.isEmpty()) {
            task.cancel();
        }
    }
}