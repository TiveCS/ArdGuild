package team.creativecode.ardguild.cmds;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.creativecode.ardguild.Main;
import team.creativecode.ardguild.manager.Guild;
import team.creativecode.ardguild.utils.DataConverter;
import team.creativecode.ardguild.utils.DataGetter;
import team.creativecode.ardguild.utils.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArdGuildCmd implements CommandExecutor {

    Main plugin = Main.getPlugin(Main.class);

    public List<Guild> guildlist(int page){
        List<Guild> g = new ArrayList<Guild>(Guild.guilds.values());
        List<Guild> f = new ArrayList<Guild>();
        for (int i = 0; i < 7;i++){
            int num = (i + ((page - 1)*7));
            if ((g.size() - 1) < num) {
                break;
            }else{
                f.add(g.get(num));
                continue;
            }
        }
        return f;
    }
    public void help(Player p, int page, String label){
        Placeholder plc = new Placeholder();
        plc.inputData("label", label);

        List<String> msg = new ArrayList<String>();
        TextComponent next = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6>&7]"));
        TextComponent previous = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6<&7]"));

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[]&8==============&7[ &a&lArd Guild &7]&8==============&7[]"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
        switch (page){
            case 1:
                msg.add(Main.language.getMessages().get("help.info").get(0));
                msg.add(Main.language.getMessages().get("help.create").get(0));
                msg.add(Main.language.getMessages().get("help.disband").get(0));
                msg.add(Main.language.getMessages().get("help.join").get(0));
                msg.add(Main.language.getMessages().get("help.leave").get(0));
                msg.add(Main.language.getMessages().get("help.list").get(0));
                break;
            case 2:
                msg.add(Main.language.getMessages().get("help.kick").get(0));
                msg.add(Main.language.getMessages().get("help.invite").get(0));
                msg.add(Main.language.getMessages().get("help.leader").get(0));
                msg.add(Main.language.getMessages().get("help.sethome").get(0));
                msg.add(Main.language.getMessages().get("help.home").get(0));
                msg.add(Main.language.getMessages().get("help.invite-list").get(0));
                break;
            case 3:
                msg.add(Main.language.getMessages().get("help.war").get(0));
                msg.add(Main.language.getMessages().get("help.enemy").get(0));
                msg.add(Main.language.getMessages().get("help.ally").get(0));
                msg.add(Main.language.getMessages().get("help.shop").get(0));
                msg.add(Main.language.getMessages().get("help.storage").get(0));
                msg.add(Main.language.getMessages().get("help.friendlyfire").get(0));
                break;
            case 4:
                msg.add(Main.language.getMessages().get("help.delhome").get(0));
                /*msg.add(Main.language.getMessages().get("help.enemy").get(0));
                msg.add(Main.language.getMessages().get("help.ally").get(0));
                msg.add(Main.language.getMessages().get("help.shop").get(0));
                msg.add(Main.language.getMessages().get("help.storage").get(0));
                msg.add(Main.language.getMessages().get("help.friendlyfire").get(0));*/
                break;
        }
        msg = DataConverter.colored(msg);
        for (int i = 0; i < msg.size(); i++) {
            msg.set(i, plc.use(msg.get(i)));
            p.sendMessage(msg.get(i));
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
    }

    @Deprecated
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (command.getName().equalsIgnoreCase("ardguild")){
            if (commandSender instanceof Player) {
                Player p = (Player) commandSender;
                if (p.hasPermission("ardguild.command")){
                    Guild g = new Guild(p);
                    if (args.length == 1 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
                        help(p, 1, s);
                        return true;
                    } else if (args.length == 2 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
                        try {
                            help(p, Integer.parseInt(args[1]), s);
                        } catch (Exception e) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.placeholder.use("%prefix% &cInvalid command argument!")));
                        }
                        return true;
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("invitelist")) {

                            return true;
                        }
                        if (args[0].equalsIgnoreCase("friendlyfire") || args[0].equalsIgnoreCase("ff")){
                            if (g.hasLeader() && g.getLeader().equals(p)){
                                g.switchFriendlyfire();
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("home")){
                            if (g.hasLeader() && g.getMembers().contains(p.getUniqueId().toString())){
                                g.home(p, "default");
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("sethome")){
                            if (g.getLeader().equals(p)){
                                g.sethome(p, "default");
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quit")){
                            if (g.hasLeader() && !g.getLeader().equals(p)){
                                g.leave(p);
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("disband")) {
                            if (g.getMembers().contains(p.getUniqueId().toString())) {
                                if (g.getLeader().equals(p)) {
                                    g.disband();
                                } else {
                                    Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.no-permission")));
                                }
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-belong-to-guild")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("list")) {
                            List<Guild> gd = guildlist(1);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Guild List &8(&c" + gd.size() + " Guilds&8) &bPage 1"));
                            p.sendMessage(" ");
                            for (Guild gt : gd){
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e" + gt.getName() + " &8(&6" + g.getLeader().getName() + "&8) &8[&a" + g.getMembers().size() + " Member(s)&8]"));
                            }
                            p.sendMessage(" ");
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("info")) {
                            String name = "";
                            if (!g.hasLeader()) {
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                                return true;
                            }
                            for (String ss : g.getMembers()) {
                                OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(ss));
                                if (name.equalsIgnoreCase("")) {
                                    name = "&7" + op.getName();
                                } else {
                                    name = name + "&8, &7" + op.getName();
                                }
                            }

                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m======================&8[ &e&l" + g.getName() + " &8]&7&m======================"));
                            p.sendMessage(" ");
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Leader             &e" + g.getLeader().getName()));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6K&8/&6D            &c" + g.getKills() + "&8/&4" + g.getDeaths()));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Level              &e" + g.getLevel()));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6EXP                &a" + g.getExp()));
                            p.sendMessage(" ");
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Members &8(&a" + g.getMembers().size() + "&8) &7" + name));

                            return true;
                        }
                    } else if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("delhome") || args[0].equalsIgnoreCase("deletehome")){
                            if (g.hasLeader() && g.getLeader().equals(p)){
                                g.delhome(p, args[1]);
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("home")){
                            if (g.hasLeader() && g.getMembers().contains(p.getUniqueId().toString())){
                                g.home(p, args[1]);
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("sethome")){
                            if (g.getLeader().equals(p)){
                                g.sethome(p, args[1]);
                            }else{
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("kick")){
                            if (g.getLeader().equals(p)) {
                                OfflinePlayer target = null;
                                try {
                                    if (target.equals(null)) {
                                        Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-found")));
                                        return true;
                                    }
                                } catch (Exception e) {
                                    try {
                                        target = Bukkit.getOfflinePlayer(args[1]);
                                        if (target.equals(null)){return true;}
                                    }catch(Exception ep) {
                                        Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-found")));
                                        return true;
                                    }
                                }
                                g.kick(p, target);
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("join")) {
                            if (!Guild.guilds.containsKey(args[1])) {
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                                return true;
                            }
                            if (g.getMembers().contains(p.getUniqueId().toString())) {
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("guild.join-failed-has-guild")));
                            } else {
                                Guild ng = new Guild(args[1]);
                                ng.join(p, false);
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new")) {
                            if (Guild.guilds.containsKey(args[1])){
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                                return true;
                            }
                            if (new Guild(p).hasLeader()) {
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("guild.create-failed-has-guild")));
                                return true;
                            }
                            if (args[1].length() > plugin.getConfig().getInt("guild.max-name-length")){
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("guild.create-failed-overlength")));
                                return true;
                            }
                            double vault = plugin.getConfig().getDouble("guild.vault-require");
                            if ((plugin.getConfig().getBoolean("hook.vault"))){
                                if (Main.economy.getBalance(p) >= vault) {
                                    Main.economy.withdrawPlayer(p, vault);
                                    Main.placeholder.inputData("money", vault + "");
                                    Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.vault-take")));
                                    new Guild(p, args[1]);

                                }else{
                                    Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.vault-not-enough")));
                                }
                            }else{
                                new Guild(p, args[1]);
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("inv")) {
                            OfflinePlayer target = null;
                            if (g.getLeader().getPlayer().equals(p)) {
                                target = DataGetter.getOnlinePlayerFromText(args[1]);
                                try {
                                    if (target.equals(null)) {
                                        Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-found")));
                                        return true;
                                    }
                                } catch (Exception e) {
                                    try {
                                        target = Bukkit.getOfflinePlayer(args[1]);
                                        if (target.equals(null)){return true;}
                                    }catch(Exception ep) {
                                        Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-found")));
                                        return true;
                                    }
                                }
                                g.invite(target);
                                return true;
                            } else {
                                Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.action-failed")));
                                return true;
                            }
                        }
                    } else {
                        Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.not-found")));
                        return true;
                    }
                }else{
                    Main.language.sendMessage(p, Main.placeholder.useAsList(Main.language.getMessages().get("alert.no-permission")));
                    return true;
                }
            }
        }

        return false;
    }
}
