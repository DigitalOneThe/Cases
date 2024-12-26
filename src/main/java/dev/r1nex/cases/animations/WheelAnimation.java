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

    @Override
    public void start(BlockData blockData, Player player, Location location, Block block, int points, List<GroupData> groups) {
        for (int i = 0; i < points; i++) {
            int index = ThreadLocalRandom.current().nextInt(groups.size());
            GroupData group = groups.get(index);
            ItemStack itemStack = group.getItemStack();
            String displayName = group.getDisplayName();

            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location.clone());
            DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', displayName));
            DHAPI.addHologramLine(hologram, itemStack);
            holograms.put(hologram, group);
        }

        Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
        loc.setY(loc.getY() + 1.0);

        final int totalTicks = 20 * 10;
        final Location center = location.clone();

        new BukkitRunnable() {
            int ticks = 0;

            int removeDelay = 0;

            final Queue<Hologram> hologramQueue = new LinkedList<>(holograms.keySet());
            final double angleIncrement = 2 * Math.PI / holograms.size();

            @Override
            public void run() {
                if (!(radius >= 2.0)) {
                    radius += 0.05;
                }

                final Color color = Color.fromRGB(
                        (int) (Math.sin(ticks * 0.1) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 2) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 4) * 127 + 128)
                );
                double angle = (ticks * speed / 20.0) % 360;
                angle = Math.toRadians(angle);

                for (Map.Entry<Hologram, GroupData> entry : holograms.entrySet()) {
                    final double x = radius * Math.cos(angle);
                    final double y = radius * Math.sin(angle);

                    final Location target = center.clone().add(
                            center.getDirection().multiply(x).add(new Vector(0, 0, 1)
                                    .multiply(y).add(new Vector(0, -0.5, 0)))
                    );

                    DHAPI.moveHologram(entry.getKey(), target);

                    center.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            target,
                            0,
                            new Particle.DustOptions(color, 1)
                    );

                    angle += angleIncrement;
                }

                ticks++;
                if (ticks >= totalTicks) {
                    Bukkit.getScheduler().runTaskTimer(Cases.getInstance(), bukkitTask -> {
                        removeDelay++;

                        if (removeDelay >= 1) {
                            setSpeed(getSpeed() - 0.01);
                        }

                        if (hologramQueue.size() > 1 && removeDelay >= 2) {
                            Hologram hologram = hologramQueue.remove();
                            DHAPI.removeHologram(hologram.getName());
                            removeDelay = 0;
                        } else {
                            final Hologram hologram = hologramQueue.element();
                            final Location hologramLoc = hologram.getLocation();
                            final double distanceToCenter = hologramLoc.distance(center);

                            if (distanceToCenter < 0.01) {
                                Bukkit.getScheduler().runTaskTimer(Cases.getInstance(), (remove) -> {
                                    GroupData group = holograms.get(DHAPI.getHologram(hologram.getName()));
                                    if (group == null) return;
                                    for (String action : group.getAction()) {
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

                                    if (blockData == null) return;
                                    if (blockData.getEffector() != null) blockData.getEffector().setPause(false);

                                    blockData.setOpen(false);
                                    plugin.setChestOpened(block, false);
                                    blockData.getHologram().enable();
                                    blockData.getHistory().add(new HistoryData(player, group));
                                    DHAPI.removeHologram(hologram.getName());
                                    holograms.clear();
                                    hologramQueue.clear();
                                    remove.cancel();
                                }, 40L, 0L);

                                bukkitTask.cancel();
                                cancel();
                                return;
                            }

                            final double distanceItemToItem = hologramLoc.distance(loc);

                            final double coefficient = 0.1;
                            final double lendingSpeed = Math.max(0.01, coefficient / (distanceItemToItem + 1));

                            Vector direction = center.toVector().subtract(hologramLoc.toVector()).normalize();
                            hologramLoc.add(direction.multiply(lendingSpeed));
                            DHAPI.moveHologram(hologram, hologramLoc);
                            plugin.setChestOpened(block, true);
                        }
                    }, 0L, 1);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}