package de.jonas.telepads.listener;

import org.bukkit.block.Beacon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.logging.*;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.gui.TelepadGui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class OpenGui implements Listener {

    @EventHandler
    public void onTelepadClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getClickedBlock().getState() == null) return;
        if (!(e.getClickedBlock().getState() instanceof Beacon b)) return;
        PersistentDataContainer container = b.getPersistentDataContainer();
        if (!container.has(GiveBuildItem.telepadNum)) return;
        DataBasePool db = Telepads.INSTANCE.basePool;
        MiniMessage mm = MiniMessage.miniMessage();
        e.setCancelled(true);
        Player p = e.getPlayer();
        int id = b.getPersistentDataContainer().get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER);
        if (!(p.hasPermission("telepads.admin") || DataBasePool.playerIsOwner(db, 
            id, p.getUniqueId()))) { 
                p.sendMessage(mm.deserialize("<red>Du bist nicht der Eigentümer dieses Telepads!</red>"));
                return;
            }

        if (!DataBasePool.telepadExists(db, id)) {
            p.sendMessage(mm.deserialize("Ein fehler ist aufgetreten, bitte melde dich im support."));
            Telepads.INSTANCE.getLogger().log(Level.WARNING, "Telepad error, Location: " + e.getClickedBlock().getLocation());
            return;
        }

        e.getPlayer().openInventory(new TelepadGui(e.getPlayer(), container.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER)).getInventory());
    }
    
}
