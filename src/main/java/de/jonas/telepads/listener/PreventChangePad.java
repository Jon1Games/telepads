package de.jonas.telepads.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.jonas.telepads.commands.GiveBuildItem;

public class PreventChangePad implements Listener{

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (
            e.getBlock().getType().equals(Material.DARK_PRISMARINE) &&
            checkCrossSurroundingPadBeacon(e.getBlock().getLocation())
            ) {
                e.setCancelled(true);
                return;
        } else if (
            e.getBlock().getType().equals(Material.STRIPPED_WARPED_HYPHAE) &&
            checkSurroundingPadBeacon(e.getBlock().getLocation())
            ) {
                e.setCancelled(true);
                return;
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> {
            if (
                block.getType().equals(Material.DARK_PRISMARINE) &&
                checkCrossSurroundingPadBeacon(block.getLocation())
                ) {
                    return true;
            } else if (
                block.getType().equals(Material.STRIPPED_WARPED_HYPHAE) &&
                checkSurroundingPadBeacon(block.getLocation())
                ) {
                    return true;
            } else if (
                block.getType().equals(Material.BEACON) &&
                block.getState() instanceof Beacon b &&
                b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
                ) {
                    return true;
            } else return false;
        });
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> {
            if (
                block.getType().equals(Material.DARK_PRISMARINE) &&
                checkCrossSurroundingPadBeacon(block.getLocation())
                ) {
                    return true;
            } else if (
                block.getType().equals(Material.STRIPPED_WARPED_HYPHAE) &&
                checkSurroundingPadBeacon(block.getLocation())
                ) {
                    return true;
            } else if (
                block.getType().equals(Material.BEACON) &&
                block.getState() instanceof Beacon b &&
                b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
                ) {
                    return true;
            } else return false;
        });
    }

    @EventHandler
    public void onPistonExtetion(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (
                block.getType().equals(Material.DARK_PRISMARINE) &&
                checkCrossSurroundingPadBeacon(block.getLocation())
                ) {
                    e.setCancelled(true);
                    return;
            } else if (
                block.getType().equals(Material.STRIPPED_WARPED_HYPHAE) &&
                checkSurroundingPadBeacon(block.getLocation())
                ) {
                    e.setCancelled(true);
                    return;
            }
        }
    }

    @EventHandler
    public void onPistonRetraction(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            if (
                block.getType().equals(Material.DARK_PRISMARINE) &&
                checkCrossSurroundingPadBeacon(block.getLocation())
                ) {
                    e.setCancelled(true);
                    return;
            } else if (
                block.getType().equals(Material.STRIPPED_WARPED_HYPHAE) &&
                checkSurroundingPadBeacon(block.getLocation())
                ) {
                    e.setCancelled(true);
                    return;
            }
        }
    }

    private boolean checkSurroundingPadBeacon(Location loc) {
        Location l1 = loc.clone().add(1,0,0);
        Location l2 = loc.clone().add(-1,0,0);
        Location l3 = loc.clone().add(0,0,1);
        Location l4 = loc.clone().add(0,0,-1);;
        if (
            l1.getBlock().getType().equals(Material.BEACON) &&
            l1.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l2.getBlock().getType().equals(Material.BEACON) &&
            l2.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l3.getBlock().getType().equals(Material.BEACON) &&
            l3.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l4.getBlock().getType().equals(Material.BEACON) &&
            l4.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {;
            return true;
        } else {
            return false;
        }
    }
    
    private boolean checkCrossSurroundingPadBeacon(Location loc) {
        Location l1 = loc.clone().add(1,0,1);
        Location l2 = loc.clone().add(1,0,-1);
        Location l3 = loc.clone().add(-1,0,1);
        Location l4 = loc.clone().add(-1,0,-1);
        if (
            l1.getBlock().getType().equals(Material.BEACON) &&
            l1.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l2.getBlock().getType().equals(Material.BEACON) &&
            l2.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l3.getBlock().getType().equals(Material.BEACON) &&
            l3.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else if (
            l4.getBlock().getType().equals(Material.BEACON) &&
            l4.getBlock().getState() instanceof Beacon b &&
            b.getPersistentDataContainer().has(GiveBuildItem.telepadNum)
        ) {
            return true;
        } else {
            return false;
        }
    }

}
