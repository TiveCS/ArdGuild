package team.creativecode.ardguild;

import org.bukkit.plugin.java.JavaPlugin;
import team.creativecode.ardguild.cmds.ArdGuildChatCmd;
import team.creativecode.ardguild.cmds.ArdGuildCmd;
import team.creativecode.ardguild.events.GeneralHandler;
import team.creativecode.ardguild.manager.Guild;
import team.creativecode.ardguild.utils.ConfigManager;
import team.creativecode.ardguild.utils.Language;
import team.creativecode.ardguild.utils.Placeholder;

public class Main extends JavaPlugin {

    public static Language language;
    public static Placeholder placeholder;

    @Override
    public void onEnable(){
        loadFile();
        loadCmds();
        loadEvents();

        Guild.loadGuilds();
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
