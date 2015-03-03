package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class JackHammer extends AbilityListener implements Disableable {
    public int cooldown = 30;
    public String cooldownMessage = ChatColor.RED + "%s seconds until you can use jackhammer again!";
    private HashMap<ItemStack, Integer> cooldownTime = new HashMap<ItemStack, Integer>();
    public String hammerName = "Jack Hammer";
    public int ticksBetweenBreaks = 20;
    private HashMap<ItemStack, Integer> uses = new HashMap<ItemStack, Integer>();
    public int usesBeforeCooldown = 5;

    @EventHandler
    public void onBreak(final BlockBreakEvent event) {
        if (isSpecialItem(event.getPlayer().getItemInHand(), hammerName) && hasAbility(event.getPlayer())) {
            ItemStack item = event.getPlayer().getItemInHand();
            Hungergames hg = HungergamesApi.getHungergames();
            if (cooldownTime.containsKey(item) && cooldownTime.get(item) < hg.currentTime) {
                event.getPlayer().sendMessage(String.format(cooldownMessage, cooldownTime.get(item)));
            } else {
                int used = 0;
                if (uses.containsKey(item))
                    used = uses.get(item);
                used++;
                final Block b = event.getBlock().getRelative(BlockFace.UP);
                uses.put(item, used);
                if (used >= usesBeforeCooldown) {
                    cooldownTime.put(item, hg.currentTime + cooldown);
                }
                new BukkitRunnable() {
                    Block block = b;

                    public void run() {
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                            block = block.getRelative(BlockFace.UP);
                        } else
                            cancel();
                    }
                }.runTaskTimer(hg, ticksBetweenBreaks, ticksBetweenBreaks);
            }
        }
    }
}
