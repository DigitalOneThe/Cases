package dev.r1nex.cases.animations.interfaces;

import org.bukkit.Location;

public interface Effector {
    void setEffectRadius(double value);
    void setPause(boolean value);
    double getEffectRadius();
    boolean isPause();

    void start(Location center);
}
