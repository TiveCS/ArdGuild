package team.creativecode.ardguild.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.creativecode.ardguild.Main;
import team.creativecode.ardguild.manager.Guild;

public class ArdGuildChatCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            if (command.getName().equalsIgnoreCase("ardguildchat")){
                Player p = (Player) commandSender;
                Guild g = new Guild(p);
                if (g.getMembers().contains(p.getUniqueId().toString())){
                    g.chat(p);
                }
                return true;
            }
        }
        return false;
    }
}
