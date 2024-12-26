package dev.r1nex.cases.listeners;

import dev.r1nex.cases.Cases;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.gui.Gui;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Listeners implements Listener {
    private final Cases plugin;

    public Listeners(Cases plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void OnBreakBlock(BlockBreakEvent event) {
        BlockData blockData = plugin.getBlocks().get(event.getBlock());
        if (blockData != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Block block = event.getClickedBlock();
        if (!plugin.getBlocks().containsKey(block)) return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Gui gui = new Gui(plugin, 6, "&6Коробки");
            gui.showAllGroups(event.getPlayer(), plugin.getBlocks().get(block).getGroupData());
            return;
        }

        assert block != null;
        Location location = block.getLocation();
        location.setX(location.getX() + 0.5);
        location.setY(location.getY() + 1.4);
        location.setZ(location.getZ() + 0.5);

        location.setYaw(-90);
        location.setPitch(90);

        BlockData blockData = plugin.getBlocks().get(block);
        if (blockData == null) return;

        if (blockData.isOpen()) {
            String message = plugin.getConfig().getString("messages.point-is-open");
            assert message != null;
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            event.setCancelled(true);
            return;
        }

        Gui gui = new Gui(plugin, 6, "&eОткрыть коробку?");
        gui.showAccept(event.getPlayer(), blockData, block, location);

        event.setCancelled(true);
    }
}
