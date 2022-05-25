package de.neocraftr.scammerlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.neocraftr.scammerlist.listener.*;
import de.neocraftr.scammerlist.settings.SettingsManager;
import de.neocraftr.scammerlist.utils.*;
import net.labymod.addon.AddonLoader;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScammerList extends LabyModAddon {

    public static final String PREFIX = "§8[§4ScammerInfo§8] §r",
                               PREFIX_LINE = "§f-------------------- §4ScammerInfo §f--------------------",
                               CONSOLE_PREFIX = "[ScammerInfo] ",
                               COMMAND_PREFIX = ".",
                               VERSION = "1.9.3";
    public static final int PLAYERS_PER_LIST_PAGE = 15;

    private static ScammerList scammerList;
    private Gson gson;
    private SettingsManager settings;
    private Helper helper;
    private Updater updater;
    private ListManager listManager;
    private UpdateQueue updateQueue;
    private long lastUpdateTime = 0;
    private boolean addClan, removeClan, clanInProcess;
    private PlayerType clanPlayerType;
    private Set<ClientCommandEvent> commandListeners = new HashSet<>();

    @Override
    public void onEnable() {
        scammerList = this;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Scammer.class, new Scammer.ScammerDeserializer())
                .create();
        settings = new SettingsManager();
        helper = new Helper();
        updater = new Updater();
        listManager = new ListManager();
        updateQueue = new UpdateQueue();

        getApi().getEventManager().register(new ChatSendListener());
        getApi().getEventManager().register(new ChatReceiveListener());
        getApi().getEventManager().register(new ModifyChatListener());
        getApi().registerForgeListener(new PreRenderListener());
        getApi().registerForgeListener(new TickListener());
        registerEvent(new ScammerCommandListener());
        registerEvent(new TrustedCommandListener());
    }

    @Override
    public void loadConfig() {
        updater.setAddonJar(AddonLoader.getFiles().get(about.uuid));

        settings.loadSettings();
        listManager.loadLists();

        if(getConfig().has("lastUpdateTime")) {
            lastUpdateTime = getConfig().get("lastUpdateTime").getAsLong();
        }

        if(settings.isAutoUpdate()) {
            if(lastUpdateTime + settings.getUpdateInterval()*86400000L < System.currentTimeMillis()) {
                listManager.updateLists(() -> {
                    lastUpdateTime = System.currentTimeMillis();
                    getConfig().addProperty("lastUpdateTime", lastUpdateTime);
                    saveConfig();
                    System.out.println(CONSOLE_PREFIX + "Updated player names.");
                });
            }
        }
    }

    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        this.settings.fillSettings(settings);
    }

    public void displayMessage(String msg) {
        getApi().displayMessageInChat(msg);
    }

    public void registerEvent(ClientCommandEvent listener) {
        getCommandListeners().add(listener);
    }

    public static ScammerList getScammerList() {
        return scammerList;
    }

    public Gson getGson() {
        return gson;
    }

    public SettingsManager getSettings() {
        return settings;
    }

    public Helper getHelper() {
        return helper;
    }

    public Updater getUpdater() {
        return updater;
    }

    public ListManager getListManager() {
        return listManager;
    }

    public UpdateQueue getUpdateQueue() {
        return updateQueue;
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

    public PlayerType getClanPlayerType() {
        return clanPlayerType;
    }
    public void setClanPlayerType(PlayerType clanPlayerType) {
        this.clanPlayerType = clanPlayerType;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Set<ClientCommandEvent> getCommandListeners() {
        return commandListeners;
    }
}
