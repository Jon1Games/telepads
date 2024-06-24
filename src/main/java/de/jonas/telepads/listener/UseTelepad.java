package de.jonas.telepads.listener;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Beacon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.ParticleBuilder;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.effects.SpiralEffect;
import de.jonas.telepads.particle.spawner.BuilderParticle;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class UseTelepad implements Listener{

    DataBasePool db = Telepads.INSTANCE.basePool;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) return;
        Location to = e.getTo().clone();
        to.add(0,-1,0);
        if (to.getBlock() != null && to.getBlock().getState() instanceof Beacon b) {
            MiniMessage mm = MiniMessage.miniMessage();
            PersistentDataContainer container = b.getPersistentDataContainer();
            if (!container.has(GiveBuildItem.telepadNum)) return;
            if (Telepads.getEconomy().getBalance(e.getPlayer()) <= 2d) {
                e.getPlayer().sendMessage(mm.deserialize("<red>Du hast nicht genügen Geld um dich zu Teleportieren.</red>"));
                return;
            }
            int id = container.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER);
            Location l = DataBasePool.getDestination(db, id);
            if (l == null) return;
            l.setPitch(to.getPitch());
            l.setYaw(to.getYaw());
            Telepads.getEconomy().withdrawPlayer(e.getPlayer(), 2d);
            e.getPlayer().sendMessage(mm.deserialize("Dir wurden <green>2 Coins</green> zum Teleport abgezogen."));
            e.getPlayer().teleport(l.add(0.5,1,0.5));
            new ParticleRunner(
                    Telepads.INSTANCE,
                    l,
                    new SpiralEffect(2,
                            1,
                            2,
                            new BuilderParticle(
                                    new ParticleBuilder(Particle.DUST)
                                        .count(1)
                                        .color(Color.PURPLE, 1f)
                                        .source(e.getPlayer()))
                    ),
                    2,
                    10);
        }

    }
    
}
