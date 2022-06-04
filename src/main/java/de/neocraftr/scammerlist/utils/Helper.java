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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Helper {

    private ScammerList sc = ScammerList.getScammerList();

    public List<String> getNameHistoryFromUUID(String uuid) {
        List<String> names = new ArrayList<>();

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
            System.out.println(ScammerList.CONSOLE_PREFIX + "Could not get name from mojang api: "+e);
        }
        Collections.reverse(names);
        return names;
    }

    public String getNameFromUUID(String uuid) {
        if(uuid.startsWith("!")) {
            return uuid;
        }

        try {
            uuid = CharMatcher.is('-').removeFrom(uuid);
            BufferedReader reader = Resources.asCharSource(new URL(String.format("https://api.minetools.eu/uuid/%s", uuid)), StandardCharsets.UTF_8).openBufferedStream();
            JsonObject json = sc.getGson().fromJson(reader, JsonObject.class);

            if(json == null) throw new Exception("No response for name "+uuid);
            if(!json.has("name") || !json.has("status") || !json.get("status").getAsString().equals("OK"))
                throw new Exception("Invalid response: "+json);

            return json.get("name").getAsString();
        } catch(Exception e) {
            System.out.println(ScammerList.CONSOLE_PREFIX + "Could not get name from minetools api: "+e);
        }
        return null;
    }

    public String getUUIDFromName(String name) {
        if(name.startsWith("!")) {
            return name;
        }

        try {
            BufferedReader reader = Resources.asCharSource(new URL(String.format("https://api.minetools.eu/uuid/%s", name)), StandardCharsets.UTF_8).openBufferedStream();
            JsonObject json = sc.getGson().fromJson(reader, JsonObject.class);

            if(json == null) throw new Exception("No response for name "+name);
            if(!json.has("id") || !json.has("status") || !json.get("status").getAsString().equals("OK"))
                throw new Exception("Invalid response: "+json);

            String uuid = json.get("id").getAsString();
            return Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})").matcher(uuid).replaceAll("$1-$2-$3-$4-$5");
        } catch(Exception e) {
            System.out.println(ScammerList.CONSOLE_PREFIX + "Could not get uuid from minetools api: "+e);
        }
        return null;
    }

    public String colorize(String msg) {
        return msg.replace("&", "§");
    }

    public String replaceUrlWildcards(String msg) {
        switch(msg.toLowerCase()) {
            case "%scammer-radar-sc%":
                return "http://newh1ve.de:6060/scammer/scammers";
            case "%scammer-radar-mm%":
                return "http://newh1ve.de:6060/mm/middlemans";
            default:
                return msg;
        }
    }
}
