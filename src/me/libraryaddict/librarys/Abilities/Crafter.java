package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.librarys.Misc.FakeFurnace;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Crafter extends AbilityListener implements Disableable {

    public int craftingStarItemId = Material.NETHER_STAR.getId();
    public String craftingStarItemName = ChatColor.WHITE + "Crafting Star";
    public int furnacePowderItemId = Material.BLAZE_POWDER.getId();
    public String furnacePowderItemName = ChatColor.WHITE + "Furnace Powder";
    private transient Map<ItemStack, FakeFurnace> furnaces = new HashMap<ItemStack, FakeFurnace>();

    public Crafter() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), new Runnable() {
            public void run() {
                for (FakeFurnace furnace : furnaces.values())
                    furnace.tick();
            }
        }, 1, 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
                && hasAbility(event.getPlayer())) {
            Player p = event.getPlayer();
            if (isSpecialItem(item, craftingStarItemName) && craftingStarItemId == item.getTypeId()) {
                p.openWorkbench(null, true);
            } else if (isSpecialItem(item, furnacePowderItemName) && furnacePowderItemId == item.getTypeId()) {
                if (!furnaces.containsKey(item)) {
                    furnaces.put(item, new FakeFurnace());
                }
                ((CraftPlayer) p).getHandle().openFurnace(furnaces.get(item));
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        Iterator<ItemStack> itel = event.getDrops().iterator();
        List<ItemStack> drops = new ArrayList<ItemStack>();
        while (itel.hasNext()) {
            ItemStack item = itel.next();
            if (item != null && furnaces.containsKey(item)) {
                FakeFurnace furnace = furnaces.remove(item);
                if (furnace != null) {
                    for (net.minecraft.server.v1_7_R1.ItemStack i : furnace.getContents())
                        drops.add(CraftItemStack.asBukkitCopy(i));
                }
            }
        }
        event.getDrops().addAll(drops);
    }

}
