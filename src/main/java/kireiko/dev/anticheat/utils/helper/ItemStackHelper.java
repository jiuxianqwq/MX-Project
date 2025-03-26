package kireiko.dev.anticheat.utils.helper;

import kireiko.dev.anticheat.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Method;

public class ItemStackHelper {
    private static boolean MODERN_API;
    private static final Method SET_UNBREAKABLE_METHOD;

    static {
        Method method;
        try {
            method = ItemMeta.class.getMethod("setUnbreakable", boolean.class);
            MODERN_API = true;
        } catch (NoSuchMethodException e) {
            method = null;
            MODERN_API = false;
        }
        SET_UNBREAKABLE_METHOD = method;
    }

    public static void setUnbreakable(ItemStack item, boolean unbreakable) {
        if (item == null) return;
        try {
            ItemMeta meta = item.getItemMeta();
            if (MODERN_API) {
                SET_UNBREAKABLE_METHOD.invoke(meta, unbreakable);
                item.setItemMeta(meta);
            } else {
                handleLegacy(item, unbreakable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleLegacy(ItemStack item, boolean unbreakable) throws Exception {
        // server version getter
        String version = BukkitUtils.getVersion();
        Class<?> craftClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        Object nmsItem = craftClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);

        Object tag = getTag(nmsItem);
        tag.getClass().getMethod("setInt", String.class, int.class).invoke(tag, "Unbreakable", unbreakable ? 1 : 0);
        nmsItem.getClass().getMethod("setTag", tag.getClass()).invoke(nmsItem, tag);

        ItemStack result = (ItemStack) craftClass.getMethod("asBukkitCopy", nmsItem.getClass()).invoke(null, nmsItem);
        item.setItemMeta(result.getItemMeta());
    }

    private static Object getTag(Object nmsItem) throws Exception {
        if ((boolean) nmsItem.getClass().getMethod("hasTag").invoke(nmsItem)) {
            return nmsItem.getClass().getMethod("getTag").invoke(nmsItem);
        }
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + ".NBTTagCompound").newInstance();
    }
}