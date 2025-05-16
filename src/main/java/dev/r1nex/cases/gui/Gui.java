package dev.r1nex.cases.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.r1nex.cases.Cases;
import dev.r1nex.cases.data.BlockData;
import dev.r1nex.cases.data.GroupData;
import dev.r1nex.cases.data.HistoryData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Gui {
    private final Cases plugin;
    private final int size;
    private final String title;

    public Gui(Cases plugin, int size, String title) {
        this.plugin = plugin;
        this.size = size;
        this.title = title;
    }

    public void open(Player player) {
        ChestGui chestGui = new ChestGui(size, ChatColor.translateAlternateColorCodes('&', title));

        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();

        plugin.getBlocks().forEach((block, blockData) -> {
            List<Component> components = new ArrayList<>();

            String fileName = blockData.getName();
            YamlConfiguration yaml = plugin.getConfigs().getYaml(fileName);
            List<String> caseName = yaml.getStringList("case-name");
            caseName.forEach(s -> components.add(
                    Component.text(ChatColor.translateAlternateColorCodes('&', s)))
            );

            ItemStack itemStack = new ItemStack(Material.NAME_TAG);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.lore(components);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, inventoryClickEvent -> {
                if (inventoryClickEvent.isLeftClick()) showAllGroups(player, blockData.getGroupData());

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1);
                inventoryClickEvent.setCancelled(true);
            });

            items.add(guiItem);
        });
        pane.populateWithGuiItems(items);

        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void showAllGroups(Player player, List<GroupData> data) {
        ChestGui chestGui = new ChestGui(size, ChatColor.translateAlternateColorCodes('&', title));

        PaginatedPane pane = new PaginatedPane(1, 1, 7, 4);
        List<GuiItem> items = new ArrayList<>();

        data.forEach(blockData -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text(
                    ChatColor.translateAlternateColorCodes('&',
                            blockData.getDisplayName())
                    )
            );
            lore.add(Component.text(
                    ChatColor.translateAlternateColorCodes('&', "&7Шанс выпадения: "
                            + blockData.getChance()
                    )
            ));
            lore.add(Component.text(""));

            ItemStack itemStack = blockData.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.lore(lore);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.displayName(Component.text(""));
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, inventoryClickEvent -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1);
                inventoryClickEvent.setCancelled(true);
            });

            items.add(guiItem);
        });
        pane.populateWithGuiItems(items);

        chestGui.addPane(pane);
        chestGui.show(player);
    }

    public void showAccept(Player player, BlockData blockData, Block block, Location location) {
        ChestGui chestGui = new ChestGui(size, ChatColor.translateAlternateColorCodes('&', title));
        chestGui.setOnGlobalClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        PaginatedPane acceptPage = new PaginatedPane(1, 2, 3, 3);
        PaginatedPane cancelPage = new PaginatedPane(5, 2, 3, 3);
        List<GuiItem> items = new ArrayList<>();
        List<GuiItem> items1 = new ArrayList<>();

        ItemStack item = getItemStack(
                Material.WRITABLE_BOOK, "&7» &eНажмите, чтобы посмотреть содержимое этой коробки"
        );
        StaticPane navigation = new StaticPane(0, 0, 9, 5);

        navigation.addItem(new GuiItem(item, inventoryClickEvent ->
                showAllGroups(player, blockData.getGroupData())), 4, 0
        );

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.displayName(Component.text(
                    ChatColor.translateAlternateColorCodes('&', "&a&lОТКРЫТЬ"))
            );
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, inventoryClickEvent -> {
                if (blockData == null) {
                    return;
                }

                if (blockData.isOpen()) {
                    String message = plugin.getConfig().getString("messages.point-is-open");
                    assert message != null;
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    player.closeInventory();
                    return;
                }

                blockData.getAnimation().setRadius(0.0);

                if (blockData.getEffector() != null) {
                    blockData.getEffector().setPause(true);
                }

                blockData.getHologram().disable();
                blockData.getAnimation().start(
                        blockData, player, location, block, 8, blockData.getGroupData()
                );

                blockData.setOpen(true);

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1);
                player.closeInventory();
            });

            items.add(guiItem);
        }
        acceptPage.populateWithGuiItems(items);

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.displayName(Component.text(
                    ChatColor.translateAlternateColorCodes('&', "&c&lОТМЕНА"))
            );
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, inventoryClickEvent -> player.closeInventory());

            items1.add(guiItem);
        }
        cancelPage.populateWithGuiItems(items1);

        chestGui.addPane(acceptPage);
        chestGui.addPane(cancelPage);
        chestGui.addPane(navigation);
        populateHistoryItems(chestGui, new PaginatedPane(0, 5, 9, 1), blockData);
        chestGui.show(player);
    }

    public ItemStack getItemStack(Material material, String displayName) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', displayName)));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public void populateHistoryItems(ChestGui chestGui, PaginatedPane pane, BlockData blockData) {
        List<GuiItem> items = new ArrayList<>();
        for (HistoryData data : blockData.getHistory()) {
            if (data == null) continue;
            Player player = data.getPlayer();
            GroupData group = data.getGroup();
            List<Component> components = new ArrayList<>();
            List<String> lore = plugin.getConfigs().getYaml(blockData.getName()).getStringList("history-string");

            lore.forEach(s -> {
                String replace = s
                        .replace("%player%", player.getName())
                        .replace("%group%", group.getDisplayName());
                components.add(Component.text(ChatColor.translateAlternateColorCodes('&', replace)));
            });

            ItemStack itemStack = new ItemStack(data.getGroup().getItemStack().getType());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(""));
            itemMeta.lore(components);
            itemStack.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(itemStack, inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
            items.add(guiItem);
        }

        pane.populateWithGuiItems(items);
        chestGui.addPane(pane);
    }
}
