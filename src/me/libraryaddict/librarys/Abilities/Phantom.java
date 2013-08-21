package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Phantom extends AbilityListener implements Disableable {
    private HashMap<ItemStack, Integer> cooldown = new HashMap<ItemStack, Integer>();
    public String cooldownMessage = ChatColor.RED + "You cannot use that just yet! %s seconds of cooldown remaining!";
    public int cooldownTime = 60;
    public boolean doubleFallDamage = true;
    private HashMap<Player, Integer> flightLeft = new HashMap<Player, Integer>();
    public String flightLeftMessage = ChatColor.RED + "You have %s of flight left!";
    public String flightWoreOff = ChatColor.RED + "Your flight disappeared!";
    public boolean giveFlightArmor = true;
    private Hungergames hg = HungergamesApi.getHungergames();
    public int phantomFeatherId = Material.FEATHER.getId();
    public String phantomFeatherName = "Condor's Feather";
    private HashMap<Player, ItemStack[]> playerArmor = new HashMap<Player, ItemStack[]>();
    public int secondsOfFlight = 5;

    private ItemStack colorIn(Material mat) {
        ItemStack armor = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        meta.setColor(Color.WHITE);
        armor.setItemMeta(meta);
        return armor;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (playerArmor.containsKey(event.getWhoClicked())) {
            if (event.getCurrentItem().getType().name().contains("LEATHER_")
                    && ((LeatherArmorMeta) event.getCurrentItem().getItemMeta()).getColor().equals(Color.WHITE))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null)
{
  AnticheatAPI.exemptPlayer(player, CheckType.FLY);

}
        if (event.getAction().name().contains("RIGHT") && isSpecialItem(event.getItem(), phantomFeatherName)
                && event.getItem().getTypeId() == phantomFeatherId && hasAbility(event.getPlayer())) {
            Player p = event.getPlayer();
            if (cooldown.containsKey(event.getItem()) && cooldown.get(event.getItem()) > hg.currentTime) {
                p.sendMessage(String.format(cooldownMessage, hg.returnTime(cooldown.get(event.getItem()) - hg.currentTime)));
                return;
            }
            cooldown.put(event.getItem(), hg.currentTime + cooldownTime);
            flightLeft.put(p, secondsOfFlight + 1);
            p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 2, 1);
            p.setAllowFlight(true);
            p.setFlying(true);
            if (giveFlightArmor) {
                PlayerInventory inv = p.getInventory();
                playerArmor.put(p, inv.getArmorContents());
                inv.setHelmet(colorIn(Material.LEATHER_HELMET));
                inv.setChestplate(colorIn(Material.LEATHER_CHESTPLATE));
                inv.setLeggings(colorIn(Material.LEATHER_LEGGINGS));
                inv.setBoots(colorIn(Material.LEATHER_BOOTS));
                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        flightLeft.remove(event.getKilled().getPlayer());
        if (playerArmor.containsKey(event.getKilled().getPlayer())) {
            Iterator<ItemStack> itel = event.getDrops().iterator();
            while (itel.hasNext()) {
                ItemStack item = itel.next();
                if (item.getType().name().contains("LEATHER_")
                        && ((LeatherArmorMeta) item.getItemMeta()).getColor().equals(Color.WHITE))
                    itel.remove();
            }
            for (ItemStack item : playerArmor.remove(event.getKilled().getPlayer()))
                event.getDrops().add(item);
        }
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        Iterator<Player> itel = flightLeft.keySet().iterator();
        while (itel.hasNext()) {
            Player p = itel.next();
            flightLeft.put(p, flightLeft.get(p) - 1);
            if (flightLeft.get(p) <= 0) {
                itel.remove();
                p.setAllowFlight(false);
                if (giveFlightArmor)
                    p.getInventory().setArmorContents(playerArmor.remove(p));
                p.getWorld().playSound(p.getLocation(), Sound.AMBIENCE_RAIN, 3, 4);
                p.sendMessage(flightWoreOff);
            } else
                p.sendMessage(String.format(flightLeftMessage, flightLeft.get(p)));
        }
    }

}
