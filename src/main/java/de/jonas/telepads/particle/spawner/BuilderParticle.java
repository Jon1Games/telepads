package de.jonas.telepads.particle.spawner;

import com.destroystokyo.paper.ParticleBuilder;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.ParticleSpawner;

public class BuilderParticle implements ParticleSpawner {
    private ParticleBuilder builder;

    public BuilderParticle(ParticleBuilder builder) {
        this.builder = builder;
    }

    public void spawn(ParticleRunner runner, double progress, double xo, double yo, double zo) {
        builder.location(
                runner.loc.getWorld(),
                runner.loc.getX() + xo,
                runner.loc.getY() + yo,
                runner.loc.getZ() + zo)
            .spawn();
    }

}
