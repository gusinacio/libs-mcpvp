package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.Hungergames.Types.Gamer;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class Endermage extends AbilityListener implements Disableable {
    public boolean canWarpOtherEndermanges = true;
    private transient HashMap<Player, Integer> cooldown = new HashMap<Player, Integer>();
    public String cooldownMessage = ChatColor.RED + "You cannot use this just yet! Wait %s seconds!";
    public int cooldownTime = 30;
    public String endermagePortalName = "Endermage Portal";
    private Hungergames hg = HungergamesApi.getHungergames();
    public int invincibleTicks = 100;
    public String nooneWarped = ChatColor.RED + "You didn't get anyone! Cooldown still applies!";
    public String warpedMessage = ChatColor.LIGHT_PURPLE + "Warped!\n" + ChatColor.RED
            + "You are invincible for 5 seconds!\nPrepare to fight!";

    private boolean isEnderable(Location portal, Location player) {
        return Math.abs(portal.getX() - player.getX()) < 2 && Math.abs(portal.getZ() - player.getZ()) < 2
                && Math.abs(portal.getY() - player.getY()) >= 3.5;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (isSpecialItem(item, endermagePortalName) && hasAbility(event.getPlayer())) {
            event.setCancelled(true);
            Player mage = event.getPlayer();
            mage.updateInventory();
            final Block b = event.getBlock();
            if (cooldown.containsKey(mage) && cooldown.get(mage) > hg.currentTime) {
                mage.sendMessage(String.format(cooldownMessage, cooldown.get(mage) - hg.currentTime));
            } else {
                Location portal = b.getLocation().clone().add(0.5, 0.5, 0.5);
                boolean foundSomeone = false;
                for (Gamer gamer : HungergamesApi.getPlayerManager().getAliveGamers()) {
                    Player victim = gamer.getPlayer();
                    if (gamer.isAlive() && victim != mage && isEnderable(portal, victim.getLocation())
                            && (canWarpOtherEndermanges || !hasAbility(victim))) {
                        foundSomeone = true;
                        warp(victim, portal);
                    }
                }
                cooldown.put(mage, hg.currentTime + cooldownTime);
                if (foundSomeone) {
                    warp(mage, portal);
                } else
                    mage.sendMessage(nooneWarped);
            }
        }
    }

    private void warp(Player victim, Location portal) {
        victim.playEffect(victim.getLocation(), Effect.ENDER_SIGNAL, 9);
        victim.playEffect(portal, Effect.ENDER_SIGNAL, 9);
        victim.playSound(victim.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1.2F);
        victim.playSound(portal, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
        if (invincibleTicks > 0)
            victim.setNoDamageTicks(invincibleTicks);
        victim.teleport(portal);
        victim.sendMessage(warpedMessage);
    }

}
    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && item != null && item.getTypeId() == endermagePortalId
                && isSpecialItem(item, endermagePortalName) && hasAbility(event.getPlayer())) {
            event.setCancelled(true);
            final Block b = event.getClickedBlock();
            if (endermages.contains(b))
                return;
            endermages.add(b);
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() == 0)
                event.getPlayer().setItemInHand(new ItemStack(0));
            final Location portal = b.getLocation().clone().add(0.5, 0.5, 0.5);
            final Material material = b.getType();
            final byte dataValue = b.getData();
            portal.getBlock().setTypeId(Material.ENDER_PORTAL_FRAME.getID());
            final Gamer mager = HungergamesApi.getPlayerManager().getGamer(event.getPlayer());
            for (int i = 0; i <= 5; i++) {
                final int no = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
                    public void run() {
                        for (Gamer gamer : HungergamesApi.getPlayerManager().getAliveGamers()) {
                            Player p = gamer.getPlayer();
                            if (p != mager.getPlayer() && isEnderable(portal, p.getLocation()) && p.getKit() != Puxador) {
                               
                                if (gamer.isAlive()) {
                                    if (p.getLocation().distance(portal) > 4) {
                                        p.playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 9);
                                        p.playEffect(portal, Effect.ENDER_SIGNAL, 9);
                                        p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                                        p.playSound(portal, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                                    }
                                    if (invincibleTicks > 0)
                                        p.setNoDamageTicks(invincibleTicks);
                                    p.teleport(portal.clone().add(0, 0.5, 0));
                                    portal.getBlock().setTypeIdAndData(material.getId(), dataValue, true);
                            if (mager.isAlive()) {
                                ItemStack item = new ItemStack(endermagePortalId);
                                ItemMeta meta = item.getItemMeta();
                                meta.setDisplayName(endermagePortalName);
                                item.setItemMeta(meta);
                                item.addEnchantment(EnchantmentManager.UNLOOTABLE, 1);
                                EnchantmentManager.updateEnchants(item);
                                HungergamesApi.getKitManager().addItem(mager.getPlayer(), item);
                                endermages.remove(b);
                                }
                            }
                        }
                        else if (no == 5) {
                            portal.getBlock().setTypeIdAndData(material.getId(), dataValue, true);
                            if (mager.isAlive()) {
                                ItemStack item = new ItemStack(endermagePortalId);
                                ItemMeta meta = item.getItemMeta();
                                meta.setDisplayName(endermagePortalName);
                                item.setItemMeta(meta);
                                item.addEnchantment(EnchantmentManager.UNLOOTABLE, 1);
                                EnchantmentManager.updateEnchants(item);
                                HungergamesApi.getKitManager().addItem(mager.getPlayer(), item);
                                endermages.remove(b);
                            }
                        }
                    }
                }, i * 20);
            }
        }
    }
