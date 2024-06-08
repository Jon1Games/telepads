package de.jonas.telepads.particle.spawner;

import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.ParticleSpawner;
import org.bukkit.Particle;

public class PortalParticle implements ParticleSpawner {
    public static final PortalParticle EFFECT = new PortalParticle();

    public void spawn(ParticleRunner runner, double progress, double xo, double yo, double zo) {
        runner.loc.getWorld().spawnParticle(Particle.PORTAL,
                runner.loc.getX() + xo,
                runner.loc.getY() + yo,
                runner.loc.getZ() + zo, 1);
    }
}
