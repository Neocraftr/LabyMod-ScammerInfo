package de.neocraftr.scammerlist;

import com.google.common.base.CharMatcher;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class Helper {

    private ScammerList sc = ScammerList.getScammerList();

    public ArrayList<String> getNamesFromUUID(String uuid) {
        ArrayList<String> names = new ArrayList<>();

        if(uuid.startsWith("!")) {
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
        if(name.startsWith("!")) {
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

    public void downloadOnlineScammerList() {
        try {
            try(BufferedReader reader = Resources.asCharSource(new URL(ScammerList.ONLINE_SCAMMER_URL), StandardCharsets.UTF_8).openBufferedStream()) {

                JsonReader json = new JsonReader(reader);
                json.beginArray();

                sc.getOnlineScammerListUUID().clear();

                while (json.hasNext()) {
                    json.beginObject();
                    while (json.hasNext()) {
                        if(json.nextName().equals("uuid")) {
                            String uuid = json.nextString();
                            sc.getOnlineScammerListUUID().add(uuid);
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
        sc.getNameChangedPlayers().clear();

        // Update online list
        downloadOnlineScammerList();
        ArrayList<String> newOnlineScammerListName = new ArrayList<>();
        sc.getOnlineScammerListUUID().forEach(uuid -> {
            ArrayList<String> names = getNamesFromUUID(uuid);
            if(!sc.getOnlineScammerListName().contains(names.get(0))) {
                if(names.size() == 1) {
                    sc.getNameChangedPlayers().add(names.get(0));
                } else {
                    sc.getNameChangedPlayers().add(names.get(0)+" ["+names.get(1)+"]");
                }
            }
            newOnlineScammerListName.add(names.get(0));
        });
        sc.setOnlineScammerListName(newOnlineScammerListName);

        // Update private list
        ArrayList<String> newScammerListName = new ArrayList<>();
        sc.getScammerListUUID().forEach(uuid -> {
            ArrayList<String> names = getNamesFromUUID(uuid);
            if(!sc.getScammerListName().contains(names.get(0))) {
                if(names.size() == 1) {
                    sc.getNameChangedPlayers().add(names.get(0));
                } else {
                    sc.getNameChangedPlayers().add(names.get(0)+" ["+names.get(1)+"]");
                }
            }
            newScammerListName.add(names.get(0));
        });
        sc.setScammerListName(newScammerListName);

        sc.saveConfig();
    }
}