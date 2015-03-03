package me.libraryaddict.librarys.Abilities;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Analyzer extends AbilityListener implements Disableable {
    /*
    * Attack hidden players
    *
    * Copyright 2012 Kristian S. Stangeland (Comphenix)
    *
    * This library is free software; you can redistribute it and/or
    * modify it under the terms of the GNU Lesser General private
    * License as published by the Free Software Foundation; either
    * version 2.1 of the License, or (at your option) any later version.
    *
    * This library is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    * Lesser General private License for more details.
    *
    * You should have received a copy of the GNU Lesser General private
    * License along with this library. If not, see <[url]http://www.gnu.org/licenses/>[/url].
    */

    private class Vector3D {

        // Use protected members, like Bukkit
        private final double x;
        private final double y;
        private final double z;

        private Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private Vector3D(Location location) {
            this(location.toVector());
        }

        private Vector3D(Vector vector) {
            if (vector == null)
                throw new IllegalArgumentException("Vector cannot be NULL.");
            this.x = vector.getX();
            this.y = vector.getY();
            this.z = vector.getZ();
        }

        private Vector3D abs() {
            return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
        }

        private Vector3D add(double x, double y, double z) {
            return new Vector3D(this.x + x, this.y + y, this.z + z);
        }

        private Vector3D add(Vector3D other) {
            if (other == null)
                throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x + other.x, y + other.y, z + other.z);
        }

        private Vector3D multiply(double factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        private Vector3D multiply(int factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        private Vector3D subtract(Vector3D other) {
            if (other == null)
                throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x - other.x, y - other.y, z - other.z);
        }
    }

    public boolean alwaysDisplayInfo = false;
    public boolean countSoup = false;
    private HashMap<Player, String> currentNames = new HashMap<Player, String>();
    public String informationItem = ChatColor.WHITE + "Itemstack " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has "
            + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " items";
    public String informationMob = ChatColor.WHITE + "Mob " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has "
            + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " health";
    public String informationPlayer = ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has Health: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Hunger:" + ChatColor.YELLOW + " %s" + ChatColor.WHITE + ", Armor: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Kit: " + ChatColor.YELLOW + "%s";
    public String informationPlayerSoup = ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has Health: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Hunger:" + ChatColor.YELLOW + " %s" + ChatColor.WHITE + ", Armor: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Kit: " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + ", Soups: " + ChatColor.YELLOW + "%s";
    public int rangeToScan = 100;
    private int scheduler;
    public int ticksPerEntityCheck = 10;

    public Analyzer() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null)
            throw new Exception(
                    String.format(HungergamesApi.getConfigManager().getLoggerConfig().getDependencyNotFound(), "ProtocolLib"));
    }

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                ticksPerEntityCheck, ticksPerEntityCheck);
    }

    private int getArmorRating(ItemStack[] items) {
        int i = 0;
        for (ItemStack item : items)
            if (item != null)
                i += getArmorValue(item);
        return i;
    }

    private int getArmorValue(ItemStack armor) {
        Material mat = armor.getType();
        if (mat == Material.LEATHER_HELMET || mat == Material.LEATHER_BOOTS || mat == Material.GOLD_BOOTS
                || mat == Material.CHAINMAIL_BOOTS)
            return 1;
        if (mat == Material.LEATHER_LEGGINGS || mat == Material.GOLD_HELMET || mat == Material.CHAINMAIL_HELMET
                || mat == Material.IRON_HELMET || mat == Material.IRON_BOOTS)
            return 2;
        if (mat == Material.LEATHER_CHESTPLATE || mat == Material.GOLD_LEGGINGS || mat == Material.DIAMOND_BOOTS
                || mat == Material.DIAMOND_HELMET)
            return 3;
        if (mat == Material.CHAINMAIL_LEGGINGS)
            return 4;
        if (mat == Material.GOLD_CHESTPLATE || mat == Material.CHAINMAIL_CHESTPLATE || mat == Material.IRON_LEGGINGS)
            return 5;
        if (mat == Material.IRON_LEGGINGS || mat == Material.DIAMOND_LEGGINGS)
            return 6;
        if (mat == Material.DIAMOND_CHESTPLATE)
            return 8;
        return 0;
    }

    private Entity getEntityInSight(Player p) {
        Location observerPos = p.getEyeLocation();
        Vector3D observerDir = new Vector3D(observerPos.getDirection());
        Vector3D observerStart = new Vector3D(observerPos);
        Vector3D observerEnd = observerStart.add(observerDir.multiply(rangeToScan));

        Entity hit = null;

        // Get nearby entities
        for (Entity entity : p.getNearbyEntities(rangeToScan, rangeToScan, rangeToScan)) {
            // Bounding box of the given player
            if (entity instanceof Player && !HungergamesApi.getPlayerManager().getGamer(entity).isAlive())
                continue;
            Vector3D targetPos = new Vector3D(entity.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

            if (hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                if (hit == null
                        || hit.getLocation().distanceSquared(observerPos) > entity.getLocation().distanceSquared(observerPos)) {
                    hit = entity;
                }
            }
        }
        return hit;
    }

    private String getInformation(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof Player) {
            Player p = (Player) entity;
            if (countSoup) {
                int i = 0;
                for (ItemStack item : p.getInventory())
                    if (item != null && item.getType() == Material.MUSHROOM_SOUP)
                        i++;
                return String.format(informationPlayerSoup, p.getName(), p.getHealth(), p.getFoodLevel(), getArmorRating(p
                        .getInventory().getArmorContents()), HungergamesApi.getKitManager().getKitByPlayer(p).getName(), i);
            }
            return String.format(informationPlayer, p.getName(), p.getHealth(), p.getFoodLevel(), getArmorRating(p.getInventory()
                    .getArmorContents()), HungergamesApi.getKitManager().getKitByPlayer(p).getName());
        } else if (entity instanceof Item) {
            Item item = (Item) entity;
            return String.format(informationItem, HungergamesApi.getNameManager().getItemName(item.getItemStack()), item
                    .getItemStack().getAmount());
        } else if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            return String.format(informationMob, HungergamesApi.getNameManager().getName(living.getType().name()),
                    living.getHealth());
        }
        return null;
    }

    private Runnable getRunnable() {
        return new Runnable() {
            boolean zero;

            public void run() {
                zero = !zero;
                for (Player p : getMyPlayers()) {
                    ItemStack item = p.getItemInHand();
                    if (item == null || item.getType() == Material.AIR)
                        continue;
                    Entity entity = getEntityInSight(p);
                    String info = getInformation(entity);
                    if (alwaysDisplayInfo) {
                        if (info != null)
                            info += ChatColor.COLOR_CHAR + (zero ? "a" : "b");
                    } else {
                        if (currentNames.containsKey(p)) {
                            if (info == null)
                                currentNames.remove(p);
                            else if (currentNames.get(p).equals(info)) {
                                continue;
                            }
                        } else if (info != null)
                            currentNames.put(p, info);
                        else
                            continue;
                    }
                    ItemMeta meta = item.getItemMeta();
                    if (info != null)
                        meta.setDisplayName(info);
                    item = item.clone();
                    item.setItemMeta(meta);
                    net.minecraft.server.v1_7_R1.ItemStack itemstack = CraftItemStack.asNMSCopy(item);
                    PacketContainer packet = new PacketContainer(103);
                    StructureModifier<Object> mods = packet.getModifier();
                    mods.write(0, 0);
                    mods.write(1, 44 - Math.abs(p.getInventory().getHeldItemSlot() - 8));
                    mods.write(2, itemstack);
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    // Source:
    // [url]http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/[/url]
    private boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;

        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        if (Math.abs(c.x) > e.x + ad.x)
            return false;
        if (Math.abs(c.y) > e.y + ad.y)
            return false;
        if (Math.abs(c.z) > e.z + ad.z)
            return false;

        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
            return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
            return false;
        if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
            return false;

        return true;
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        currentNames.remove(event.getKilled().getPlayer());
    }

    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        if (scheduler < 0 && HungergamesApi.getHungergames().currentTime >= 0) {
            scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                    ticksPerEntityCheck, ticksPerEntityCheck);
        }
    }

    public void unregisterPlayer(Player player) {
        super.unregisterPlayer(player);
        if (HungergamesApi.getHungergames().currentTime >= 0 && this instanceof Disableable && getMyPlayers().size() == 0)
            Bukkit.getScheduler().cancelTask(scheduler = -0);
    }
}
