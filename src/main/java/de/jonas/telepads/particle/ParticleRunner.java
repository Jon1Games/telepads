package de.jonas.telepads.particle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ParticleRunner implements Runnable {
    public Location loc;
    public BukkitTask task;
    private ParticleEffect effect;
    private int age = 0;
    private int maxAge;

    /**
     * Starts to play a specified Particle Effect
     * @param plugin plugin that playes the effect
     * @param loc location where to play the effect
     * @param effect to be displayed
     * @param period in ticks of spawns
     * @param maxAge in period
     */
    public ParticleRunner(Plugin plugin, Location loc, ParticleEffect effect, int period, int maxAge) {
        this.loc = loc;
        this.maxAge = maxAge;
        this.effect = effect;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, period);
    }

    public void run() {
        double progress = (double) age / (double) maxAge;
        effect.tick(this, progress);
        age++;
        if(age > maxAge) task.cancel();
    }
}
