package de.jonas.telepads;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.*;
import de.jonas.stuff.Stuff;
import de.jonas.stuff.interfaced.ClickEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.stuff.utility.PagenationInventory;
import de.jonas.stuff.utility.UseNextChatInput;
import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.gui.PublishGUI;
import de.jonas.telepads.gui.TelepadGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class Events {
    
    public static final NamespacedKey teleID= new NamespacedKey("telepads", "telepad_id");

    public static final ClickEvent closeInv = Events::closeInvI;
    public static final ClickEvent cancelEvent = Events::cancelEventI;
    public static final ClickEvent selectTelepadLocation = Events::selectTelepadLocationI;
    public static final ClickEvent openPublishGUI = Events::openPublishGUII;
    public static final ClickEvent listPermittetPlayers = Events::listPermittetPlayersI;
    public static final ClickEvent removePlayerPermission = Events::removePlayerPermissionI;
    public static final ClickEvent addPlayerPermission = Events::addPlayerPermissionI;
    public static final ClickEvent publishToEveryone = Events::publishToEveryoneI;
    public static final ClickEvent openTelepadGUI = Events::openTelepadGUII;

    public Events() {
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(closeInv, "telepads:closeinv");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(cancelEvent, "telepads:cancelevent");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(selectTelepadLocation, "telepads:select_telepad_destination");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(openPublishGUI, "telepads:open_publish_gui");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(listPermittetPlayers, "telepads:list_permittet_player");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(removePlayerPermission, "telepads:remove_permittet_player");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(addPlayerPermission, "telepads:add_permittet_player");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(publishToEveryone, "telepads:publish_to_everyone");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(openTelepadGUI, "telepads:open_telepad_gui");
    }

    private static void closeInvI(InventoryClickEvent e) {
		e.setCancelled(true);
        e.getWhoClicked().closeInventory();
	}

    private static void cancelEventI(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public static void selectTelepadLocationI(InventoryClickEvent e) {
        DataBasePool db = Telepads.INSTANCE.basePool;
        MiniMessage mm = MiniMessage.miniMessage();
        e.setCancelled(true);
        PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        int idsource = container.get(TelepadGui.src, PersistentDataType.INTEGER);
        int id = container.get(TelepadGui.desti, PersistentDataType.INTEGER);
    
        DataBasePool.setNewDestinationID(db, idsource, id);
        Component name = Component.text(DataBasePool.getName(db, id));
        e.getWhoClicked().sendMessage(mm.deserialize("Du hast \"<desti>\" erfolgreich als Ziel gesetzt.",
        Placeholder.component("desti", name)));
        Location l = DataBasePool.getlocation(db, idsource);
        l.add(0.5,2.5,0.5);
        l.getNearbyEntitiesByType(TextDisplay.class, 0.1).forEach(display -> {
            PersistentDataContainer persis = display.getPersistentDataContainer();
            if (!persis.has(GiveBuildItem.telepadNum)) return;
            if (persis.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER) == idsource) display.remove();
        });
        TextDisplay t = (TextDisplay) l.getWorld().spawnEntity(l, EntityType.TEXT_DISPLAY);
        t.text(name);
        t.getPersistentDataContainer().set(GiveBuildItem.telepadNum, PersistentDataType.INTEGER, idsource);
        t.setBillboard(Display.Billboard.CENTER);
        e.getWhoClicked().closeInventory();
    }

    public static void openPublishGUII(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof TelepadGui tg) {
            e.setCancelled(true);
            e.getWhoClicked().openInventory(new PublishGUI(tg.id, tg.level).getInventory());
        }
    }

    public static void publishToEveryoneI(InventoryClickEvent e) {
        DataBasePool db = Telepads.INSTANCE.basePool;
        if (e.getClickedInventory().getHolder() instanceof PublishGUI tg) {
            DataBasePool.setPublic(db, tg.id);
            tg.publish = !(tg.publish);
            tg.executePublishUpdate();
        }
        e.setCancelled(true);
    }

    public static void listPermittetPlayersI(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof PublishGUI pg) {
            DataBasePool db = Telepads.INSTANCE.basePool;
            e.setCancelled(true);
            List<UUID> list = DataBasePool.getPermittetPlayer(db, pg.id);
            List<ItemStack> items = new ArrayList<>();
            for (UUID a : list) {
                PlayerProfile prof = Bukkit.getOfflinePlayer(a).getPlayerProfile();
                if (!prof.completeFromCache()) {
                    prof.complete();
                }
                ItemStack item = new ItemBuilder()
                    .setSkull(a)
                    .setName(prof.getName())
                    .addLoreLine("Klicke um zu entfernen.")
                    .whenClicked("telepads:remove_permittet_player")
                    .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(teleID, PersistentDataType.INTEGER, pg.id);
                item.setItemMeta(meta);
                items.add(item);   
            }
            e.getWhoClicked().openInventory(new PagenationInventory(items).getInventory());
        }
    }

    public static void addPlayerPermissionI(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof PublishGUI pg) {
            DataBasePool db = Telepads.INSTANCE.basePool;
            MiniMessage mm = MiniMessage.miniMessage();
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            new UseNextChatInput((Player) e.getWhoClicked())
                .sendMessage("Schreibe den Spielernamne den du hinzufügen willst in den Chat.<br>Schreibe \"exit\" zum abzubrechen.")
                .setChatEvent((player, message) -> {
                    if (message.equalsIgnoreCase("exit")) {
                      player.sendMessage("Abgebrochen");
                      return;
                    }
                    DataBasePool.addPlayerPermission(db, pg.id, Bukkit.getOfflinePlayer(message).getUniqueId());
                    player.sendMessage(mm.deserialize("Der Spieler \"<green><name></green>\" wurde für dieses Telepad gesetzt.",
                    Placeholder.component("name", Component.text(Bukkit.getOfflinePlayer(message).getName()))));
                    // player.sendMessage(mm.deserialize("<red>Ungültiger Name.</red>"));
                })
                .capture();
        }
    }

    public static void removePlayerPermissionI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        DataBasePool db = Telepads.INSTANCE.basePool;
        ItemMeta meta = e.getCurrentItem().getItemMeta();
        SkullMeta skull = (SkullMeta) e.getCurrentItem().getItemMeta();
        e.setCancelled(true);
        DataBasePool.removePlayerPermission(db, meta.getPersistentDataContainer().get(teleID, PersistentDataType.INTEGER), skull.getPlayerProfile().getId());
        e.getWhoClicked().sendMessage(mm.deserialize("Der Spieler <green>\"" + skull.getPlayerProfile().getName() + "\"</green> wurde entfernt."));
        if (e.getInventory().getHolder() instanceof PagenationInventory pg) {
            pg.items.remove(e.getCurrentItem());
            pg.fillPage(pg.currentpage);
        }
    }

    public static void openTelepadGUII(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory().getHolder() instanceof PublishGUI pg) {
            e.getWhoClicked().openInventory(new TelepadGui((Player) e.getWhoClicked(), pg.id).getInventory());
        }
    }

}
