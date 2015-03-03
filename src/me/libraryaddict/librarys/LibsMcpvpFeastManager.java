package me.libraryaddict.librarys;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import me.libraryaddict.Hungergames.Interfaces.ChestManager;
import me.libraryaddict.Hungergames.Listeners.LibsFeastManager;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class LibsMcpvpFeastManager extends LibsFeastManager {

    public void generateChests(final Location loc, int height) {
        final int h = height - 1;
        ChestManager cm = HungergamesApi.getChestManager();
        for (int x = -h; x <= h; x++) {
            for (int z = -h; z <= h; z++) {
                Block block = loc.clone().add(x, 0, z).getBlock();
                Block b = block;
                if (x == 0 && z == 0)
                    gen.setBlockFast(b, Material.ENCHANTMENT_TABLE.getId(), (short) 0);
                else if (Math.abs(x + z) % 2 == 0) {
                    gen.addToProcessedBlocks(block);
                    block.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
                    Chest chest = (Chest) block.getState();
                    cm.fillChest(chest.getInventory());
                    chest.update();
                }
            }
        }
    }
}
