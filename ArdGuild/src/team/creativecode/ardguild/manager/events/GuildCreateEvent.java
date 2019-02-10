package team.creativecode.ardguild.manager.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import team.creativecode.ardguild.manager.Guild;

public class GuildCreateEvent extends Event implements Cancellable {

    private Player leader;
    private Guild guild;
    private boolean isCancelled;
    private static final HandlerList handlers = new HandlerList();

    public GuildCreateEvent(Guild guild, Player leader){
        this.guild = guild;
        this.leader = leader;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlersList(){
        return handlers;
    }
}
