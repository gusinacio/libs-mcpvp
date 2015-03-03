package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Managers.PlayerManager;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Vulture extends AbilityListener implements CommandExecutor, Disableable {
    private class KillInfo {
        private String cause;
        private Location deathLoc;
        private String killedName;
        private String killerName;
        private int time;

        public KillInfo(Location loc, Gamer killed, Gamer killer) {
            time = HungergamesApi.getHungergames().currentTime;
            killedName = killed.getName();
            if (killer == null)
                killerName = noKiller;
            else
                killerName = killer.getName();
            deathLoc = loc;
            if (killed.getPlayer().getLastDamageCause() != null)
                cause = killed.getPlayer().getLastDamageCause().getCause().name();
            else
                cause = "NONE";
            if (deathCauses.containsKey(cause))
                cause = deathCauses.get(cause);
        }

        public String getCause() {
            return cause;
        }

        public String getKilled() {
            return killedName;
        }

        public String getKiller() {
            return killerName;
        }

        public Location getLocation() {
            return deathLoc;
        }

        public String getTime() {
            return HungergamesApi.getHungergames().returnTime(HungergamesApi.getHungergames().currentTime - time);
        }
    }

    public String betCommandName = "bet";
    public String betPaidOff = ChatColor.RED + "Your bet on %s paid off!";
    private HashMap<Player, Gamer> bets = new HashMap<Player, Gamer>();
    public String betVictimAlreadyBetOn = ChatColor.RED + "%s already has a vulture betting on him!";
    public String betVictimAlreadyBettingOn = ChatColor.RED + "You are already betting on %s!";
    public String betVictimBetPlaced = ChatColor.RED + "Bet placed! If %s dies next. You get their items!";
    public String betVictimCantBetYourself = ChatColor.RED + "You cannot bet on yourself!";
    public String betVictimCurrentlyBetting = ChatColor.RED + "You currently are betting on %s!";
    public String betVictimIsDead = ChatColor.RED + "The player is already dead!";
    public String betVictimNoPlayerArgs = ChatColor.RED + "You didn't provide a player name!";
    public String betVictimNotFound = ChatColor.RED + "Player not found!";
    public String betVictimNotVulture = ChatColor.YELLOW + "You are not a vulture!";
    public String bodyCommandBodyFound = ChatColor.YELLOW
            + "Found the body of %s!\nYou adjust your compass to point at the location!\nCords: %s, %s, %s";
    public String bodyCommandBodyNotFound = ChatColor.YELLOW
            + "You look through your notebooks but you have no logs of that player!";
    public String bodyCommandName = "body";
    public String bodyCommandNoArgs = ChatColor.RED + "You need to define a player name!";
    public String bodyCommandNotVulture = ChatColor.YELLOW + "You are not a vulture!";
    public String bookName = "Death Note";
    public String[] damageCauses = new String[] { "BLOCK_EXPLOSION Block explosion", "CONTACT Cactus", "CUSTOM Unknown",
            "DROWNING Drowning", "ENTITY_ATTACK Entity Attack", "ENTITY_EXPLOSION Explosion", "FALL Fall",
            "FALLING_BLOCK Falling Block", "FIRE fire", "FIRE_TICK Fire", "LAVA Lava", "LIGHTNING Lightning", "MAGIC Magic",
            "MELTING Melting", "NONE None", "POISON Poison", "PROJECTILE Projectile", "STARVATION Starvation",
            "SUFFOCATION Suffocation", "SUICIDE Suicide", "THORNS Thorns", "VOID Void", "WITHER Wither" };
    private HashMap<String, String> deathCauses = new HashMap<String, String>();
    private ArrayList<KillInfo> kills = new ArrayList<KillInfo>();
    public String killString = "Time " + ChatColor.WHITE + "-" + ChatColor.RED + " Killed " + ChatColor.WHITE + "-"
            + ChatColor.BLUE + " Cause " + ChatColor.WHITE + "-" + ChatColor.GREEN + " Killer " + ChatColor.WHITE + "-"
            + ChatColor.YELLOW + " Location";
    public String killStringLayout = "%Time% ago " + ChatColor.WHITE + "-" + ChatColor.RED + " %Killed% " + ChatColor.WHITE + "-"
            + ChatColor.BLUE + " %Cause% " + ChatColor.WHITE + "-" + ChatColor.GREEN + " %Killer% " + ChatColor.WHITE + "-"
            + ChatColor.YELLOW + " %Location%";
    public String locationLayout = "(%s, %s, %s)";
    public String noKiller = "None";
    public String noKillsYet = ChatColor.BLUE + "There are no kills yet! Care to be the first?";

    public String[] getCommands() {
        return new String[] { bodyCommandName, betCommandName };
    }

    public boolean load(ConfigurationSection section, boolean isNewFile) {
        boolean returns = super.load(section, isNewFile);
        for (String s : damageCauses) {
            String[] split = s.split(" ");
            deathCauses.put(split[0], s.substring(1 + split[0].length()));
        }
        return returns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        PlayerManager pm = HungergamesApi.getPlayerManager();
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase(betCommandName)) {
            if (hasAbility(p)) {
                if (args.length > 0) {
                    if (!bets.containsKey(p)) {
                        Gamer victim = pm.getGamer(Bukkit.getPlayer(args[0]));
                        if (victim != null) {
                            if (victim.getPlayer() != p) {
                                if (victim.isAlive()) {
                                    if (bets.containsValue(victim)) {
                                        sender.sendMessage(String.format(betVictimAlreadyBetOn, victim.getName()));
                                        return true;
                                    }
                                    bets.put(p, victim);
                                    sender.sendMessage(String.format(betVictimBetPlaced, victim.getName()));
                                } else
                                    sender.sendMessage(betVictimIsDead);
                            } else
                                sender.sendMessage(betVictimCantBetYourself);
                        } else
                            sender.sendMessage(betVictimNotFound);
                    } else
                        sender.sendMessage(String.format(betVictimAlreadyBettingOn, bets.get(p).getName()));
                } else {
                    if (bets.containsKey(p))
                        sender.sendMessage(String.format(betVictimCurrentlyBetting, bets.get(p).getName()));
                    else
                        sender.sendMessage(betVictimNoPlayerArgs);
                }
            } else
                sender.sendMessage(betVictimNotVulture);
        } else if (cmd.getName().equalsIgnoreCase(bodyCommandName)) {
            if (hasAbility(p)) {
                if (args.length > 0) {
                    for (KillInfo kill : kills) {
                        if (kill.getKilled().toLowerCase().startsWith(args[0].toLowerCase())) {
                            Location loc = kill.getLocation().clone();
                            p.setCompassTarget(loc);
                            sender.sendMessage(String.format(bodyCommandBodyFound, kill.getKilled(), loc.getBlockX(),
                                    loc.getBlockY(), loc.getBlockZ()));
                            return true;
                        }
                    }
                    sender.sendMessage(bodyCommandBodyNotFound);
                } else
                    sender.sendMessage(bodyCommandNoArgs);
            } else
                sender.sendMessage(bodyCommandNotVulture);
        }
        return true;
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        bets.remove(event.getKilled().getPlayer());
        KillInfo killInfo = new KillInfo(event.getDropsLocation(), event.getKilled(), event.getKillerPlayer());
        kills.add(killInfo);
        Iterator<Player> playerItel = bets.keySet().iterator();
        while (playerItel.hasNext()) {
            Player p = playerItel.next();
            if (bets.get(p) == event.getKilled()) {
                p.sendMessage(String.format(betPaidOff, event.getKilled().getName()));
                Iterator<ItemStack> itel = event.getDrops().iterator();
                List<ItemStack> addBack = new ArrayList<ItemStack>();
                while (itel.hasNext() && addBack.size() == 0) {
                    ItemStack item = itel.next();
                    itel.remove();
                    HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
                    for (ItemStack itemstack : leftovers.values())
                        addBack.add(itemstack);
                }
                for (ItemStack itemstack : addBack)
                    event.getDrops().add(itemstack);
                bets.put(p, null);
                break;
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT")) {
            if (isSpecialItem(event.getItem(), bookName) && hasAbility(event.getPlayer())) {
                Player p = event.getPlayer();
                if (kills.size() == 0)
                    p.sendMessage(noKillsYet);
                else {
                    p.sendMessage(killString);
                    int k = kills.size() - 11;
                    if (k < 0)
                        k = 0;
                    for (int i = kills.size() - 1; i >= k; i--) {
                        KillInfo info = kills.get(i);
                        String sendingString = killStringLayout;
                        sendingString = sendingString.replace("%Killed%", info.getKilled());
                        sendingString = sendingString.replace("%Time%", info.getTime());
                        sendingString = sendingString.replace("%Cause%", info.getCause());
                        Location loc = info.getLocation();
                        sendingString = sendingString.replace("%Location%",
                                String.format(locationLayout, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                        sendingString = sendingString.replace("%Killer%", info.getKiller());
                        p.sendMessage(sendingString);
                    }
                }
            }
        }
    }
}
