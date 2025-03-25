package kireiko.dev.anticheat.api.player.fun;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FunItemsService {
    public static void give(final Player player) {
        final boolean modern = ProtocolLibrary.getProtocolManager().getMinecraftVersion()
                .compareTo(new MinecraftVersion("1.13")) >= 0.0;
        { // hook
            final ItemStack rod = new ItemStack(Material.FISHING_ROD);
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9Hook");
                meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
        { // rocket launcher
            final ItemStack rod = new ItemStack(modern ? Material.DIAMOND_HORSE_ARMOR : Material.valueOf("DIAMOND_BARDING"));
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9Rocket Launcher");
                meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
        { // winter staff
            final ItemStack rod = new ItemStack(Material.DIAMOND_HOE);
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9Winter Staff");
                meta.addEnchant(Enchantment.DAMAGE_ALL, 15, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
        { // blaze staff
            final ItemStack rod = new ItemStack(Material.BLAZE_ROD);
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9Blaze Staff");
                meta.addEnchant(Enchantment.DAMAGE_ALL, 15, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
        { // ak47
            final ItemStack rod = new ItemStack(modern ? Material.GOLDEN_HORSE_ARMOR : Material.valueOf("GOLD_BARDING"));
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9AK-47");
                meta.addEnchant(Enchantment.DAMAGE_ALL, 9, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
        { // cursed spell
            final ItemStack rod = new ItemStack(Material.BOOK);
            final ItemMeta meta = rod.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§9Cursed Spell");
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                meta.setUnbreakable(true);
                rod.setItemMeta(meta);
            }
            player.getInventory().addItem(rod);
        }
    }
}