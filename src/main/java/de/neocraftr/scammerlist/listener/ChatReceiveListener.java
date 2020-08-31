package de.neocraftr.scammerlist.listener;

import de.neocraftr.scammerlist.ScammerList;
import de.neocraftr.scammerlist.utils.Database;
import de.neocraftr.scammerlist.utils.ListType;
import net.labymod.api.events.MessageReceiveEvent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatReceiveListener implements MessageReceiveEvent {

    private ScammerList sc = ScammerList.getScammerList();
    private Pattern clanMemberRegex = Pattern.compile("^>> (\\!?\\w{1,16}) \\((Online|Offline)\\)");
    private ArrayList<String> clanMemberList = new ArrayList<>();
    private String clanName;
    private boolean clanMessage;
    private int newPlayers;

    @Override
    public boolean onReceive(String msgRaw, String msg) {
        if(sc.isAddClan() || sc.isRemoveClan()) {
            if(msg.equals("----------- Clan-Mitglieder -----------")) {
                if(isClanMessage()) {
                    boolean addClan = sc.isAddClan(), removeClan = sc.isRemoveClan();
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getClanMemberList().forEach(name -> {
                            if(addClan) {
                                String uuid = sc.getHelper().getUUIDFromName(name);
                                if (uuid != null) {
                                    if (!sc.getHelper().addPlayer(uuid, name, ListType.PRIVATE)) {
                                        addNewPlayer();
                                    }
                                } else {
                                    sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e"+name+" §cwurde nicht gefunden.");
                                }
                            }
                            if(removeClan) {
                                String uuid = sc.getHelper().getUUIDFromName(name);
                                if (uuid != null) {
                                    if (sc.getHelper().removePlayer(uuid, ListType.PRIVATE)) {
                                        addNewPlayer();
                                    }
                                } else {
                                    sc.displayMessage(ScammerList.PREFIX + "§cDer Spieler §e"+name+" §cwurde nicht gefunden.");
                                }
                            }
                        });

                        sc.saveConfig();
                        if(addClan) sc.displayMessage(ScammerList.PREFIX + "§aEs wurden §e"+getNewPlayers()+" §aSpieler des Clans §e"+getClanName()+" §azur Scammerliste hinzugefügt.");
                        if(removeClan) sc.displayMessage(ScammerList.PREFIX + "§aEs wurden §e"+getNewPlayers()+" §aSpieler des Clans §e"+getClanName()+" §avon der Scammerliste entfernt.");

                        setClanMessage(false);
                        getClanMemberList().clear();
                        setClanName("");
                        setNewPlayers(0);
                        sc.setClanInProcess(false);
                    }).start();

                    sc.setAddClan(false);
                    sc.setRemoveClan(false);
                } else {
                    setClanMessage(true);
                }
            }

            if(msg.startsWith("[Clans]")) {
                if(sc.isAddClan()) sc.displayMessage(ScammerList.PREFIX+"§cDer Clan konnte nicht hinzugefügt werden!");
                if(sc.isRemoveClan()) sc.displayMessage(ScammerList.PREFIX+"§cDer Clan konnte nicht entfernt werden!");
                sc.setAddClan(false);
                sc.setRemoveClan(false);
                sc.setClanInProcess(false);
            }

            if(isClanMessage()) {
                if(msg.startsWith("Clan-Name:")) {
                    setClanName(msg.split(":")[1].trim());
                }

                Matcher m = clanMemberRegex.matcher(msg);
                if(m.matches()) {
                    getClanMemberList().add(m.group(1));
                }
            }
        }
        return false;
    }

    public ArrayList<String> getClanMemberList() {
        return clanMemberList;
    }
    public void setClanMemberList(ArrayList<String> clanMemberList) {
        this.clanMemberList = clanMemberList;
    }

    public String getClanName() {
        return clanName;
    }
    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public boolean isClanMessage() {
        return clanMessage;
    }
    public void setClanMessage(boolean clanMessage) {
        this.clanMessage = clanMessage;
    }

    public int getNewPlayers() {
        return newPlayers;
    }
    public void setNewPlayers(int newPlayers) {
        this.newPlayers = newPlayers;
    }
    public void addNewPlayer() {
        this.newPlayers++;
    }
}
