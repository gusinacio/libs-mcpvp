package me.libraryaddict.librarys.Abilities;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Managers.EnchantmentManager;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Launcher extends AbilityListener implements Disableable {
    public String launcherBlockName = ChatColor.WHITE + "Launcher Block";
    public double launcherStrength = 1;

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata("Launcher")) {
            event.getBlock().removeMetadata("Launcher", HungergamesApi.getHungergames());
            event.setCancelled(true);
            Item item = event
                    .getBlock()
                    .getWorld()
                    .dropItemNaturally(event.getBlock().getLocation().clone().add(0.5, 0, 0.1),
                            new ItemStack(event.getBlock().getType()));
            event.getBlock().setType(Material.AIR);
            ItemStack itemstack = item.getItemStack().clone();
            ItemMeta meta = itemstack.getItemMeta();
            meta.setDisplayName(launcherBlockName);
            itemstack.setItemMeta(meta);
            itemstack.addEnchantment(EnchantmentManager.UNLOOTABLE, 1);
            EnchantmentManager.updateEnchants(itemstack);
            item.setItemStack(itemstack);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Iterator<Block> itel = event.blockList().iterator();
        while (itel.hasNext()) {
            Block block = itel.next();
            if (block.hasMetadata("Launcher")) {
                BlockBreakEvent newEvent = new BlockBreakEvent(block, null);
                onBreak(newEvent);
                itel.remove();
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (HungergamesApi.getPlayerManager().getGamer(p).isAlive()) {
            Block b = event.getTo().getBlock().getRelative(BlockFace.DOWN);
            if (b.hasMetadata("Launcher") && b.getMetadata("Launcher").size() > 0) {
                double strength = 0;
                Block under = b;
                while (under.getType() == b.getType() && under.hasMetadata("Launcher") && under.getData() == b.getData()) {
                    under = under.getRelative(BlockFace.DOWN);
                    strength++;
                }
                strength /= 2;
                BlockFace face = (BlockFace) b.getMetadata("Launcher").get(0).value();
                double y = (double) face.getModY() * strength;
                if (y == 0)
                    y = 0.1 * strength;
                strength *= launcherStrength;
                Vector vector = new Vector((double) face.getModX() * strength, y, (double) face.getModZ() * strength);
                p.setFallDistance(-1000);
                p.setVelocity(vector);
            }
        }
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks())
            if (b.hasMetadata("Launcher"))
                event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isSpecialItem(event.getItemInHand(), this.launcherBlockName)) {
            if (hasAbility(event.getPlayer())) {
                BlockFace face = event.getBlockAgainst().getFace(event.getBlock());
                if (face == BlockFace.DOWN)
                    face = BlockFace.UP;
                event.getBlock().setMetadata("Launcher", new FixedMetadataValue(HungergamesApi.getHungergames(), face));
            } else
                event.setCancelled(true);
        }
    }
}
