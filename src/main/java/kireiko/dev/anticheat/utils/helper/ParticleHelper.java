// ParticleHelper.java
package kireiko.dev.anticheat.utils.helper;

import org.bukkit.Location;
import org.bukkit.World;
import java.lang.reflect.Method;

public final class ParticleHelper {
    private static final boolean MODERN;
    private static Method SPAWN_METHOD;
    private static Class<?> PARTICLE_ENUM;

    static {
        boolean modern = false;
        try {
            PARTICLE_ENUM = Class.forName("org.bukkit.Particle");
            World.class.getMethod("spawnParticle", PARTICLE_ENUM, Location.class,
                    int.class, double.class, double.class, double.class, double.class);
            modern = true;
        } catch (Exception e) {
            try {
                SPAWN_METHOD = World.class.getMethod("spawnParticle",
                        String.class, Location.class, int.class,
                        double.class, double.class, double.class, double.class);
            } catch (Exception ignored) {}
        }
        MODERN = modern;
    }

    public static void spawn(World world, String type, Location loc, int count,
                             double x, double y, double z, double extra) {
        try {
            if (MODERN) {
                Object particle = Enum.valueOf((Class<Enum>) PARTICLE_ENUM, type);
                world.getClass().getMethod("spawnParticle", PARTICLE_ENUM, Location.class,
                                int.class, double.class, double.class, double.class, double.class)
                        .invoke(world, particle, loc, count, x, y, z, extra);
            } else if (SPAWN_METHOD != null) {
                SPAWN_METHOD.invoke(world, type, loc, count, x, y, z, extra);
            }
        } catch (Exception ignored) {}
    }
}
