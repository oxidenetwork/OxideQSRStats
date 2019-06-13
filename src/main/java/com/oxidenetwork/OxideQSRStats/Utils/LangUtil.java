/*
#
#   This file has been inspired by the guys at DiscordSRV. They make a very awesome
#   Discord plugin for Spigot that I recommend to everybody. Since I'm a beginner
#   coder I learned a lot from their code. Copied a lot of the structure of DiscordSRV
#   because it is so damm good.
#
#   You should totaly check them out at https://www.spigotmc.org/resources/discordsrv.18494/
#   or their github https://github.com/Scarsz/DiscordSRV
#
*/

package com.oxidenetwork.OxideQSRStats.Utils;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LangUtil {

    public enum Language {

        EN("English");

        @Getter final String code;
        @Getter final String name;

        Language(String name) {
            this.code = name().toLowerCase();
            this.name = name;
        }

    }

    /**
     * Fixed messages
     */
    @SuppressWarnings("serial")
    public enum InternalMessage {
		CANNOT_LOAD_CONFIG_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Configuration file cannot be loaded.");
        }}), CANNOT_LOAD_LANG_FILE_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Language file cannot be opened");
        }}), LANGUAGE_INITIALIZED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Language file loaded");
        }}), INVALID_CONFIG(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Invalid Config files");
        }}), INVALID_DATABASE_CONNECTION(new HashMap<Language, String>() {{ 
        	put(Language.EN, "setupDatabase - Database connection is not valid!");
        }}), ERROR_DATABASE_CONNECTION(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Error connecting to database!");
        }}), ERROR_DATABASE_SETUP(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Error setting up database!");
        }}), CONFIG_RELOAD(new HashMap<Language, String>() {{ 
        	put(Language.EN, "OxideQSRStats Config Reloaded.");
        }}), CONFIG_SAVED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Config file saved");
        }});
    	
    	
    	
    	
    	
        @Getter private final Map<Language, String> definitions;
        InternalMessage(Map<Language, String> definitions) {
            this.definitions = definitions;

            // warn about if a definition is missing any translations for messages
            for (Language language : Language.values())
                if (!definitions.containsKey(language))
                    OxideQSRStats.debug("Language " + language.getName() + " missing from definitions for " + name());
        }

        @Override
        public String toString() {
            return definitions.getOrDefault(userLanguage, definitions.get(Language.EN));
        }

    }

    public enum Message {

    	NOT_INGAME_PLAYER("NotIngamePlayer", true),
    	OUT_OF_STOCK_FOR_PLAYER("OutOfStockForPlayer", true),
    	SHOP_ALL_STOCKED_FOR_PLAYER("ShopAllStockedForPlayer", true),
    	PLAYER_HAS_NO_SHOP("PlayerHasNoShops", true),
    	NO_COMMAND_PERMISSION("NoCommandPermission", true),
    	HELP_COMMAND("HelpCommand", true),
    	HELP_EMPTY_COMMAND("HelpEmptyCommand", true),
    	HELP_RELOAD_COMMAND("HelpReloadCommand", true),
    	HELP_SAVE_COMMAND("HelpSaveCommand", true),
    	HELP_DEBUG_COMMAND("HelpDebugCommand", true),
    	HELP_VERSION_COMMAND("HelpVersionCommand", true),
    	CURRENT_VERSION("CurrentVersion", true),
    	CHATHEADER_VERSION_INFORMATION("ChatHeaderVersionInformation", true);

        @Getter private final String keyName;
        @Getter private final boolean translateColors;

        Message(String keyName, boolean translateColors) {
            this.keyName = keyName;
            this.translateColors = translateColors;
        }

        @Override
        public String toString() {
            String message = messages.getOrDefault(this, "");
            return message;
        }

    }

    @Getter private static final Map<Message, String> messages = new HashMap<>();
    @Getter private static final Yaml yaml = new Yaml();
    @Getter private static Language userLanguage;
    static {
        String languageCode = System.getProperty("user.language").toUpperCase();

        String forcedLanguage = OxideQSRStats.config().getString("ForcedLanguage");
        if (StringUtils.isNotBlank(forcedLanguage) && !forcedLanguage.equalsIgnoreCase("none")) {
            if (forcedLanguage.length() == 2) {
                languageCode = forcedLanguage.toUpperCase();
            } else {
                for (Language language : Language.values()) {
                    if (language.getName().equalsIgnoreCase(forcedLanguage)) {
                        languageCode = language.getCode();
                    }
                }
            }
        }

        try {
            userLanguage = Language.valueOf(languageCode);
        } catch (Exception e) {
            userLanguage = Language.EN;

            OxideQSRStats.info("Unknown user language " + languageCode.toUpperCase() + ".");
            //OxideQSRStats.info("If you fluently speak " + languageCode.toUpperCase() + " as well as English, see the GitHub repo to translate it!");
        }

        saveConfig();
        saveMessages();
        reloadMessages();

        OxideQSRStats.info(InternalMessage.LANGUAGE_INITIALIZED + userLanguage.getName());
    }

    private static void saveResource(String resource, File destination, boolean overwrite) {
        if (destination.exists() && !overwrite) return;

        try {
            FileUtils.copyInputStreamToFile(OxideQSRStats.class.getResourceAsStream(resource), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        saveConfig(false);
    }
    public static void saveConfig(boolean overwrite) {
        File destination = OxideQSRStats.getPlugin().getConfigFile();
        String resource = "/config/" + userLanguage.getCode() + ".yml";

        saveResource(resource, destination, overwrite);
    }

    public static void saveMessages() {
        saveMessages(false);
    }
    public static void saveMessages(boolean overwrite) {
        String resource = "/messages/" + userLanguage.getCode() + ".yml";
        File destination = OxideQSRStats.getPlugin().getMessagesFile();

        saveResource(resource, destination, overwrite);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void reloadMessages() {
        if (!OxideQSRStats.getPlugin().getMessagesFile().exists()) return;

        try {
            for (Map.Entry entry : (Set<Map.Entry>) yaml.loadAs(FileUtils.readFileToString(OxideQSRStats.getPlugin().getMessagesFile(), Charset.forName("UTF-8")), Map.class).entrySet()) {
                for (Message message : Message.values()) {
                    if (message.getKeyName().equalsIgnoreCase((String) entry.getKey())) {
                        messages.put(message, (String) entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            OxideQSRStats.error("Failed loading " + OxideQSRStats.getPlugin().getMessagesFile().getPath() + ": " + e.getMessage());

            File movedToFile = new File(OxideQSRStats.getPlugin().getMessagesFile().getParent(), "messages-" + OxideQSRStats.getPlugin().getRandom().nextInt(100) + ".yml");
            try { FileUtils.moveFile(OxideQSRStats.getPlugin().getMessagesFile(), movedToFile); } catch (IOException ignored) {}
            saveMessages();
            OxideQSRStats.error("A new messages.yml has been created and the erroneous one has been moved to " + movedToFile.getPath());
            reloadMessages();
        }
    }

}