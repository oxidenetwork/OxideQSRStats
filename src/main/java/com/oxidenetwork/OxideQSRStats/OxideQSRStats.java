package com.oxidenetwork.OxideQSRStats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oxidenetwork.OxideQSRStats.Database.*;
import com.oxidenetwork.OxideQSRStats.Database.Database.ConnectionException;
import com.oxidenetwork.OxideQSRStats.Listeners.QSR_ShopPurchaseEvent;
import com.oxidenetwork.OxideQSRStats.Utils.*;

import lombok.Getter;

@SuppressWarnings("unused")
public class OxideQSRStats extends JavaPlugin {
    public static boolean isReady = false;
    public static String version = "";
    public static OxideQSRStats instance;
	@Getter DatabaseManager databaseManager;
    @Getter DatabaseHelper databaseHelper;
    @Getter Database database;
    @Getter String dbPrefix; 
    @Getter int dbTimeOut;

    @Getter private File configFile = new File(getDataFolder(), "config.yml");
    @Getter private File messagesFile = new File(getDataFolder(), "messages.yml");
    @Getter private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter private Random random = new Random();
    
    public static OxideQSRStats getPlugin() {
        return getPlugin(OxideQSRStats.class);
    }
    public static FileConfiguration config() {
        return OxideQSRStats.getPlugin().getConfig();
    }
    
    @Override
    public void onLoad() {
        instance = this;
    }
    
	@Override
    public void onEnable() {
        version = getDescription().getVersion();
        Thread initThread = new Thread(this::init, "OxideQSRStats-Thread");
        initThread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            getLogger().severe("OxideQSRStats failed to load properly: " + e.getMessage() + ".");
        });
        initThread.start();
    }
	
	public void init() {
		initConfigAndLang();
		setupDatabase();
		databaseManager = new DatabaseManager(this, database);
		getLogger().info("I'm done loading.");
		registerEvents();
	}
	
	public void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new QSR_ShopPurchaseEvent(this), this);
		OxideQSRStats.debug("Events Registered");
	}

	private void initConfigAndLang() {
        if (!configFile.exists()) {
            LangUtil.saveConfig();
            reloadConfig();
        }
        if (!messagesFile.exists()) {
            LangUtil.saveMessages();
            LangUtil.reloadMessages();
        }

        ConfigUtil.migrate();

        try {
            getConfig();
        } catch (IllegalArgumentException e) {
            OxideQSRStats.error(LangUtil.InternalMessage.INVALID_CONFIG + ": " + e.getMessage());
            try {
                new Yaml().load(FileUtils.readFileToString(getConfigFile(), Charset.forName("UTF-8")));
            } catch (IOException io) {
            	OxideQSRStats.error(io.getMessage());
            }
            return;
        }
        dbPrefix = getConfig().getString("DatabasePrefix");
        dbTimeOut = getConfig().getInt("DatabaseTimeOut");
        OxideQSRStats.info("Loaded Config Version: " + getConfig().getString("ConfigVersion"));
	}
	
    private boolean setupDatabase() {
        try {
            DatabaseCore dbCore;
            
            if (getConfig().getBoolean("DatabaseMySQL")) {
                // MySQL database - Required database be created first.
                String dbPrefix = getConfig().getString("DatabasePrefix");
                if (dbPrefix == null || dbPrefix.equals("none"))
                    dbPrefix = "";
                String user = getConfig().getString("DatabaseUser");
                String pass = getConfig().getString("DatabasePassword");
                String host = getConfig().getString("DatabaseHost");
                String port = getConfig().getString("DatabasePort");
                String database = getConfig().getString("DatabaseName");
                boolean useSSL = getConfig().getBoolean("DatabaseUseSSL");
                dbCore = new MySQLCore(host, user, pass, database, port, useSSL);
            } else {
                // SQLite database - Doing this handles file creation
                dbCore = new SQLiteCore(new File(this.getDataFolder(), "stats.db"));
            }
            database = new Database(dbCore);
            if (database.getConnection().isValid(10)) {
	            // Make the database up to date
	            databaseHelper = new DatabaseHelper(this, database);
            } else {
            	OxideQSRStats.error("setupDatabase - Database connection is not valid!");
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            OxideQSRStats.error("Error connecting to database.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            OxideQSRStats.error("Error setting up database.");
            return false;
        }
        return true;
    }
	
    // log messages. Thank you DiscordSRV this works really well
    public static void info(LangUtil.InternalMessage message) {
        info(message.toString());
    }
    public static void info(String message) {
        getPlugin().getLogger().info(message);
    }
    public static void warning(LangUtil.InternalMessage message) {
        warning(message.toString());
    }
    public static void warning(String message) {
        getPlugin().getLogger().warning(message);
    }
    public static void error(LangUtil.InternalMessage message) {
        error(message.toString());
    }
    public static void error(String message) {
        getPlugin().getLogger().severe(message);
    }
    
	public static void debug(String message) {
        // return if plugin is not in debug mode
        if (getPlugin().getConfig().getInt("DebugLevel") == 0) return;

        getPlugin().getLogger().info("[DEBUG] " + message + (getPlugin().getConfig().getInt("DebugLevel") >= 2 ? "\n" + getStackTrace() : ""));
    }
    
	public static String getStackTrace() {
        List<String> stackTrace = new LinkedList<>();
        stackTrace.add("Stack trace @ debug call (THIS IS NOT AN ERROR)");
        Arrays.stream(ExceptionUtils.getStackTrace(new Throwable()).split("\n"))
                .filter(s -> s.toLowerCase().contains("oxidediscordapi"))
                .filter(s -> !s.contains("DebugUtil.getStackTrace"))
                .forEach(stackTrace::add);
        return String.join("\n", stackTrace);
    }
	
}
