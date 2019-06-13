package com.oxidenetwork.OxideQSRStats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oxidenetwork.OxideQSRStats.Database.*;
import com.oxidenetwork.OxideQSRStats.Database.Database.ConnectionException;
import com.oxidenetwork.OxideQSRStats.Listeners.*;
import com.oxidenetwork.OxideQSRStats.Utils.*;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class OxideQSRStats extends JavaPlugin {
    public static boolean isReady = false;
	public static String chatPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "OX-QS" + ChatColor.GRAY + "] " + ChatColor.RESET;
	static String baseCommand = "oxqs";
    @Getter static String version = "";
    
    public static OxideQSRStats instance;
	static @Getter DatabaseManager databaseManager;
    static @Getter DatabaseHelper databaseHelper;
    static @Getter Database database;
    @Getter String dbPrefix; 
    @Getter int dbTimeOut;
    @Getter boolean isDbMySQL;

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
		registerEvents();

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
	}
	
	public void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new QSR_ShopSuccessPurchaseEvent(this), this);
		pm.registerEvents(new QSR_ShopCreateEvent(this), this);
		//pm.registerEvents(new SignListener(this), this);
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
        isDbMySQL = getConfig().getBoolean("DatabaseMySQL");
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
            	OxideQSRStats.error(LangUtil.InternalMessage.INVALID_DATABASE_CONNECTION);
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            OxideQSRStats.error(LangUtil.InternalMessage.ERROR_DATABASE_CONNECTION);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            OxideQSRStats.error(LangUtil.InternalMessage.ERROR_DATABASE_SETUP);
            return false;
        }
        return true;
    }
	
    @Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase(baseCommand)) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					reloadConfig();
					sendChatMessage(sender, ChatColor.YELLOW + "" + LangUtil.InternalMessage.CONFIG_RELOAD);
					return true;
				} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					sendHelp(sender);
					return true;
				} else if (args[0].equalsIgnoreCase("save")) {
					if (!sender.hasPermission("oxqrstats.admin")) {
						sendNoPermission(sender);
						return false;
					}
					sendChatMessage(sender, "" + LangUtil.InternalMessage.CONFIG_SAVED);
					saveConfig();
					return true;
				} else if (args[0].equalsIgnoreCase("debug")) {
					if (!sender.hasPermission("oxqrstats.admin")) {
						sendNoPermission(sender);
						return false;
					}
					setDebugLevelCommand(sender, args);
					return true;
				} else if (args[0].equalsIgnoreCase("empty")) {
					if ((sender instanceof Player)) {
						   BukkitRunnable runnable = new BukkitRunnable() {
							      @Override
							      public void run() {
							    	  getOutofStockShopsForPlayer((Player) sender);
							      }
							   };
							   runnable.runTaskLater(this, 1L);
						return true;
					} else {
						sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_INGAME_PLAYER);
						return false;
					}
				} else if (args[0].equalsIgnoreCase("version")) {
					sendVersionInfo(sender);
					return true;
				}
			} else {
				sendHelp(sender);
				return false;
			}
		}
		return false;
	}

    public void sendVersionInfo(CommandSender sender) {
    	sendChatHeader(sender, "" + LangUtil.Message.CHATHEADER_VERSION_INFORMATION);
    	sendChatMessage(sender, ChatColor.YELLOW + "" + LangUtil.Message.CURRENT_VERSION + Bukkit.getPluginManager().getPlugin("OxideQSRStats-Spigot").getDescription().getVersion());
    	sendChatMessage(sender, ChatColor.YELLOW + "" + Bukkit.getPluginManager().getPlugin("OxideQSRStats-Spigot").getDescription().getDescription());
    	sendChatMessage(sender, ChatColor.YELLOW + "QuickShop Version: " + Bukkit.getPluginManager().getPlugin("QuickShop").getDescription().getVersion());
    }
    
    public void getOutofStockShopsForPlayer(Player sender) {
		boolean hasShops = false;
    	List<Shop> playerShops = new ArrayList<Shop>();
		Iterator<Shop> shops = QuickShop.instance.getShopManager().getShopIterator();
		for (Iterator<Shop> iter = shops; iter.hasNext(); ) {
		    Shop s = iter.next();
		    Player p = (Player) sender;
		    
		    if (s.getOwner().compareTo(p.getUniqueId()) == 0) {
		    	if (s.getRemainingStock() < 1) {
		    		playerShops.add(s);
		    	}
	    		hasShops = true;
		    }
		}
		
		if (hasShops) {
			if (playerShops.size() > 0) {
				sendChatMessage(sender, ChatColor.GREEN + "" + LangUtil.Message.OUT_OF_STOCK_FOR_PLAYER + ChatColor.RESET);
				for (int i = 0; i < playerShops.size(); i++) {
					
					sendChatMessage(sender, ChatColor.AQUA + MsgUtil.getItemi18n(playerShops.get(i).getItem().getType().name()) + ChatColor.GREEN + 
							" -> X:" + ChatColor.YELLOW + playerShops.get(i).getLocation().getBlockX() + ChatColor.GREEN +
							" Y:" + ChatColor.YELLOW + playerShops.get(i).getLocation().getBlockY() + ChatColor.GREEN +
							" Z:" + ChatColor.YELLOW + playerShops.get(i).getLocation().getBlockZ() + ChatColor.RESET);
				}
			} else {
				sendChatMessage(sender, ChatColor.GREEN + "" + LangUtil.Message.SHOP_ALL_STOCKED_FOR_PLAYER + ChatColor.RESET);
				return;
			}
		} else {
			sendChatMessage(sender, ChatColor.GREEN + "" + LangUtil.Message.PLAYER_HAS_NO_SHOP + ChatColor.RESET);
			return;
		}
    }
    
    public void setDebugLevelCommand(CommandSender sender, String[] args) {
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("0")) {
				OxideQSRStats.instance.getConfig().set("DebugLevel", 0);
				sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.GREEN + "0 (OFF)");
			} else if (args[1].equalsIgnoreCase("1")) {
				OxideQSRStats.instance.getConfig().set("DebugLevel", 1);
				sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.YELLOW + "1 (BASIC)");
			} else if (args[1].equalsIgnoreCase("2")) {
				OxideQSRStats.instance.getConfig().set("DebugLevel", 2);
				sendChatMessage(sender, ChatColor.WHITE + "Debug level set to " + ChatColor.RED + "2 (FULL)");
			} else {
				sendChatMessage(sender, ChatColor.RED + "Set to 0 (OFF), 1 (BASIC) or 2 (FULL). Example: /" + baseCommand + " debug 0");
			}
		} else {
			if (OxideQSRStats.instance.getConfig().getInt("DebugLevel") == 0) {
				sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.GREEN + "0 (OFF)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 1 to turn it on.");
			} else if (OxideQSRStats.instance.getConfig().getInt("DebugLevel") == 1) {
				sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.YELLOW + "1 (BASIC)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 0 to turn it off.");
			} else if (OxideQSRStats.instance.getConfig().getInt("DebugLevel") == 2){
				sendChatMessage(sender, ChatColor.WHITE + "Debug level is currently " + ChatColor.RED + "2 (FULL)" + ChatColor.WHITE + ". Use /" + baseCommand + " debug 0 to turn it off.");
			} else {
				sendChatMessage(sender, "Well this is awkward");
			}
		}
    }
    
    public void sendNoPermission(CommandSender sender) {
		sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NO_COMMAND_PERMISSION);
    }
    
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase(baseCommand)) {
			List<String> l = new ArrayList<>();

			if (args.length == 1 ) {
				l.add("?");
				l.add("help");
				l.add("empty");

				if (sender.hasPermission("oxlp.admin")) {
					l.add("reload");
					l.add("save");
					l.add("debug");
					l.add("version");
				}
			} else {
				if (args[0].equalsIgnoreCase("debug")) {
					l.add("0");
					l.add("1");
					l.add("2");
				}
			}
			return l;
		}
		return null;
	}
	
	public void sendHelp(CommandSender sender) {
		sendChatHeader(sender, "Command Help");
		sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "?" + ChatColor.RESET
				+ ChatColor.WHITE + " " + LangUtil.Message.HELP_COMMAND);
		sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "help"
				+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_COMMAND);
		sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "empty"
				+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_EMPTY_COMMAND);
		
		if (sender.hasPermission("oxqs.admin")) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "reload"
					+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_RELOAD_COMMAND);
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "save"
					+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_SAVE_COMMAND);
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "debug"
					+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_DEBUG_COMMAND);
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.YELLOW + "/" + baseCommand + " " + ChatColor.GREEN + "version"
					+ ChatColor.RESET + ChatColor.WHITE + " " + LangUtil.Message.HELP_VERSION_COMMAND);
		}
	}

	public void sendChatHeader(CommandSender sender, String description) {
		sender.sendMessage(chatPrefix + ChatColor.RESET + " - " + description);
	}
	
	public void sendChatMessage(CommandSender sender, String message) {
		sender.sendMessage(chatPrefix + ChatColor.RESET + message);
		if ((sender instanceof Player)) { // Console send this, don't send another debug line
			OxideQSRStats.debug(ChatColor.RESET + message);
		}
	}
    
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
                .filter(s -> s.toLowerCase().contains("oxideqsrstats"))
                .filter(s -> !s.contains("DebugUtil.getStackTrace"))
                .forEach(stackTrace::add);
        return String.join("\n", stackTrace);
    }

}
