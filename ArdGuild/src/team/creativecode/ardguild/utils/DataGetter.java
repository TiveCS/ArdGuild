package team.creativecode.ardguild.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DataGetter {

    public static Player getOnlinePlayerFromText(String name){
        for (Player p : Bukkit.getOnlinePlayers()){
            if (p.getName().equalsIgnoreCase(name)){
                return p;
            }
        }
        return null;
    }

}
