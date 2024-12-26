package dev.r1nex.cases.animations;

import dev.r1nex.cases.Cases;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CircleAnimation {

    public void startAnimation(double x, double y, double z) {
        // Configuration variables
        final Location center = new Location(Bukkit.getWorld("world"), x, y, z); // Starting point
        final double radius = 1.5; // Radius of the circle
        final double height = 5; // Height of the spiral
        final int particleCount = 45; // Number of particles per circle
        final List<Material> itemMaterials = new ArrayList<>(); // List of items to display
        itemMaterials.add(Material.DIAMOND);
        itemMaterials.add(Material.GOLD_INGOT);
        itemMaterials.add(Material.EMERALD);
        itemMaterials.add(Material.BOOK);
        itemMaterials.add(Material.BOOK);
        itemMaterials.add(Material.BOOK);
        itemMaterials.add(Material.BOOK);
        itemMaterials.add(Material.BOOK);

        final int animationDuration = 200; // Duration in ticks (10 seconds)

        // Spawn items and store them in an array
        Item[] items = new Item[itemMaterials.size()];
        for (int i = 0; i < itemMaterials.size(); i++) {
            ItemStack itemStack = new ItemStack(itemMaterials.get(i));
            Item item = center.getWorld().dropItem(center, itemStack);
            item.setPickupDelay(Integer.MAX_VALUE); // Prevent item pickup
            item.setGravity(false); // Prevent item from falling
            items[i] = item;
        }

        // Start the animation task
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > animationDuration) {
                    // Remove items and cancel the task after the animation
                    for (Item item : items) {
                        item.remove();
                    }
                    cancel();
                    return;
                }

                // Calculate the spiral radius over time
                double currentRadius = (radius * ticks) / animationDuration;
                // Calculate the angle for the spiral
                double angleIncrement = Math.PI / 16;
                double angle = ticks * angleIncrement;

                // Create a color gradient
                Color color = Color.fromRGB(
                        (int) (Math.sin(ticks * 0.1) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 2) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 4) * 127 + 128)
                );

                // Generate particles in a circle
                for (int i = 0; i < particleCount; i++) {
                    double theta = 2 * Math.PI * i / particleCount;
                    double x = currentRadius * Math.cos(theta);
                    double y = (height * ticks) / animationDuration;
                    double z = currentRadius * Math.sin(theta);
                    Location particleLocation = center.clone().add(x, y, z);

                    // Spawn the particle with the color gradient
                    center.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            particleLocation,
                            0,
                            new Particle.DustOptions(color, 1)
                    );
                }

                // Move items along the circle
                for (int i = 0; i < items.length; i++) {
                    Item item = items[i];
                    double itemAngle = angle + (2 * Math.PI * i / items.length);
                    double x = radius * Math.cos(itemAngle);
                    double y = center.getY() + height;
                    double z = radius * Math.sin(itemAngle);
                    Location itemLocation = new Location(
                            center.getWorld(),
                            center.getX() + x,
                            y,
                            center.getZ() + z
                    );
                    item.teleport(itemLocation);
                }

                ticks++;
            }
        }.runTaskTimer(Cases.getInstance(), 0L, 1L); // Run the task every tick (20 times per second)
    }
}
