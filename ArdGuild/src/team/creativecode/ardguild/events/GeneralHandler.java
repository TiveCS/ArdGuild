package team.creativecode.ardguild.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

}
