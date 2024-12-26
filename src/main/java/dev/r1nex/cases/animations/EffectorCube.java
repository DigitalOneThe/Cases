package dev.r1nex.cases.animations;

import dev.r1nex.cases.Cases;
import dev.r1nex.cases.animations.interfaces.Effector;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectorCube implements Effector {

    private double effectorRadius = 0.0;
    private boolean isPause = false;

    @Override
    public void setEffectRadius(double value) {
        effectorRadius = value;
    }

    @Override
    public void setPause(boolean value) {
        isPause = value;
    }

    @Override
    public double getEffectRadius() {
        return effectorRadius;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void start(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            final Location center = location.clone();
            // Размер куба
            final double size = getEffectRadius();
            final double halfSize = size / 2;

            final double[][] vertices = {
                    { -halfSize, -halfSize, -halfSize },
                    { halfSize, -halfSize, -halfSize },
                    { halfSize, halfSize, -halfSize },
                    { -halfSize, halfSize, -halfSize },
                    { -halfSize, -halfSize, halfSize },
                    { halfSize, -halfSize, halfSize },
                    { halfSize, halfSize, halfSize },
                    { -halfSize, halfSize, halfSize }
            };

            @Override
            public void run() {
                if (isPause()) return;

                final Color color = Color.fromRGB(
                        (int) (Math.sin(ticks * 0.1) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 2) * 127 + 128),
                        (int) (Math.sin(ticks * 0.1 + 4) * 127 + 128)
                );
                final double angle = ticks * 0.05;
                ticks++;

                for (double[] vertex : vertices) {
                    final double x = vertex[0] * Math.cos(angle) - vertex[2] * Math.sin(angle);
                    final double z = vertex[0] * Math.sin(angle) + vertex[2] * Math.cos(angle);

                    final Location particleLocation = center.clone().add(x, vertex[1], z);

                    center.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            particleLocation,
                            0,
                            new Particle.DustOptions(color, 1)
                    );
                }
            }
        }.runTaskTimerAsynchronously(Cases.getInstance(), 10L, 3L);
    }
}
