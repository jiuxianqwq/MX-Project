package kireiko.dev.anticheat.utils.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public final class ReflectionWrapper {

    public static boolean isParticleSupported() {
        try {
            Class.forName("org.bukkit.Particle");
            World.class.getMethod("spawnParticle",
                    Class.forName("org.bukkit.Particle"),
                    Location.class, int.class,
                    double.class, double.class, double.class, double.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void spawnParticle(World world, String particleName, Location loc, int count,
                                     double offsetX, double offsetY, double offsetZ, double extra) {
        try {
            if (!isParticleSupported()) {
                return;
            }
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Enum<?> particleEnum = Enum.valueOf((Class<Enum>) particleClass, particleName);
            Method spawnParticleMethod = world.getClass().getMethod("spawnParticle",
                    particleClass, Location.class, int.class,
                    double.class, double.class, double.class, double.class);
            spawnParticleMethod.invoke(world, particleEnum, loc, count, offsetX, offsetY, offsetZ, extra);
        } catch (Exception ignored) {
        }
    }

    public static void setUnbreakable(ItemStack item, boolean unbreakable) {
        if (item == null) return;
        try {
            ItemMeta meta = item.getItemMeta();
            try {
                Method setUnbreakableMethod = meta.getClass().getMethod("setUnbreakable", boolean.class);
                setUnbreakableMethod.invoke(meta, unbreakable);
                item.setItemMeta(meta);
                return;
            } catch (NoSuchMethodException ignored) {
            }

            // server version getter
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Method asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopyMethod.invoke(null, item);

            Method hasTagMethod = nmsItem.getClass().getMethod("hasTag");
            boolean hasTag = (boolean) hasTagMethod.invoke(nmsItem);
            Object tag;
            if (!hasTag) {
                Class<?> nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
                tag = nbtTagCompoundClass.newInstance();
            } else {
                Method getTagMethod = nmsItem.getClass().getMethod("getTag");
                tag = getTagMethod.invoke(nmsItem);
            }
            Method setIntMethod = tag.getClass().getMethod("setInt", String.class, int.class);
            setIntMethod.invoke(tag, "Unbreakable", unbreakable ? 1 : 0);
            Method setTagMethod = nmsItem.getClass().getMethod("setTag", tag.getClass());
            setTagMethod.invoke(nmsItem, tag);
            Method asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItem.getClass());
            ItemStack result = (ItemStack) asBukkitCopyMethod.invoke(null, nmsItem);
            item.setType(result.getType());
            item.setItemMeta(result.getItemMeta());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}