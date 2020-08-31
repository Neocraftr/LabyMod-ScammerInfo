package de.neocraftr.scammerlist.utils;

import com.google.common.base.CharMatcher;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.neocraftr.scammerlist.ScammerList;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Helper {

    private ScammerList sc = ScammerList.getScammerList();
    private Database db = ScammerList.getScammerList().getDatabase();

    public ArrayList<String> getNamesFromUUID(String uuid) {
        ArrayList<String> names = new ArrayList<>();

        if(uuid.startsWith("!") || uuid.equals("*")) {
            names.add(uuid);
            return names;
        }

        try {
            uuid = CharMatcher.is('-').removeFrom(uuid);
            try (BufferedReader reader = Resources.asCharSource(new URL(String.format("https://api.mojang.com/user/profiles/%s/names", uuid)), StandardCharsets.UTF_8).openBufferedStream()) {
                JsonReader json = new JsonReader(reader);
                json.beginArray();

                while (json.hasNext()) {
                    json.beginObject();
                    while (json.hasNext()) {
                        String key = json.nextName();
                        switch (key) {
                            case "name":
                                names.add(json.nextString());
                                break;
                            default:
                                json.skipValue();
                                break;
                        }
                    }
                    json.endObject();
                }

                json.endArray();
            }
        } catch(IOException e) {
            System.out.println("[ScammerList] Could not get name from mojang api: "+e.getMessage());
        }
        Collections.reverse(names);
        return names;
    }

    public String getUUIDFromName(String name) {
        if(name.startsWith("!") || name.equals("*")) {
            return name;
        }

        try {
            try (BufferedReader reader = Resources.asCharSource(new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", name)), StandardCharsets.UTF_8).openBufferedStream()) {
                JsonObject json = sc.getGson().fromJson(reader, JsonObject.class);
                if(json == null || !json.has("id")) return null;
                String uuid = json.get("id").getAsString();
                return Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})").matcher(uuid).replaceAll("$1-$2-$3-$4-$5");
            }
        } catch(IOException e) {
            System.out.println("[ScammerList] Could not get uuid from mojang api: "+e.getMessage());
        }
        return null;
    }

    private void downloadOnlineScammerList() {
        try {
            try(BufferedReader reader = Resources.asCharSource(new URL(ScammerList.ONLINE_SCAMMER_URL), StandardCharsets.UTF_8).openBufferedStream()) {

                JsonReader json = new JsonReader(reader);
                json.beginArray();

                clearList(ListType.ONLINE);

                while (json.hasNext()) {
                    json.beginObject();
                    while (json.hasNext()) {
                        if(json.nextName().equals("uuid")) {
                            String uuid = json.nextString();
                            addPlayer(uuid, "", ListType.ONLINE);
                        } else {
                            json.nextString();
                        }
                    }
                    json.endObject();
                }

                json.endArray();

                sc.saveConfig();
            }
        } catch (IOException e) {
            System.out.println("[ScammerList] Could not load online scammer list: "+e.getMessage());
        }
    }

    public void updateLists() {
        sc.setUpdatingList(true);
        sc.getNameChangedPlayers().clear();

        downloadOnlineScammerList();

        HashMap<String, String> scammerList = getList(ListType.ONLINE);
        scammerList.putAll(getList(ListType.PRIVATE));
        sc.getOnlineListUUID().forEach(uuid -> {
            ArrayList<String> names = getNamesFromUUID(uuid);
            if(!sc.getOnlineListName().contains(names.get(0))) {
                addNameChange(names);
            }
            newOnlineScammerListName.add(names.get(0));
        });

        sc.saveConfig();
        sc.setUpdatingList(false);
    }

    private void addNameChange(ArrayList<String> names) {
        if(names.size() == 1) {
            if(!sc.getNameChangedPlayers().contains(names.get(0))) {
                sc.getNameChangedPlayers().add(names.get(0));
            }
        } else {
            if(!sc.getNameChangedPlayers().contains(names.get(0)+" ["+names.get(1)+"]")) {
                sc.getNameChangedPlayers().add(names.get(0)+" ["+names.get(1)+"]");
            }
        }
    }

    public String colorize(String msg) {
        return msg.replace("&", "§");
    }

    public boolean checkUUID(String uuid, ListType type) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("SELECT EXISTS(SELECT uuid FROM scammer WHERE uuid = ? AND type = ?);");
            ps.setString(0, uuid);
            ps.setString(1, type.toString());
            ResultSet rs = ps.executeQuery();
            ps.close();
            rs.next();
            return rs.getInt(0) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkName(String name, ListType type) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("SELECT EXISTS(SELECT uuid FROM scammer WHERE name = ? AND type = ?);");
            ps.setString(0, name);
            ps.setString(1, type.toString());
            ResultSet rs = ps.executeQuery();
            ps.close();
            rs.next();
            return rs.getInt(0) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addPlayer(String uuid, String name, ListType type) {
        if(checkUUID(uuid, type)) return false;
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO scammer(uuid, name, type) VALUES (?, ?, ?);");
            ps.setString(0, uuid);
            ps.setString(1, name);
            ps.setString(2, type.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removePlayer(String uuid, ListType type) {
        if(!checkUUID(uuid, type)) return false;
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM scammer WHERE uuid = ? AND type = ?;");
            ps.setString(0, uuid);
            ps.setString(1, type.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateName(String uuid, String newName, ListType type) {
        if(!checkUUID(uuid, type)) return false;
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("UPDATE scammer SET name = ? WHERE uuid = ? AND type = ?;");
            ps.setString(0, newName);
            ps.setString(1, uuid);
            ps.setString(2, type.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearList(ListType type) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM scammer WHERE type = ?;");
            ps.setString(0, type.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getList(ListType type) {
        HashMap<String, String> scammerList = new HashMap<>();
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("SELECT uuid, name FROM scammer WHERE type = ?;");
            ps.setString(0, type.toString());
            ResultSet rs = ps.executeQuery();
            ps.close();
            while(rs.next()) {
                scammerList.put(rs.getString(0), rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scammerList;
    }

}
