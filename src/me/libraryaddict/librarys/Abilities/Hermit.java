package me.libraryaddict.librarys.Abilities;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Hermit extends AbilityListener implements Disableable {
    public String failedToFindLocation = ChatColor.GREEN + "So sorry! I was unable to find a location for you to spawn at!";
    public int timesToLoop = 100;

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
            public void run() {
                double borderSize = HungergamesApi.getConfigManager().getMainConfig().getBorderSize() - 15;
                Location spawn = HungergamesApi.getHungergames().world.getSpawnLocation();
                for (Player p : getMyPlayers()) {
                    // Now lets make it so he never spawns less then half the border size
                    for (int i = 0; i < timesToLoop; i++) {
                        double addX = new Random().nextInt((int) (borderSize / 2)) + (borderSize / 2);
                        double addZ = new Random().nextInt((int) (borderSize / 2)) + (borderSize / 2);
                        if (new Random().nextBoolean())
                            addX = -addX;
                        if (new Random().nextBoolean())
                            addZ = -addZ;
                        Block block = spawn.getWorld()
                                .getHighestBlockAt((int) (spawn.getX() + addX), (int) (spawn.getZ() + addZ));
                        if (!block.getChunk().isLoaded()) {
                            block.getChunk().load();
                        }
                        while (block.getRelative(BlockFace.UP).getType() != Material.AIR && !block.isLiquid())
                            block = block.getRelative(BlockFace.UP);
                        if (block.isLiquid()) {
                            if (i + 1 == timesToLoop)
                                p.sendMessage(failedToFindLocation);
                            continue;
                        }
                        p.teleport(block.getLocation().clone().add(0, 1.5, 0));
                        break;
                    }

                }
            }
        });
    }

}
