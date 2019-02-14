package team.creativecode.ardguild;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import team.creativecode.ardguild.cmds.ArdGuildChatCmd;
import team.creativecode.ardguild.cmds.ArdGuildCmd;
import team.creativecode.ardguild.events.GeneralHandler;
import team.creativecode.ardguild.manager.Guild;
import team.creativecode.ardguild.utils.ConfigManager;
import team.creativecode.ardguild.utils.Language;
import team.creativecode.ardguild.utils.Placeholder;

import java.util.Random;

public class Main extends JavaPlugin {

    public static Economy economy = null;
    public static Language language = null;
    public static Placeholder placeholder = null;

    @Override
    public void onEnable(){
        loadFile();
        loadCmds();
        loadEvents();

        if (getConfig().getBoolean("hook.vault")) {
            if (setupEconomy()) {
                getServer().getConsoleSender().sendMessage("[" + getDescription().getName() + "] Vault has been hooked");
            }
        }
        Guild.loadGuilds();
        if (getConfig().getBoolean("hook.vault")) {
            placeholder.inputData("vault-money", getConfig().getDouble("guild.vault-require") + "");
        }
    }

    public static boolean chance(double ch){
        return ch >= new Random().nextDouble() * 100;
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void loadEvents() {
        getServer().getPluginManager().registerEvents(new GeneralHandler(), this);
    }

    private void loadCmds(){

        getCommand("ardguild").setExecutor(new ArdGuildCmd());
        getCommand("ardguildchat").setExecutor(new ArdGuildChatCmd());
    }

    public void loadFile(){
        getConfig().options().copyDefaults(true);
        saveConfig();

        ConfigManager.createFolder(this.getDataFolder() + "/Guild");
        if (Language.defFile.exists()){
            try {
                String fv = ConfigManager.get(Language.defFile, "file-version").toString();
                if (!ConfigManager.get(Language.defFile, "file-version").toString().equals(this.getDescription().getVersion())) {
                    Language.defFile.delete();
                }
            }catch(Exception e){e.printStackTrace();}
        }

        Language.loadLanguages();
        language = Language.languages.get(getConfig().getString("language"));
        placeholder = new Placeholder();
    }

}
