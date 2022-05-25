package de.neocraftr.scammerlist.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class Scammer {

    private String uuid;
    private String name;
    private String description;
    private String originalName;
    private final long date;

    public Scammer(String uuid, String name, String originalName, String description, long date) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.originalName = originalName;
        this.date = date;
    }

    public Scammer(String uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.originalName = name;
        this.description = description;
        this.date = System.currentTimeMillis();
    }

    public Scammer(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.originalName = name;
        this.date = System.currentTimeMillis();
    }

    public Scammer(String uuid) {
        this.uuid = uuid;
        this.date = System.currentTimeMillis();
    }

    public String getUUID() {
        return uuid;
    }
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalName() {
        return originalName;
    }
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public long getDate() {
        return date;
    }

    public static class ScammerDeserializer implements JsonDeserializer<Scammer> {

        @Override
        public Scammer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = json.getAsJsonObject();

            long date = 0;
            if(object.has("date")) {
                final JsonElement dateValue = object.get("date");
                // Convert ISO Timestamp to Unix time
                if(dateValue.getAsJsonPrimitive().isNumber()) {
                    date = dateValue.getAsLong();
                } else if(dateValue.getAsJsonPrimitive().isString()) {
                    try {
                        String isoTimestamp = dateValue.getAsString();
                        date = ZonedDateTime.parse(isoTimestamp).toEpochSecond()*1000;
                    } catch(DateTimeParseException e) {}
                }
            }

            String description = null;
            // Convert notice attribute to description
            if(object.has("description") && !object.get("description").isJsonNull()) {
                description = object.get("description").getAsString();
            } else if(object.has("notice") && !object.get("notice").isJsonNull()) {
                description = object.get("notice").getAsString();
                if(description.equalsIgnoreCase("No notice provided")) description = null;
            }


            String uuid = object.has("uuid") && !object.get("uuid").isJsonNull() ? object.get("uuid").getAsString() : null;
            String name = object.has("name") && !object.get("name").isJsonNull() ? object.get("name").getAsString() : null;
            String originalName = object.has("originalName") && !object.get("originalName").isJsonNull() ? object.get("originalName").getAsString() : null;

            return new Scammer(uuid, name, originalName, description, date);
        }
    }
}
