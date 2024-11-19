package de.jonas.telepads.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import net.kyori.adventure.text.Component;

public class CustomizeGUI implements InventoryHolder{

    Inventory inv;
    public int id, blockID;
    String pl,name;
    public CustomizeGUI(int id, int level) {
    DataBasePool db = Telepads.INSTANCE.basePool;
    String name = DataBasePool.getName(db, id);
    String blockID = DataBasePool.getBlockID(db, id);
    Material block;
    if (blockID == null) {
        block = Material.BEACON;
    } else {block = Material.getMaterial(blockID.toUpperCase());}
    
    this.id = id;

        inv = Bukkit.createInventory(this, (9*5), Component.text(""));

        int[] place = {0,1,2,3,4,5,6,7,8,9,10,11,13,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32,33,34,35,36,37,38,39,40,41,42,43,44};
        for (int a : place) {
            inv.setItem(a,
                new ItemBuilder()
                    .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                    .setName("")
                    .whenClicked("telepads:cancelevent")
                    .build()    
            );
        }

        inv.setItem(12, 
            new ItemBuilder()
                .setMaterial(Material.NAME_TAG)
                .setName(name)
                .addLoreLine("Klicken Namen ändern.")
                .whenClicked("telepads:click_change_name")
            .build()
        );

        inv.setItem(14, 
            new ItemBuilder()
                .setMaterial(block)
                .setName("Anzeige Block")
                .whenClicked("telepads:click_block")
            .build()
        );

        inv.setItem(31, 
            new ItemBuilder()
                .setMaterial(Material.BARRIER)
                .setName("Zurück")
                .whenClicked("telepads:open_telepad_gui")
                .build()
        );

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
    
}
