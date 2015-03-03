package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Madman extends AbilityListener implements Disableable {
    public int radius = 20;
    public int rateOfMadness = 5;

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        if (HungergamesApi.getHungergames().currentTime > HungergamesApi.getConfigManager().getMainConfig()
                .getTimeForInvincibility())
            for (Player p : getMyPlayers()) {
                ArrayList<Player> nearby = new ArrayList<Player>();
                for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
                    if (e instanceof Player && HungergamesApi.getPlayerManager().getGamer(e).isAlive()) {
                        nearby.add((Player) e);
                    }
                }
                if (nearby.size() > 1) {
                    for (Player player : nearby) {
                        int currentTicks = 0;
                        int amp = 0;
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                                amp = effect.getAmplifier();
                                currentTicks = effect.getDuration();
                                break;
                            }
                        }
                        if (currentTicks >= 300) {
                            currentTicks -= 200;
                            amp++;
                        }
                        currentTicks += (20 + (nearby.size() * rateOfMadness));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, currentTicks, amp), true);
                    }
                }
            }
    }

}
