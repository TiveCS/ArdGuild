package team.creativecode.ardguild.manager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import team.creativecode.ardguild.Main;
import team.creativecode.ardguild.manager.events.GuildCreateEvent;
import team.creativecode.ardguild.utils.ConfigManager;
import team.creativecode.ardguild.utils.Placeholder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Guild {

    public static HashMap<String, Guild> guilds = new HashMap<String, Guild>();
    public static Main plugin = Main.getPlugin(Main.class);
    public static File folder = new File(plugin.getDataFolder() + "/Guild");

    private List<String> chat = new ArrayList<String>();
    private Placeholder plc = new Placeholder();
    private List<String> inviteList = new ArrayList<String>();
    private List<String> members = new ArrayList<String>();
    private String name;
    private OfflinePlayer leader;
    private File file;
    private FileConfiguration configuration;
    private boolean hasLeader = false;

    public static void loadGuilds(){
        guilds.clear();
        for (File file : folder.listFiles()){
            String name = file.getName().split("[.]")[0];
            Guild g = new Guild(name);
            if (g.hasLeader() && g.getMembers().size() > 0){
                guilds.put(name, g);
            }else{
                continue;
            }
        }
        plugin.getServer().getConsoleSender().sendMessage("[" + plugin.getDescription().getName() + "] " + guilds.size() + " guild file(s) has been loaded");
    }

    // For creating guild
    public Guild(OfflinePlayer executor, String name) {
        GuildCreateEvent event = new GuildCreateEvent(this, executor.getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.name = name;
            this.leader = executor;
            this.hasLeader = true;
            this.members = new ArrayList<String>();
            members.add(executor.getUniqueId().toString());

            file = new File(plugin.getDataFolder() + "/Guild", name + ".yml");
            if (!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.configuration = YamlConfiguration.loadConfiguration(file);

            ConfigManager.init(file, "leader", this.leader.getUniqueId().toString());
            ConfigManager.init(file, "members", members);

            loadDefaultData();
            placeholder();
            guilds.put(this.name, this);
            if (executor.isOnline()) {
                List<String> list = Main.language.getMessages().get("guild.create");
                for (String s : list) {
                    executor.getPlayer().sendMessage(plc.use(s));
                }
            }
        }
    }

    // For getting guild
    public Guild(OfflinePlayer player){
        for (String n : guilds.keySet()){
            Guild g = guilds.get(n);
            String uuid = player.getUniqueId().toString();
            if (g.getMembers().contains(uuid)){
                this.members = g.getMembers();
                this.chat = g.getPlayerInChat();
                this.file = g.getFile();
                this.configuration = g.getConfiguration();
                this.leader = g.getLeader();
                this.name = g.getName();
                this.hasLeader = true;
                placeholder();
                break;
            }else{
                this.hasLeader = false;
            }
        }
    }

    // For getting guild
    public Guild(String name){
        this.name = name;
        this.file = new File(plugin.getDataFolder() + "/Guild", name + ".yml");
        if (file.exists()) {
            this.configuration = YamlConfiguration.loadConfiguration(file);
            this.inviteList = new ArrayList<String>(this.configuration.getStringList("invite-list"));
            this.chat = new ArrayList<String>(this.configuration.getStringList("chat"));
            this.members = this.configuration.getStringList("members");
            if (ConfigManager.contains(file, "leader")) {
                this.leader = Bukkit.getOfflinePlayer(UUID.fromString(ConfigManager.get(getFile(), "leader").toString()));
                this.hasLeader = true;
            }
            placeholder();
        }
    }

    public boolean chat(Player p){
        System.out.println(this.chat);
        System.out.println( " ");
        System.out.println(getPlayerInChat());
        if (getPlayerInChat().contains(p.getUniqueId().toString())){
            this.chat.remove(p.getUniqueId().toString());
            ConfigManager.input(getFile(), "chat", getPlayerInChat());
            p.sendMessage(plc.use(Main.language.getMessages().get("guild.chat").get(1)));
            return true;
        }else{
            this.chat.add(p.getUniqueId().toString());
            ConfigManager.input(getFile(), "chat", getPlayerInChat());
            p.sendMessage(plc.use(Main.language.getMessages().get("guild.chat").get(0)));
            return false;
        }
    }

    public void disband(){
        try {
            if (getFile().exists()) {
                broadcast(Main.language.getMessages().get("guild.disband"));
                getFile().delete();
                loadGuilds();
            }
        }catch (Exception e){}
    }

    public void join(OfflinePlayer target, boolean bypass){
        plc.inputData("player", target.getName());
        plc.inputData("target", target.getName());
        plc.inputData("uuid", target.getUniqueId().toString());
        List<String> msg = new ArrayList<String>();

        if (this.inviteList.contains(target.getUniqueId().toString())){
            this.inviteList.remove(target.getUniqueId().toString());
            this.members.add(target.getUniqueId().toString());
            msg = Main.language.getMessages().get("guild.join");
        }else{
            if (bypass){
                this.members.add(target.getUniqueId().toString());
                msg = Main.language.getMessages().get("guild.join");
            }else{
                if (target.getPlayer().isOnline()) {
                    Main.language.sendMessage(target.getPlayer(), "guild.join-failed-not-invited");
                }
            }
        }
        ConfigManager.input(getFile(), "members", this.members);
        ConfigManager.input(getFile(), "invite-list", this.inviteList);

        broadcast(msg);
        loadGuilds();
    }

    public void invite(OfflinePlayer target){
        plc.inputData("player", target.getName());
        plc.inputData("target", target.getName());
        plc.inputData("uuid", target.getUniqueId().toString());
        placeholder();
        List<String> msg = new ArrayList<String>();

        if (this.inviteList.contains(target.getUniqueId().toString())){
            this.inviteList.remove(target.getUniqueId().toString());
            msg = Main.language.getMessages().get("guild.invite-revoke");
        }else {
            msg = Main.language.getMessages().get("guild.invite");
            this.inviteList.add(target.getUniqueId().toString());
        }

        broadcast(msg);

        for (String ms : msg){
            target.getPlayer().sendMessage(plc.use(ms));
        }
        ConfigManager.input(this.file, "invite-list", this.inviteList);
        loadGuilds();
    }

    public void loadDefaultData(){

        ConfigManager.init(file, "info.level", 1);
        ConfigManager.init(file, "info.exp", 0);
        ConfigManager.init(file, "info.point", 0);
        ConfigManager.init(file, "info.kills", 0);
        ConfigManager.init(file, "info.deaths", 0);
    }

    public void broadcast(List<String> msg){
        for (String member : getMembers()){
            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(member));
            if (op.isOnline()){
                for (String ms : msg){
                    op.getPlayer().sendMessage(plc.use(ms));
                }
            }
        }
    }

    public void broadcast(String msg){
        for (String member : getMembers()){
            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(member));
            if (op.isOnline()){
                op.getPlayer().sendMessage(msg);
            }
        }
    }

    public OfflinePlayer getLeader(){
        return this.leader;
    }

    public List<String> getMembers(){
        return this.members;
    }

    public FileConfiguration getConfiguration(){
        return this.configuration;
    }

    public String getName(){
        return this.name;
    }

    public File getFile(){
        return this.file;
    }

    public List<String> getInviteList(){
        return this.inviteList;
    }

    public int getPoint(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.point").toString());
    }

    public int getExp(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.exp").toString());
    }

    public int getLevel(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.level").toString());
    }

    public int getKills(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.kills").toString());
    }

    public int getDeaths(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.deaths").toString());
    }

    public Placeholder getPlaceholder(){
        return this.plc;
    }

    public List<String> getPlayerInChat(){
        return this.chat;
    }

    public void placeholder() {
        plc.inputData("guild", this.getName());
        plc.inputData("leader", this.getLeader().getName());
        plc.inputData("member-amount", this.getMembers().size() + "");
    }

    public boolean hasLeader(){
        return hasLeader;
    }

}
