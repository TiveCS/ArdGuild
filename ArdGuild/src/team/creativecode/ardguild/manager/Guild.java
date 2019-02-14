package team.creativecode.ardguild.manager;

import com.sun.deploy.config.Config;
import org.bukkit.*;
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
import java.util.*;

public class Guild {

    public enum GuildRank{
        LEADER;
    }

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
    private boolean hasLeader = false, friendlyfire = false;

    public static void loadGuild(Guild guild){
        guilds.put(guild.getName(), guild);
    }

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
            this.friendlyfire = false;
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
            plc.clearData();
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
                this.friendlyfire = g.getFriendlyFire();
                plc.clearData();
                placeholder();
                loadDefaultData();
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
            this.friendlyfire = this.configuration.getBoolean("info.friendlyfire");
            if (ConfigManager.contains(file, "leader")) {
                this.leader = Bukkit.getOfflinePlayer(UUID.fromString(ConfigManager.get(getFile(), "leader").toString()));
                this.hasLeader = true;
            }
            plc.clearData();
            placeholder();
            loadDefaultData();
        }
    }

    public void setInfoValue(String path, Object obj){
        ConfigManager.input(getFile(), "info." + path, obj);
    }

    public void setKill(int num){
        ConfigManager.input(getFile(), "info.kills", num);
    }

    public void setDeaths(int num){
        ConfigManager.input(getFile(), "info.deaths", num);
    }

    public void setExp(int num){
        ConfigManager.input(getFile(), "info.exp", num);
    }

    public void setLevel(int num){
        ConfigManager.input(getFile(), "info.level", num);
    }

    public void setPoint(int num){
        if (num != 0) {
            ConfigManager.input(getFile(), "info.point", num);
            plc.inputData("point", (num < 0 ? ChatColor.RED + "-" + num : ChatColor.GREEN + "+" + num));
            broadcast(Main.language.getMessages().get("guild.point-add"));
        }
    }

    public void setMobKill(int num){
        ConfigManager.input(getFile(), "info.mobkill", num);
    }

    public void switchFriendlyfire(){
        this.friendlyfire = !this.friendlyfire;
        plc.inputData("friendlyfire", this.friendlyfire == false ? ChatColor.GREEN + "Disabled" : ChatColor.RED + "Enabled");
        ConfigManager.input(getFile(), "info.friendlyfire", this.friendlyfire);
        broadcast(Main.language.getMessages().get("guild.friendlyfire"));
        loadGuilds();
    }

    public boolean chat(Player p){
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

    public void leave(Player target){
        plc.inputData("player", target.getName());
        plc.inputData("target", target.getName());
        plc.inputData("uuid", target.getUniqueId().toString());
        placeholder();
        List<String> msg = new ArrayList<String>();

        if (!getMembers().contains(target.getUniqueId().toString())){
            msg = Main.language.getMessages().get("guild.action-failed");
        }else {
            this.members.remove(target.getUniqueId().toString());
            msg = Main.language.getMessages().get("guild.leave");
            broadcast(msg);

            ConfigManager.input(this.file, "members", this.members);
            loadGuilds();
        }

        for (String ms : msg){
            target.getPlayer().sendMessage(plc.use(ms));
        }
    }

    public void kick(Player kicker, OfflinePlayer target){
        plc.inputData("kicker", kicker.getName());
        plc.inputData("player", target.getName());
        plc.inputData("target", target.getName());
        plc.inputData("uuid", target.getUniqueId().toString());
        placeholder();
        List<String> msg = new ArrayList<String>();

        if (!getMembers().contains(target.getUniqueId().toString())){
            msg = Main.language.getMessages().get("guild.action-failed");
        }else {
            this.members.remove(target.getUniqueId().toString());
            msg = Main.language.getMessages().get("guild.kick");
            broadcast(msg);

            ConfigManager.input(this.file, "members", this.members);
            loadGuilds();
        }

        if (target.isOnline()) {
            for (String ms : msg) {
                target.getPlayer().sendMessage(plc.use(ms));
            }
        }

    }

    public void invite(OfflinePlayer target){
        plc.clearData();
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

        if (target.isOnline()) {
            for (String ms : msg) {
                target.getPlayer().sendMessage(plc.use(ms));
            }
        }
        ConfigManager.input(this.file, "invite-list", this.inviteList);
        loadGuilds();
    }

    public void sethome(Player executor, String name){
        plc.clearData();
        placeholder();
        if (plugin.getConfig().getStringList("guild.sethome-disabled-world").contains(executor.getWorld().getName())){
            Main.language.sendMessage(executor, Main.placeholder.useAsList(Main.language.getMessages().get("alert.unpermitted-world")));
        }else {
            String path = "homes." + name;
            ConfigManager.input(getFile(), path + ".x", executor.getLocation().getX());
            ConfigManager.input(getFile(), path + ".y", executor.getLocation().getY());
            ConfigManager.input(getFile(), path + ".z", executor.getLocation().getZ());

            ConfigManager.input(getFile(), path + ".yaw", executor.getLocation().getYaw());
            ConfigManager.input(getFile(), path + ".pitch", executor.getLocation().getPitch());

            ConfigManager.input(getFile(), path + ".world", executor.getLocation().getWorld().getName());

            plc.inputData("player", executor.getName());
            plc.inputData("home", name);
            plc.inputData("x", executor.getLocation().getX() + "");
            plc.inputData("y", executor.getLocation().getY() + "");
            plc.inputData("z", executor.getLocation().getZ() + "");
            plc.inputData("world", executor.getLocation().getWorld().getName());

            placeholder();
            broadcast(Main.language.getMessages().get("guild.sethome"));
            loadGuilds();
            Main.language.flush("guild.sethome");
        }
    }

    public void home(Player executor, String name){
        String path = "homes." + name;
        double x,y,z;
        float yaw, pitch;
        World world;

        plc.clearData();
        plc.inputData("player", executor.getName());
        plc.inputData("home", name);

        List<String> msg = new ArrayList<String>();
        try {
            x = getConfiguration().getDouble(path + ".x");
            y = getConfiguration().getDouble(path + ".y");
            z = getConfiguration().getDouble(path + ".z");
            yaw = (float) getConfiguration().getDouble(path + ".yaw");
            pitch = (float) getConfiguration().getDouble(path + ".pitch");

            world = Bukkit.getWorld(getConfiguration().getString(path + ".world"));
            executor.teleport(new Location(world, x, y, z, yaw, pitch));

            plc.inputData("x", x + "");
            plc.inputData("y", y + "");
            plc.inputData("z", z + "");
            plc.inputData("world", world.getName());
            placeholder();
            System.out.println(msg);
            msg = Main.language.getMessages().get("guild.home");
            broadcast(msg);
        }catch(Exception e){
            msg = Main.language.getMessages().get("guild.home-not-found");
            Main.language.sendMessage(executor, plc.useAsList(msg));
            e.printStackTrace();
        }
    }

    public void delhome(Player executor, String name){
        plc.inputData("player", executor.getName());
        plc.inputData("home", name);
        placeholder();
        if (delhome(name)){
            broadcast(Main.language.getMessages().get("guild.delhome"));
        }else{
            Main.language.sendMessage(executor, plc.useAsList(Main.language.getMessages().get("guild.home-not-found")));
        }
    }

    public boolean delhome(String name){
        if (getHomeList().contains(name)){
            ConfigManager.input(getFile(), "homes." + name, null);
            loadGuilds();
            return true;
        }
        return false;
    }

    public void loadDefaultData(){

        ConfigManager.init(file, "info.level", 1);
        ConfigManager.init(file, "info.exp", 0);
        ConfigManager.init(file, "info.point", 0);
        ConfigManager.init(file, "info.kills", 0);
        ConfigManager.init(file, "info.deaths", 0);
        ConfigManager.init(file, "info.mobkill", 0);
        ConfigManager.init(file, "info.friendlyfire", false);
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

    public int getMobKill(){
        return Integer.parseInt(ConfigManager.get(getFile(), "info.mobkill").toString());
    }

    public boolean getFriendlyFire(){
        return this.friendlyfire;
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

    public Set<String> getHomes(){
        return getConfiguration().getConfigurationSection("homes").getKeys(false);
    }

    public List<String> getPlayerInChat(){
        return this.chat;
    }

    public List<String> getHomeList(){
        return new ArrayList<String>(getConfiguration().getConfigurationSection("homes").getKeys(false));
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
