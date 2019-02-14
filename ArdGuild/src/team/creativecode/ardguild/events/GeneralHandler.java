package team.creativecode.ardguild.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.creativecode.ardguild.Main;
import team.creativecode.ardguild.manager.Guild;

public class GeneralHandler implements Listener {

    Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        if (!event.isCancelled()){
            Guild g = new Guild(event.getPlayer());
            if (g.hasLeader() && g.getPlayerInChat().contains(event.getPlayer().getUniqueId().toString())){
                event.setCancelled(true);
                String send = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("guild.guild-chat-format"));
                g.placeholder();
                g.getPlaceholder().inputData("player", event.getPlayer().getName());
                g.getPlaceholder().inputData("message", event.getMessage());
                send = g.getPlaceholder().use(send);
                g.broadcast(send);
            }
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event){
        try {
            Player victim = event.getEntity();
            Player attacker = victim.getKiller();

            Guild gv = new Guild(victim), ga = new Guild(attacker);
            gv.setDeaths(gv.getDeaths() + 1);
            ga.setKill(ga.getKills() + 1);

            ga.setPoint(ga.getPoint() + 1);
            gv.getConfiguration().save(gv.getFile());
            ga.getConfiguration().save(ga.getFile());
        }catch(Exception e){}
    }

    @EventHandler
    public void mobdeath(EntityDeathEvent event){
        if (!(event.getEntity() instanceof Player)){
            try {
                Player p = event.getEntity().getKiller();
                Guild g = new Guild(p);

                g.setMobKill(g.getMobKill() + 1);
            }catch(Exception e){}
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event){
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Guild g = new Guild(victim);
            if (!g.getFriendlyFire()){
                if (event.getDamager() instanceof Player) {
                    Player attacker = (Player) event.getDamager();
                    if (g.getMembers().contains(attacker.getUniqueId().toString())) event.setCancelled(true);
                }

                if (event.getDamager() instanceof Projectile){
                    Projectile proj = (Projectile) event.getDamager();
                    if (g.getMembers().contains(((Player) proj.getShooter()).getUniqueId().toString())){event.setCancelled(true);}
                }
            }
        }
    }

}
