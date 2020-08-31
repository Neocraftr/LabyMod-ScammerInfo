package de.neocraftr.scammerlist;

import com.google.gson.Gson;
import de.neocraftr.scammerlist.listener.*;
import de.neocraftr.scammerlist.utils.Database;
import de.neocraftr.scammerlist.utils.Helper;
import de.neocraftr.scammerlist.utils.SettingsManager;
import net.labymod.addon.AddonLoader;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;

import java.util.*;

public class ScammerList extends LabyModAddon {

    public static final String PREFIX = "§8[§4Scammerliste§8] §r",
                               COMMAND_PREFIX = ".",
                               ONLINE_SCAMMER_URL = "https://coolertyp.scammer-radar.de/onlineScammer.json";
    public static final int PLAYERS_PER_LIST_PAGE = 15,
                            UPDATE_INTERVAL = 604800000; // 1 week

    private static ScammerList scammerList;
    private Gson gson;
    private SettingsManager settings;
    private Helper helper;
    private Database database;
    private long nextUpdate = 0;
    private ArrayList<String> privateListName = new ArrayList<>();
    private ArrayList<String> privateListUUID = new ArrayList<>();
    private ArrayList<String> onlineListName = new ArrayList<>();
    private ArrayList<String> onlineListUUID = new ArrayList<>();
    private ArrayList<String> nameChangedPlayers = new ArrayList<>();
    private boolean addClan, removeClan, clanInProcess, updatingList;
    private Set<ClientCommandEvent> commandListeners = new HashSet<>();

    @Override
    public void onEnable() {
        setScammerList(this);
        setGson(new Gson());
        setSettings(new SettingsManager());
        setHelper(new Helper());
        setDatabase(new Database(AddonLoader.getConfigDirectory()+"/ScammerList.db"));
        getDatabase().connect();

        getApi().getEventManager().register(new ChatSendListener());
        getApi().getEventManager().register(new ChatReceiveListener());
        getApi().getEventManager().register(new ModifyChatListener());
        getApi().registerForgeListener(new PreRenderListener());
        registerEvent(new CommandListener());
    }

    @Override
    public void onDisable() {
        getDatabase().close();
    }

    @Override
    public void loadConfig() {
        getSettings().loadSettings();

        if(getConfig().has("scammerListName")) {
            setPrivateListName(getGson().fromJson(getConfig().get("scammerListName"), ArrayList.class));
        }
        if(getConfig().has("scammerListUUID")) {
            setPrivateListUUID(getGson().fromJson(getConfig().get("scammerListUUID"), ArrayList.class));
        }
        if(getConfig().has("onlineScammerListName")) {
            setOnlineListName(getGson().fromJson(getConfig().get("onlineScammerListName"), ArrayList.class));
        }
        if(getConfig().has("onlineScammerListUUID")) {
            setOnlineListUUID(getGson().fromJson(getConfig().get("onlineScammerListUUID"), ArrayList.class));
        }
        if(getConfig().has("nameChangedPlayers")) {
            setNameChangedPlayers(getGson().fromJson(getConfig().get("nameChangedPlayers"), ArrayList.class));
        }
        if(getConfig().has("nextUpdate")) {
            setNextUpdate(getConfig().get("nextUpdate").getAsLong());
        }

        if(getSettings().isAutoUpdate()) {
            if(getNextUpdate() < System.currentTimeMillis()) {
                setNextUpdate(System.currentTimeMillis()+UPDATE_INTERVAL);

                new Thread(() -> {
                    getHelper().updateLists();
                    System.out.println("[ScammerList] Updated player names.");
                }).start();
            }
        }
    }

    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        getSettings().fillSettings(settings);
    }

    public void saveConfig() {
        getConfig().add("scammerListName", getGson().toJsonTree(getPrivateListName()));
        getConfig().add("scammerListUUID", getGson().toJsonTree(getPrivateListUUID()));
        getConfig().add("onlineScammerListName", getGson().toJsonTree(getOnlineListName()));
        getConfig().add("onlineScammerListUUID", getGson().toJsonTree(getOnlineListUUID()));
        getConfig().add("nameChangedPlayers", getGson().toJsonTree(getNameChangedPlayers()));
        getConfig().addProperty("nextUpdate", getNextUpdate());
        getConfig().addProperty("showOnlineScammer", getSettings().isShowOnlineScammer());
        getConfig().addProperty("highlightInChat", getSettings().isHighlightInChat());
        getConfig().addProperty("highlightInTablist", getSettings().isHighlightInTablist());
        getConfig().addProperty("highlightInStartkick", getSettings().isHighlightInStartkick());
        getConfig().addProperty("autoUpdate", getSettings().isAutoUpdate());
        getConfig().addProperty("scammerPrefix", getSettings().getScammerPrefix());
        super.saveConfig();
    }

    public void displayMessage(String msg) {
        getApi().displayMessageInChat(msg);
    }

    public void registerEvent(ClientCommandEvent listener) {
        getCommandListeners().add(listener);
    }

    public static void setScammerList(ScammerList scammerList) {
        ScammerList.scammerList = scammerList;
    }
    public static ScammerList getScammerList() {
        return scammerList;
    }

    public Gson getGson() {
        return gson;
    }
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setSettings(SettingsManager settings) {
        this.settings = settings;
    }
    public SettingsManager getSettings() {
        return settings;
    }

    public void setHelper(Helper helper) {
        this.helper = helper;
    }
    public Helper getHelper() {
        return helper;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
    public Database getDatabase() {
        return database;
    }

    /*public ArrayList<String> getPrivateListName() {
        return privateListName;
    }
    public void setPrivateListName(ArrayList<String> privateListName) {
        this.privateListName = privateListName;
    }

    public ArrayList<String> getPrivateListUUID() {
        return privateListUUID;
    }
    public void setPrivateListUUID(ArrayList<String> privateListUUID) {
        this.privateListUUID = privateListUUID;
    }

    public ArrayList<String> getOnlineListName() {
        return onlineListName;
    }
    public void setOnlineListName(ArrayList<String> onlineListName) {
        this.onlineListName = onlineListName;
    }

    public ArrayList<String> getOnlineListUUID() {
        return onlineListUUID;
    }
    public void setOnlineListUUID(ArrayList<String> onlineListUUID) {
        this.onlineListUUID = onlineListUUID;
    }*/

    public ArrayList<String> getNameChangedPlayers() {
        return nameChangedPlayers;
    }
    public void setNameChangedPlayers(ArrayList<String> nameChangedPlayers) {
        this.nameChangedPlayers = nameChangedPlayers;
    }

    public boolean isAddClan() {
        return addClan;
    }
    public void setAddClan(boolean addClan) {
        this.addClan = addClan;
    }

    public boolean isRemoveClan() {
        return removeClan;
    }
    public void setRemoveClan(boolean removeClan) {
        this.removeClan = removeClan;
    }

    public boolean isClanInProcess() {
        return clanInProcess;
    }
    public void setClanInProcess(boolean clanInProcess) {
        this.clanInProcess = clanInProcess;
    }

    public boolean isUpdatingList() {
        return updatingList;
    }
    public void setUpdatingList(boolean updatingList) {
        this.updatingList = updatingList;
    }

    public long getNextUpdate() {
        return nextUpdate;
    }
    public void setNextUpdate(long nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public void setCommandListeners(Set<ClientCommandEvent> commandListeners) {
        this.commandListeners = commandListeners;
    }
    public Set<ClientCommandEvent> getCommandListeners() {
        return commandListeners;
    }
}

