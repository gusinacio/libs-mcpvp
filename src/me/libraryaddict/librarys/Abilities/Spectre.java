package me.libraryaddict.librarys.Abilities;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EntityTracker;
import net.minecraft.server.v1_7_R1.EntityTrackerEntry;
import net.minecraft.server.v1_7_R1.WorldServer;

public class Spectre extends AbilityListener implements Disableable {
    public boolean addInvisToSpectre = true;
    private transient HashMap<ItemStack, Integer> cooldown = new HashMap<ItemStack, Integer>();
    public String cooldownMessage = ChatColor.BLUE + "You can use this again in %s seconds!";
    public int cooldownTime = 180;
    private transient HashMap<Player, Integer> invis = new HashMap<Player, Integer>();
    public int invisLength = 20;
    public boolean playSound = true;
    public String soundName = Sound.SPLASH.name();
    public String spectreItemName = ChatColor.WHITE + "Spectre Dust";
    public int spectreOffItemId = Material.SUGAR.getId();
    public int spectreOnItemId = Material.REDSTONE.getId();

    private EntityPlayer getHandle(Player p) {
        return ((CraftPlayer) p).getHandle();
    }

    private void hidePlayer(Player hider) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == hider)
                continue;
            EntityPlayer observer = getHandle(p);
            if (observer.playerConnection == null)
                continue;
            Map<String, Player> hiddenPlayers = null;
            try {
                Field map = CraftPlayer.class.getDeclaredField("hiddenPlayers");
                map.setAccessible(true);
                hiddenPlayers = (Map<String, Player>) map.get(p);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (hiddenPlayers.containsKey(hider.getName()))
                continue;
            hiddenPlayers.put(hider.getName(), hider);

            // remove this player from the hidden player's EntityTrackerEntry
            EntityPlayer other = getHandle(hider);
            EntityTracker tracker = ((WorldServer) other.world).tracker;
            EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(hider.getEntityId());
            if (entry != null) {
                entry.clear(observer);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        if (invis.containsKey(event.getDamager())) {
            invis.remove(event.getDamager());
            Player p = (Player) event.getDamager();
            if (addInvisToSpectre)
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            showPlayer(p);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled())
            return;
        if (invis.containsKey(event.getEntity())) {
            invis.remove(event.getEntity());
            Player p = (Player) event.getEntity();
            if (addInvisToSpectre)
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            showPlayer(p);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && event.getAction().name().contains("RIGHT")) {
            if (isSpecialItem(item, spectreItemName) && hasAbility(event.getPlayer())) {
                event.setCancelled(true);
                Player p = event.getPlayer();
                p.updateInventory();
                int currentTime = HungergamesApi.getHungergames().currentTime;
                if (item.getTypeId() == spectreOffItemId) {
                    p.sendMessage(String.format(cooldownMessage, (-(currentTime - cooldown.get(item)))));
                } else if (item.getTypeId() == spectreOnItemId) {
                    item.setTypeId(spectreOffItemId);
                    cooldown.put(item, cooldownTime + currentTime);
                    invis.put(p, currentTime + invisLength);
                    if (addInvisToSpectre)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisLength * 20, 0), true);
                    hidePlayer(p);
                }
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (invis.containsKey(event.getKilled().getPlayer()))
            invis.remove(event.getKilled().getPlayer());
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        Hungergames hg = HungergamesApi.getHungergames();
        if (cooldown.containsValue(hg.currentTime)) {
            Iterator<ItemStack> itel = cooldown.keySet().iterator();
            while (itel.hasNext()) {
                boolean carryOn = false;
                ItemStack item = itel.next();
                if (cooldown.get(item) == hg.currentTime) {
                    for (Gamer gamer : HungergamesApi.getPlayerManager().getAliveGamers()) {
                        if (gamer.getPlayer().getInventory().contains(item)) {
                            itel.remove();
                            for (ItemStack i : gamer.getPlayer().getInventory().getContents()) {
                                if (i.equals(item)) {
                                    i.setTypeId(spectreOnItemId);
                                    carryOn = true;
                                    break;
                                }
                            }
                            if (carryOn)
                                break;
                        }
                        if (gamer.getPlayer().getItemOnCursor() != null && gamer.getPlayer().getItemOnCursor().equals(item)) {
                            gamer.getPlayer().getItemOnCursor().setTypeId(spectreOnItemId);
                            carryOn = true;
                            break;
                        }
                    }
                    if (carryOn)
                        continue;
                    for (Item itemEntity : HungergamesApi.getHungergames().world.getEntitiesByClass(Item.class)) {
                        if (itemEntity.getItemStack().equals(item)) {
                            itel.remove();
                            itemEntity.getItemStack().setTypeId(spectreOnItemId);
                            carryOn = true;
                            break;
                        }
                    }
                }
            }
        }
        Sound sound = Sound.valueOf(soundName.toUpperCase());
        Iterator<Player> itel = invis.keySet().iterator();
        while (itel.hasNext()) {
            Player p = itel.next();
            if (playSound)
                for (Entity e : p.getNearbyEntities(20, 20, 20))
                    if (e instanceof Player)
                        ((Player) e).playSound(p.getLocation().clone(), sound, 1, 0);
            if (invis.get(p) <= hg.currentTime) {
                itel.remove();
                showPlayer(p);
            }
        }
    }

    private void showPlayer(Player hider) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == hider)
                continue;
            EntityPlayer observer = getHandle(p);
            if (observer.playerConnection == null)
                continue;
            Map<String, Player> hiddenPlayers = null;
            try {
                Field map = CraftPlayer.class.getDeclaredField("hiddenPlayers");
                map.setAccessible(true);
                hiddenPlayers = (Map<String, Player>) map.get(p);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (!hiddenPlayers.containsKey(hider.getName()))
                return;
            hiddenPlayers.remove(hider.getName());

            EntityPlayer other = getHandle(hider);
            EntityTracker tracker = ((WorldServer) other.world).tracker;
            EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(hider.getEntityId());
            if (entry != null && !entry.trackedPlayers.contains(observer)) {
                entry.updatePlayer(observer);
            }
        }
    }

}
