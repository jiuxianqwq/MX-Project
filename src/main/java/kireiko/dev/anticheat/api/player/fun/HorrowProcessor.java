package kireiko.dev.anticheat.api.player.fun;

import kireiko.dev.anticheat.api.player.PlayerProfile;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

import static kireiko.dev.anticheat.utils.protocol.ProtocolTools.getBlockAsync;

@UtilityClass
public class HorrowProcessor {
    public static void tick(final PlayerProfile profile) {
        final int stage = profile.horrowStage;
        if (stage > 2 && ThreadLocalRandom.current().nextDouble() < 0.04) {
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(0, 2, 0), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(1, 1, 0), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(0, 1, 1), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(-1, 1, 0), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(0, 1, -1), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(0, 1, 1), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(1, 1, 0), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(0, 1, -1), Material.BARRIER);
            PhantomWorld.setBlock(profile.getPlayer(), profile.getTo().clone().add(-1, 1, 0), Material.BARRIER);
        } else if (stage > 1 && ThreadLocalRandom.current().nextDouble() < ((stage > 3) ? 0.4 : 0.05)) {
            PhantomWorld.setLocalHealthAndHunger(profile.getPlayer(),
                            ThreadLocalRandom.current().nextFloat() * 20,
                            ThreadLocalRandom.current().nextInt() * 20);
        } else if (ThreadLocalRandom.current().nextDouble() < ((stage > 2) ? 0.3 : 0.05)) {
            PhantomWorld.setLocalTime(profile.getPlayer(), (long) (ThreadLocalRandom.current().nextDouble() * 15000));
        } else if (ThreadLocalRandom.current().nextDouble() < 0.04) {
            final Location t = profile.getTo();
            for (int dx = -4; dx <= 4; ++dx) {
                for (int dy = -4; dy <= 4; ++dy) {
                    for (int dz = -4; dz <= 4; ++dz) {
                        final Location l = new Location(
                                        profile.getPlayer().getWorld(),
                                        t.getX() + (double) dx,
                                        t.getY() + (double) dy,
                                        t.getZ() + (double) dz
                        );
                        final Block block = getBlockAsync(l);
                        if (block == null) {
                            continue;
                        }
                        final Material material = block.getType();
                        if (!material.name().equals("AIR")
                                        && ThreadLocalRandom.current().nextDouble() < ((stage > 1) ? 0.12 : 0.03)) {
                            PhantomWorld.setBlock(profile.getPlayer(), l, Material.NETHERRACK);
                            break;
                        } else if (stage > 3 && ThreadLocalRandom.current().nextDouble() < 0.05) {
                            PhantomWorld.setBlock(profile.getPlayer(), l, Material.FIRE);
                            break;
                        }
                    }
                }
            }
        }
    }
}
