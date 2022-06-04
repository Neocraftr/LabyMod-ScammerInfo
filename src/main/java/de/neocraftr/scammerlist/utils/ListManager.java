package de.neocraftr.scammerlist.utils;

import com.google.gson.*;
import de.neocraftr.scammerlist.ScammerList;
import net.labymod.addon.AddonLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListManager {
    private List<PlayerList> lists;
    private PlayerList privateListScammer, privateListTrusted;
    private File listDir;

    private ScammerList sc = ScammerList.getScammerList();

    public void loadLists() {
        lists = new ArrayList<>();

        listDir = new File(AddonLoader.getConfigDirectory(), "ScammerList");
        if(!listDir.exists()) {
            listDir.mkdirs();
        }

        // Local lists
        privateListScammer = new PlayerList("private", true, true, "Private Scammer Liste", null, PlayerType.SCAMMER);
        privateListScammer.load();

        privateListTrusted = new PlayerList("private-trusted", true, true, "Private Trusted Liste", null, PlayerType.TRUSTED);
        privateListTrusted.load();

        // Load configured lists
        boolean hasScammerRadarSC = false, hasScammerRadarMM = false;
        if(sc.getConfig().has("lists")) {
            for(JsonElement element : sc.getConfig().get("lists").getAsJsonArray()) {
                JsonObject list = element.getAsJsonObject();
                PlayerList playerList = new PlayerList(sc.getGson().fromJson(list, PlayerList.Meta.class));


                // Remove old list from previous versions
                if(playerList.getMeta().getName().equalsIgnoreCase("[SCAMMER] Radar") || playerList.getMeta().getUrl().equalsIgnoreCase("%scammer-radar%")) continue;

                if(playerList.getMeta().getId().equals("scammer-radar-sc")) hasScammerRadarSC = true;
                if(playerList.getMeta().getId().equals("scammer-radar-mm")) hasScammerRadarMM = true;


                playerList.load();
                lists.add(playerList);
            }
        }

        // Add default lists
        if(!hasScammerRadarSC) {
            PlayerList list = new PlayerList("scammer-radar-sc", true, true, "[SCAMMER] Radar SC", "%scammer-radar-sc%", PlayerType.SCAMMER);
            list.load();
            sc.getUpdateQueue().addList(list);
            lists.add(list);
        }
        if(!hasScammerRadarMM) {
            PlayerList list = new PlayerList("scammer-radar-mm", true, true, "[SCAMMER] Radar MM", "%scammer-radar-mm%", PlayerType.TRUSTED);
            list.load();
            sc.getUpdateQueue().addList(list);
            lists.add(list);
        }

        saveListSettings();
    }

    public void updateLists(Runnable callback) {
        if(callback != null) {
            sc.getUpdateQueue().registerFinishCallback(callback);
        }

        sc.getUpdateQueue().addList(privateListScammer);
        sc.getUpdateQueue().addList(privateListTrusted);
        for(PlayerList list : lists) {
            sc.getUpdateQueue().addList(list);
        }
    }

    public void cancelAllUpdates() {
        sc.getUpdateQueue().removeList(privateListScammer);
        sc.getUpdateQueue().removeList(privateListTrusted);
        for(PlayerList list : lists) {
            sc.getUpdateQueue().removeList(list);
        }
    }

    public boolean checkName(String name, PlayerType type) {
        if(type == PlayerType.SCAMMER && privateListScammer.containsName(name)) return true;
        if(type == PlayerType.TRUSTED && privateListTrusted.containsName(name)) return true;
        for(PlayerList list : lists) {
            if(!list.getMeta().isEnabled()) continue;
            if(list.getMeta().getType() != type) continue;
            if(list.containsName(name)) return true;
        }
        return false;
    }

    public boolean checkUUID(String uuid, PlayerType type) {
        if(type == PlayerType.SCAMMER && privateListScammer.containsUUID(uuid)) return true;
        if(type == PlayerType.TRUSTED && privateListTrusted.containsUUID(uuid)) return true;
        for(PlayerList list : lists) {
            if(!list.getMeta().isEnabled()) continue;
            if(list.getMeta().getType() != type) continue;
            if(list.containsUUID(uuid)) return true;
        }
        return false;
    }

    public List<PlayerList> getContainingLists(String uuid, PlayerType type) {
        List<PlayerList> containingLists = new ArrayList<>();
        if(type == PlayerType.SCAMMER && privateListScammer.containsUUID(uuid)) {
            containingLists.add(privateListScammer);
        } else if(type == PlayerType.TRUSTED && privateListTrusted.containsUUID(uuid)) {
            containingLists.add(privateListTrusted);
        }
        for(PlayerList list : lists) {
            if(!list.getMeta().isEnabled()) continue;
            if(list.getMeta().getType() != type) continue;
            if(list.containsUUID(uuid)) containingLists.add(list);
        }
        return containingLists;
    }

    public void saveListSettings() {
        JsonArray savedLists = new JsonArray();
        for(PlayerList list : lists) {
            savedLists.add(sc.getGson().toJsonTree(list.getMeta(), PlayerList.Meta.class));
        }
        sc.getConfig().add("lists", savedLists);
        sc.saveConfig();
    }

    public PlayerList createList(boolean enabled, String name, String url, PlayerType type) {
        PlayerList list = new PlayerList(UUID.randomUUID().toString(), false, enabled, name, url, type);
        lists.add(list);
        return list;
    }

    public void deleteList(PlayerList list) {
        list.deleteListFile();
        lists.remove(list);
    }

    public List<PlayerList> getLists() {
        return lists;
    }

    public PlayerList getPrivateListScammer() {
        return privateListScammer;
    }

    public PlayerList getPrivateListTrusted() {
        return privateListTrusted;
    }

    public File getListDir() {
        return listDir;
    }
}
