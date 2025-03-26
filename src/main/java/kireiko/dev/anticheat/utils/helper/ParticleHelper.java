package kireiko.dev.anticheat.utils.helper;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;

public class ParticleHelper {
    private static final Class<?> PARTICLE_CLASS;
    private static final Method SPAWN_METHOD;

    static {
        Class<?> particleClass = null;
        Method spawnMethod = null;
        try {
            particleClass = Class.forName("org.bukkit.Particle");
            spawnMethod = World.class.getMethod("spawnParticle", particleClass, Location.class,
                    int.class, double.class, double.class, double.class, double.class);
        } catch (Exception ignored) {}
        PARTICLE_CLASS = particleClass;
        SPAWN_METHOD = spawnMethod;
    }

    public static void spawn(World world, String particle, Location loc, int count,
                             double x, double y, double z, double extra) {
        try {
            if (PARTICLE_CLASS == null || SPAWN_METHOD == null) return;
            Enum<?> particleEnum = Enum.valueOf((Class<Enum>) PARTICLE_CLASS, particle);
            SPAWN_METHOD.invoke(world, particleEnum, loc, count, x, y, z, extra);
        } catch (Exception ignored) {}
    }
}