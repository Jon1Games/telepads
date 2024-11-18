package de.jonas.telepads.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import de.jonas.stuff.Stuff;
import de.jonas.stuff.interfaced.ClickEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.stuff.utility.PagenationInventory;
import de.jonas.stuff.utility.UseNextChatInput;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.commands.GiveBuildItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TelepadGui implements InventoryHolder{

    MiniMessage mm = MiniMessage.miniMessage();
    Telepads telepads = Telepads.INSTANCE;
    DataBasePool db = telepads.basePool;
    Location location, destination;
    public int id;
    UUID owner;
    public int level;
    public Inventory telepadGui;
    String destiName;

    public static final NamespacedKey desti = new NamespacedKey("telepads", "destination");
    public static final NamespacedKey src = new NamespacedKey("telepads", "source");

    static ClickEvent changeName = TelepadGui::changeNameI;
    static ClickEvent pickUp = TelepadGui::pickUpI;
    static ClickEvent openDestinationGui = TelepadGui::openDestinationGuiI;
    static ClickEvent levelUp = TelepadGui::levelUpI;

    public TelepadGui(Player p, int idC) {

        id = idC;
        location = DataBasePool.getlocation(db, id);
        String name = DataBasePool.getName(db, id);
        owner = DataBasePool.getOwner(db, id);
        level = DataBasePool.getLevel(db, id);
        destiName = DataBasePool.getDestinationName(db, id);
        destination = DataBasePool.getDestination(db, id);
        List<Component> lore = new ArrayList<>();
        if (destination != null) {
            lore.add(mm.deserialize("<white>Name: " + destiName + "</white>").decoration(TextDecoration.ITALIC,false));
            lore.add(mm.deserialize("<white>Welt: " + destination.getWorld().getName() + "</white>").decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>X: " + destination.getBlockX() + "</white>").decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>Y: " + destination.getBlockY() + "</white>").decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>Z: " + destination.getBlockZ() + "</white>").decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(mm.deserialize("Setze das Ziel fest."));
        }
        

        this.telepadGui = Bukkit.createInventory(this, (3 * 9), Component.text(name));

        Stuff.INSTANCE.itemBuilderManager.addClickEvent(changeName, "telepads:click_change_name");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(pickUp, "telepads:pick_telepad_up");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(openDestinationGui, "telepad:open_initial_destination_gui");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(levelUp, "telepad:pad_level_up");

        int[] place = {0,1,2,3,5,6,7,8,9,11,13,15,17,18,19,20,21,23,24,25,26};
        for (int a : place) {
            telepadGui.setItem(a,
                new ItemBuilder()
                    .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                    .setName("")
                    .whenClicked("telepads:cancelevent")
                    .build()    
            );
        }

        telepadGui.setItem(4, 
            new ItemBuilder()
            .setMaterial(Material.NAME_TAG)
            .setName(name)
            .addLoreLine("Klicken Namen ändern.")
            .whenClicked("telepads:click_change_name")
            .build()
        );

        telepadGui.setItem(14,
            new ItemBuilder()
                .setMaterial(Material.GRASS_BLOCK)
                .setName("Ziel")
                .setLore(lore)
                .whenClicked("telepad:open_initial_destination_gui")
                .build()
        );

        telepadGui.setItem(10, 
            new ItemBuilder()
                .setMaterial(Material.RED_DYE)
                .setName("<red>Aufheben</red>")
                .whenClicked("telepads:pick_telepad_up")
                .build()
        );

        telepadGui.setItem(12, 
            new ItemBuilder()
                .setMaterial(Material.ENDER_EYE)
                .setName("Sichtbarkeit")
                .whenClicked("telepads:open_publish_gui")
                .build()
        );

        telepadGui.setItem(22, 
        new ItemBuilder()
            .setMaterial(Material.BARRIER)
            .setName("<red>Schließen</red>")
            .whenClicked("telepads:closeinv")
            .build()
        );

        String levelLore;
        if (level == 1) {
            levelLore = "Das Aufwerten kostet 200 Coins.";
        } else {
            levelLore = "";
        }

        telepadGui.setItem(16,
            new ItemBuilder()
                .setMaterial(Material.EMERALD)
                .setName("Aufwerten")
                .addLoreLine("Level: " + level)
                .addLoreLine(levelLore)
                .whenClicked("telepad:pad_level_up")
                .build()
        );

    }

    private static void changeNameI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        DataBasePool db = telepads.basePool;
        e.getWhoClicked().closeInventory();
        TelepadGui gui = (TelepadGui) e.getInventory().getHolder(false);
        new UseNextChatInput((Player) e.getWhoClicked())
            .sendMessage("Wie möchtest du dein Telepad nennen?.<br>Schreibe \"exit\" oder \"abbrechen\" um den Vorgang abzubrechen.")
            .setChatEvent((player, message) -> {
                if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("abbrechen")) {
                    player.sendMessage("Abgebrochen");
                    return;
                }

                Pattern ptm = Pattern.compile("[a-zA-Z0-9_ ]{3,32}");
                if(ptm.matcher(message).matches()) {
                    DataBasePool.setName(db, gui.id, message);
                    player.sendMessage(mm.deserialize("Dein Telepad wurde \"<green><name></green>\" genannt.",
                    Placeholder.component("name", Component.text(message))));
                } else {
                    player.sendMessage(mm.deserialize("<red>Dieser Name ist ungültig <br>Der Name darf nur die Zeichen [a-zA-Z0-9_ ] verwenden <br> und muss zwischen 3 und 32 Zeichen lang sein.</red>"));
                }
            })
            .capture();
    }

    private static void pickUpI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        DataBasePool db = telepads.basePool;
        TelepadGui gui = (TelepadGui) e.getInventory().getHolder(false);
        UUID owner = DataBasePool.getOwner(db, gui.id);
        Location location = DataBasePool.getlocation(db, gui.id);
		if (e.getWhoClicked().getUniqueId().equals(owner) || e.getWhoClicked().hasPermission("telepads.admin")) {
            if (e.getWhoClicked().getInventory().firstEmpty() == -1) {
                e.getWhoClicked().sendMessage(mm.deserialize("<red>Dein Inventar ist voll, bitte lerre es um das Telepad aufzusammeln</red>"));
                return;
            }
            e.getWhoClicked().closeInventory();
            DataBasePool.removeTelepadsDestinationWithDestination(db, gui.id);
            DataBasePool.removeTelepad(db, gui.id);
            location.clone().add(0.5,2.5,0.5).getNearbyEntitiesByType(TextDisplay.class, 0.1).forEach(display -> {
                PersistentDataContainer persis = display.getPersistentDataContainer();
                if (!persis.has(GiveBuildItem.telepadNum)) return;
                if (persis.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER) == gui.id) display.remove();
            });
            location.getWorld().setType(location, Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, 1), Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, 0), Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(0, 0, 1), Material.AIR);
            location.getWorld().setType(location.clone().add(0, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, 0), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, 1), Material.AIR);
            e.getWhoClicked().getInventory().addItem(
                new ItemBuilder()
                    .setMaterial(Material.BEACON)
                    .setName("Telepad")
                    .whenPlaced("telepads:buildTelepad")
                    .build()
            );
            if (gui.level == 2) {
                Telepads.getEconomy().depositPlayer((OfflinePlayer) e.getWhoClicked(), 200);
                e.getWhoClicked().sendMessage(mm.deserialize("Dir wurden <green>200 Coins</green> gutgeschrieben."));
            }
            e.getWhoClicked().sendMessage(mm.deserialize("<green>Du hast das Telepad erfolgreich augehoben.</green>"));
        } else {
            e.getWhoClicked().sendMessage(mm.deserialize("<red>Du bist nicht der Besitzer dieses Telepads!</red>"));
        }
	}

    private static void openDestinationGuiI(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof TelepadGui tg) {
            e.setCancelled(true);
            DataBasePool db = Telepads.INSTANCE.basePool;
            List<Integer> pads = DataBasePool.getTelepadsIFPermission(db, e.getWhoClicked().getUniqueId(), tg.id);
            List<ItemStack> list = new ArrayList<>();
            for (int a : pads) {
                String name = DataBasePool.getName(db, a);
                ItemStack item = new ItemBuilder()
                    .setMaterial(Material.BEACON)
                    .setName(Component.text(name))
                    .whenClicked("telepads:select_telepad_destination")
                    .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(src, PersistentDataType.INTEGER, tg.id);
                meta.getPersistentDataContainer().set(desti, PersistentDataType.INTEGER, a);
                item.setItemMeta(meta);
                list.add(item);
            }
            e.getWhoClicked().openInventory(
                new PagenationInventory(list).getInventory()
                );
        }
    }

    private static void levelUpI(InventoryClickEvent e) {
        e.setCancelled(true);
        MiniMessage mm = MiniMessage.miniMessage();
        DataBasePool db = Telepads.INSTANCE.basePool;
        OfflinePlayer p = (OfflinePlayer) e.getWhoClicked();
        if (Telepads.getEconomy() == null) {
            e.getWhoClicked().sendMessage("You shoud not get this, kontakt an admin (Line 258, TelepadGUI.java)");  
            return;
        }
        if (Telepads.getEconomy().getBalance(p) >= 200d && p instanceof Player player && e.getInventory().getHolder() instanceof TelepadGui tg) {
            DataBasePool.setLevel2(db, tg.id);
            tg.level++;
            Telepads.getEconomy().withdrawPlayer(player, 200d);
            tg.telepadGui.setItem(16,
                new ItemBuilder()
                    .setMaterial(Material.EMERALD)
                    .setName("Aufwerten")
                    .addLoreLine("Level: " + tg.level)
                    .whenClicked("telepad:pad_level_up")
                    .build()
            );
            player.sendMessage(mm.deserialize("Dein Telepad wurde aufgewertet."));
        } else if (p instanceof Player player) {
            player.sendMessage(mm.deserialize("<red>Du hast nicht genügend Geld.</red>"));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return telepadGui;
    }

}
