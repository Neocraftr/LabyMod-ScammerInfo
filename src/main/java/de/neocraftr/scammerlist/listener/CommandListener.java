package de.neocraftr.scammerlist.listener;

import de.neocraftr.scammerlist.ScammerList;
import de.neocraftr.scammerlist.utils.ListType;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;

import java.util.*;

public class CommandListener implements ClientCommandEvent {

    private ScammerList sc = ScammerList.getScammerList();
    private boolean confirmClear;

    @Override
    public boolean onCommand(String cmd, String[] args) {
        if(!cmd.equalsIgnoreCase("scammer") && !cmd.equalsIgnoreCase("sc")) return false;
        if(args.length == 0) {
            printHelp();
            return true;
        }

        // Add player
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 2) {
                new Thread(() -> {
                    String uuid = sc.getHelper().getUUIDFromName(args[1]);
                    if (uuid != null) {
                        String name = sc.getHelper().getNamesFromUUID(uuid).get(0);
                        if (!sc.getHelper().addPlayer(uuid, name, ListType.PRIVATE)) {
                            if(uuid.equals(name)) {
                                sc.displayMessage(ScammerList.PREFIX + "§aDer Spieler §e" + name + " (UUID nicht verfügbar) §awurde zu deiner Scammerliste hinzugefügt.");
                            } else {
                                sc.displayMessage(ScammerList.PREFIX + "§aDer Spieler §e" + name + " (" + uuid + ") §awurde zu deiner Scammerliste hinzugefügt.");
                            }
                            sc.saveConfig();
                        } else {
                            sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + name + " §cbefindet sich bereits auf deiner Scammerliste.");
                        }
                    } else {
                        sc.displayMessage(ScammerList.PREFIX + "§cEs gibt keinen Spieler mit diesem Namen.");
                    }
                }).start();
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " add <Name>");
            }
        } else

        // Add clan
        if(args[0].equalsIgnoreCase("addclan")) {
            if (args.length == 2) {
                if(!sc.isClanInProcess()) {
                    sc.displayMessage(ScammerList.PREFIX+"§aBitte warten...");
                    sc.setAddClan(true);
                    sc.setClanInProcess(true);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/clan info "+args[1]);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(sc.isAddClan()) {
                                sc.setAddClan(false);
                                sc.setClanInProcess(false);
                                sc.displayMessage(ScammerList.PREFIX+"§cDer Clan konnte nicht hinzugefügt werden!");
                            }
                        }
                    }, 3000);
                } else {
                    sc.displayMessage(ScammerList.PREFIX + "§cEs wird bereits ein Clan hinzugefügt oder entfernt. Bitte warten!");
                }
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " addclan <Name|ClanTag>");
            }
        } else

        // Remove player
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 2) {
                new Thread(() -> {
                    String uuid = sc.getHelper().getUUIDFromName(args[1]);
                    if (uuid != null) {
                        String name = sc.getHelper().getNamesFromUUID(uuid).get(0);
                        if (sc.getHelper().removePlayer(uuid, ListType.PRIVATE)) {
                            if(uuid.equals(name)) {
                                sc.displayMessage(ScammerList.PREFIX + "§aDer Spieler §e" + name + " (UUID nicht verfügbar) §awurde von deiner Scammerliste entfernt.");
                            } else {
                                sc.displayMessage(ScammerList.PREFIX + "§aDer Spieler §e" + name + " (" + uuid + ") §awurde von deiner Scammerliste entfernt.");
                            }
                            sc.saveConfig();
                        } else {
                            sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + name + " §cbefindet sich nicht auf deiner Scammerliste.");
                        }
                    } else {
                        sc.displayMessage(ScammerList.PREFIX + "§cEs gibt keinen Spieler mit diesem Namen.");
                    }
                }).start();
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " remove <Name>");
            }
        } else

        // Remove clan
        if(args[0].equalsIgnoreCase("removeclan")) {
            if (args.length == 2) {
                if(!sc.isClanInProcess()) {
                    sc.displayMessage(ScammerList.PREFIX+"§aBitte warten...");
                    sc.setRemoveClan(true);
                    sc.setClanInProcess(true);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/clan info "+args[1]);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(sc.isRemoveClan()) {
                                sc.setRemoveClan(false);
                                sc.setClanInProcess(false);
                                sc.displayMessage(ScammerList.PREFIX+"§cDer Clan konnte nicht entfernt werden!");
                            }
                        }
                    }, 3000);
                } else {
                    sc.displayMessage(ScammerList.PREFIX + "§cEs wird bereits ein Clan hinzugefügt oder entfernt. Bitte warten!");
                }
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " removeclan <Name|ClanTag>");
            }
        } else

        // Check player
        if (args[0].equalsIgnoreCase("check")) {
            if (args.length == 2) {
                new Thread(() -> {
                    String uuid = sc.getHelper().getUUIDFromName(args[1]);
                    if (uuid != null) {
                        ArrayList<String> nameHistory = sc.getHelper().getNamesFromUUID(uuid);
                        if (sc.getHelper().isOnList(uuid, ListType.PRIVATE)) {
                            if (nameHistory.size() == 1) {
                                sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + nameHistory.get(0) + " §cbefindet sich auf deiner Scammerliste.");
                            } else {
                                sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + nameHistory.get(0) + " [" + nameHistory.get(1) + "] §cbefindet sich auf deiner Scammerliste.");
                            }
                        } else if(sc.getSettings().isShowOnlineScammer() && sc.getHelper().isOnList(uuid, ListType.ONLINE)) {
                            if (nameHistory.size() == 1) {
                                sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + nameHistory.get(0) + " §cbefindet sich auf der online Scammerliste.");
                            } else {
                                sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e" + nameHistory.get(0) + " [" + nameHistory.get(1) + "] §cbefindet sich auf der online Scammerliste.");
                            }
                        } else {
                            sc.displayMessage(ScammerList.PREFIX + "§aDer Spieler §e" + nameHistory.get(0) + " §abefindet sich nicht auf der Scammerliste.");
                        }
                    } else {
                        sc.displayMessage(ScammerList.PREFIX + "§cEs gibt keinen Spieler mit diesem Namen.");
                    }
                }).start();
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " check <Name>");
            }
        } else

        // List scammers
        if (args[0].equalsIgnoreCase("list")) {
            List<String> nameList = new ArrayList<>(sc.getHelper().getList(ListType.PRIVATE).values());
            if (!nameList.isEmpty()) {
                try {
                    int page = 0;
                    if(args.length >= 2) page = Integer.parseInt(args[1]) - 1;
                    if(page < 0) throw new NumberFormatException();

                    int numPages = (int) Math.ceil(nameList.size() / (double)ScammerList.PLAYERS_PER_LIST_PAGE);

                    if(page < numPages) {
                        int from = page * ScammerList.PLAYERS_PER_LIST_PAGE;
                        int to = page * ScammerList.PLAYERS_PER_LIST_PAGE + ScammerList.PLAYERS_PER_LIST_PAGE;
                        if(to > nameList.size() - 1)
                            to = (nameList.size() - 1 % ScammerList.PLAYERS_PER_LIST_PAGE) + 1;

                        ChatComponentText text = new ChatComponentText("");
                        text.appendText("\n§7-------------------- §eScammerliste §7--------------------");
                        for(int i=from; i<to; i++) {
                            text.appendText("\n§8- §c"+nameList.get(i));
                        }
                        if(page >= numPages - 1) {
                            text.appendText("\n§4Einträge insgesamt: §c"+nameList.size());
                        }
                        if(page > 0) {
                            ChatComponentText previousPage = new ChatComponentText("\n§a§l§n<<<");
                            previousPage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ScammerList.COMMAND_PREFIX+cmd+" list "+page));
                            text.appendSibling(previousPage);
                        } else {
                            text.appendText("\n§7§l<<<");
                        }
                        text.appendText(" §8§l[§e§l"+(page + 1)+"§8§l/§e§l"+numPages+"§8§l] ");
                        if(page < numPages - 1) {
                            ChatComponentText nextPage = new ChatComponentText("§a§l§n>>>");
                            nextPage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,ScammerList.COMMAND_PREFIX+cmd+" list "+(page + 2)));
                            text.appendSibling(nextPage);
                        } else {
                            text.appendText("§7§l>>>");
                        }

                        text.appendText("\n§7-----------------------------------------------------");
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(text);
                    } else {
                        sc.displayMessage(ScammerList.PREFIX + "§cSeite §e"+(page + 1)+" §cexistiert nicht.");
                    }
                } catch(NumberFormatException e) {
                    sc.displayMessage(ScammerList.PREFIX + "§cVerwendung: " + ScammerList.COMMAND_PREFIX + cmd + " list [Seite]");
                }
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cDeine Scammerliste ist leer.");
            }
        } else

        // List name changes
        if (args[0].equalsIgnoreCase("namechanges")) {
            if (!sc.getNameChangedPlayers().isEmpty()) {
                StringJoiner joiner = new StringJoiner("\n");
                joiner.add(ScammerList.PREFIX+"§aLetzte Namensänderungen:");
                sc.getNameChangedPlayers().forEach(name -> {
                    joiner.add("§8- §e"+name);
                });
                sc.displayMessage(joiner.toString());
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cBei der letzten Aktualisierung wurden keine Namensänderungen festgestellt.");
            }
        } else

        // Update lists
        if (args[0].equalsIgnoreCase("update")) {
            if (!sc.getHelper().getList(ListType.PRIVATE).isEmpty() || sc.getSettings().isShowOnlineScammer()) {
                if(!sc.isUpdatingList()) {
                    sc.displayMessage(ScammerList.PREFIX + "§aDie Namen der Scammerlisten werden aktualisiert. Dies kann einige Minuten dauern...");
                    new Thread(() -> {
                        sc.getHelper().updateLists();
                        sc.setNextUpdate(System.currentTimeMillis()+ScammerList.UPDATE_INTERVAL);
                        sc.saveConfig();
                        sc.displayMessage(ScammerList.PREFIX + "§aAktualisierung abgeschlossen.");
                    }).start();
                } else {
                    sc.displayMessage(ScammerList.PREFIX + "§cDie Scammerliste wird bereits aktualisiert. Bite warten!");
                }
            } else {
                sc.displayMessage(ScammerList.PREFIX + "§cDeine Scammerliste ist leer.");
            }
        } else

        // Clear scammer list
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
                if (isConfirmClear()) {
                    setConfirmClear(false);
                    sc.getHelper().clearList(ListType.PRIVATE);
                    sc.saveConfig();
                    sc.displayMessage(ScammerList.PREFIX + "§aAlle Einträge deiner Scammerliste wurden gelöscht.");
                } else {
                    sc.displayMessage(ScammerList.PREFIX + "§cBitte gib zuerst §e" + ScammerList.COMMAND_PREFIX + cmd + " clear §cein.");
                }
            } else {
                if (!isConfirmClear()) {
                    setConfirmClear(true);
                    sc.displayMessage(ScammerList.PREFIX + "§c§lAchtung: §cUm alle Einträge deiner Scammerliste zu löschen bestätige dies in den nächsten 15 Sekunden mit §e"
                            + ScammerList.COMMAND_PREFIX + cmd + " clear confirm§c.");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            setConfirmClear(false);
                        }
                    }, 15000);
                } else {
                    sc.displayMessage(ScammerList.PREFIX + "§cBitte bestätige das Löschen aller Einträge mit §e"
                            + ScammerList.COMMAND_PREFIX + cmd + " clear confirm§c.");
                }
            }
        } else printHelp();

        return true;
    }

    private void printHelp() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(ScammerList.PREFIX + "§aVerfügbare Befehle:");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer add <Name> §8- §aFügt einen Spieler zur Scammerliste hinzu.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer remove <Name> §8- §aEntfernt einen Spieler von der Scammerliste.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer addclan <Name|ClanTag §8- §aFügt die Spieler eines Clans zur Scammerliste hinzu.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer removeclan <Name|ClanTag> §8- §aEntfernt die Spieler eines Clans von der Scammerliste.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer check <Name> §8- §aÜberprüft ob sich ein Spieler auf der Scammerliste befindet.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer clear §8- §aEntfernt alle Spieler von der Scammerliste.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer list §8- §aZeigt alle Spieler auf der Scammerliste.");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer update §8- §aAktualisiert die Namen der Spieler. (Wird automatisch durchgeführt.)");
        joiner.add("§e"+ScammerList.COMMAND_PREFIX+"scammer namechanges §8- §aZeigt die Namensänderungen der letzten Aktualisierung an.)");
        sc.displayMessage(joiner.toString());
    }

        public boolean isConfirmClear() {
            return confirmClear;
        }
        public void setConfirmClear(boolean confirmClear) {
            this.confirmClear = confirmClear;
        }
}
