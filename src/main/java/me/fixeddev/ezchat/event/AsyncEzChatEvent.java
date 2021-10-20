package me.fixeddev.ezchat.event;

import me.fixeddev.ezchat.format.ChatFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class AsyncEzChatEvent extends Event implements Cancellable {
    private final AsyncPlayerChatEvent event;
    private final ChatFormat playerChatFormat;

    private Set<Player> recipients;

    private boolean cancelled;
    private static final HandlerList handlerList = new HandlerList();


    public AsyncEzChatEvent(AsyncPlayerChatEvent event, ChatFormat chatFormat, Set<Player> recipients) {
        super(event.isAsynchronous());
        this.event = event;

        this.playerChatFormat = chatFormat;
        this.recipients = recipients;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public ChatFormat getPlayerChatFormat() {
        return playerChatFormat;
    }

    public AsyncPlayerChatEvent getEvent() {
        return event;
    }

    public String getMessage() {
        return event.getMessage();
    }

    public void setMessage(String message) {
        event.setMessage(message);
    }

    public Set<Player> getRecipients() {
        return event.getRecipients();
    }

    public Player getPlayer() {
        return event.getPlayer();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

}
