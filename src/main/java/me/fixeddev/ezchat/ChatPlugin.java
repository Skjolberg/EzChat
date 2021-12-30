package me.fixeddev.ezchat;

import java.io.File;
import java.net.URLClassLoader;
import java.util.function.Supplier;
import java.util.logging.Level;

import me.fixeddev.ezchat.commands.CommandRegistry;
import me.fixeddev.ezchat.dependency.DependencyDownloader;
import me.fixeddev.ezchat.format.BaseChatFormatManager;
import me.fixeddev.ezchat.format.ChatFormatManager;
import me.fixeddev.ezchat.listener.AbstractChatListener;
import me.fixeddev.ezchat.listener.HighChatListener;
import me.fixeddev.ezchat.listener.HighestChatListener;
import me.fixeddev.ezchat.listener.LowChatListener;
import me.fixeddev.ezchat.listener.LowestChatListener;
import me.fixeddev.ezchat.listener.MonitorChatListener;
import me.fixeddev.ezchat.listener.NormalChatListener;
import me.fixeddev.ezchat.uuid.BasicUUIDCache;
import me.fixeddev.ezchat.uuid.DelegateUUIDCache;
import me.fixeddev.ezchat.uuid.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatPlugin extends JavaPlugin {

    private DependencyDownloader downloader;

    private ChatFormatManager chatFormatManager;

    private Supplier<UUIDCache> uuidCacheSupplier = BasicUUIDCache::new;

    private UUIDCache uuidCache;

    private Runnable unregisterCommands;

    @Override
    public void onLoad() {
        // we know for sure that at least the java plugin classloader is an url class loader
        //downloader = new DependencyDownloader((URLClassLoader) this.getClassLoader(), new File(getDataFolder(), "dependencies"), getLogger());
    }

    @Override
    public void onDisable() {
        unregisterCommands.run();
    }

    @Override
    public void onEnable() {
        //downloader.downloadDependencies(); never start it

        saveDefaultConfig();

        chatFormatManager = new BaseChatFormatManager(this);
        Bukkit.getServicesManager().register(ChatFormatManager.class, chatFormatManager, this, ServicePriority.Normal);

        AbstractChatListener chatListener = getChatListener();
        if (chatListener != null) {
            getServer().getPluginManager().registerEvents(chatListener, this);
        }

        registerCommands();
    }

    public DependencyDownloader getDownloader() {
        return downloader;
    }

    public UUIDCache getUuidCache() {
        if (uuidCache == null) {
            uuidCache = uuidCacheSupplier.get();
        }

        return uuidCache;
    }

    public void setUuidCache(UUIDCache uuidCache) {
        this.uuidCache = uuidCache;
    }

    public void setUuidCacheSupplier(Supplier<UUIDCache> cacheSupplier) {
        this.uuidCacheSupplier = cacheSupplier;
    }

    public Supplier<UUIDCache> getUuidCacheSupplier() {
        return uuidCacheSupplier;
    }

    private void registerCommands() {
        CommandRegistry registry = new CommandRegistry(this, chatFormatManager);
        registry.registerCommands();

        unregisterCommands = registry::unregisterCommands;
    }

    private AbstractChatListener getChatListener() {
        FileConfiguration config = getConfig();
        boolean alternativeHandling = config.getBoolean("alternative-handling", false);
        EventPriority eventPriority = EventPriority.valueOf(config.getString("chat-event-priority", "NORMAL"));

        switch (eventPriority) {
            case LOWEST:
                return new LowestChatListener(chatFormatManager, alternativeHandling);
            case LOW:
                return new LowChatListener(chatFormatManager, alternativeHandling);
            case NORMAL:
                return new NormalChatListener(chatFormatManager, alternativeHandling);
            case HIGH:
                return new HighChatListener(chatFormatManager, alternativeHandling);
            case HIGHEST:
                return new HighestChatListener(chatFormatManager, alternativeHandling);
            case MONITOR:
                return new MonitorChatListener(chatFormatManager, alternativeHandling);
            default:
                getLogger().log(Level.SEVERE, "Unknown priority {0}, disabling plugin.", eventPriority);
                this.setEnabled(false);
                break;
        }

        return null;
    }

    public static UUIDCache registerCache() {
        ChatPlugin plugin = JavaPlugin.getPlugin(ChatPlugin.class);

        UUIDCache uuidCache = plugin.getUuidCache();
        Bukkit.getServicesManager().register(UUIDCache.class, new DelegateUUIDCache(plugin::getUuidCache), plugin, ServicePriority.Normal);

        return uuidCache;
    }

}
