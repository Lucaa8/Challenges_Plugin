package ch.luca008.Admin.Editor.Sockets;

import ch.luca008.Admin.Editor.Sockets.Packets.*;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Events implements Listener {

    private SocketManager.ClientThread client;

    public Events(SocketManager.ClientThread client){
        this.client = client;
    }

    public void PacketReceiveEvent(IPacket packet){
        int id = packet.getID();
        if(packet instanceof KeepAlivePacket){
            client.keepAlive = System.currentTimeMillis();
        }
        if(packet instanceof LoginPacket){
            LoginPacket p = (LoginPacket) packet;
            String reason = null;
            if(!LoginPacket.isVersionValid(p.getVersion())){
                String[] ver = Challenges.clientVersion.split("\\.");
                reason = "La version requise pour accéder aux challenges est " + (ver[0]+"."+ver[1]+".x") + ". La version actuelle du client est " + p.getVersion()+".";
            }else{
                if(!p.getVersion().equals(Challenges.clientVersion)){
                    System.out.println("Challenges editor [LoginPacket] " +client.session.getOwner().getName()+" tries to access the server(v"+Challenges.clientVersion+") with a different client version. (v"+p.getVersion()+")");
                }
                if(!client.session.getKey().equals(p.getKey())){
                    reason = "La clé d'accès est invalide.";
                }
            }
            client.valid = reason==null;
            client.send(new LoginPacket(id, client.valid, reason));
        }
        if(packet instanceof ExitPacket){
            client.session.sendMessage("§cLa session avec l'éditeur va être fermée. §7Raison: §8"+((ExitPacket)packet).getExitReason());
            Challenges.getEditor().stopSession(null);
        }
        if(packet instanceof ChallengeStatePacket){
            ChallengeStatePacket p = (ChallengeStatePacket)packet;
            if(p.getType()==ChallengeStatePacket.Type.UPDATE){
                Challenges.Main.setChallengesEnabled(p.getState(), false);
            }
            client.send(new ChallengeStatePacket(id, Challenges.Main.areChallengesEnabled()));
        }
        if(packet instanceof ChallengesListPacket){
            Map<String,String> ch = new HashMap<>();
            Map<String,String> ca = new HashMap<>();
            for(Category c : Challenges.getManager().getCategories()){
                ca.put(c.getUuid().toString(), c.getName());
            }
            for(Challenge c : Challenges.getManager().getChallenges()){
                ch.put(c.getUuid().toString(), c.getName());
            }
            client.send(new ChallengesListPacket(id,ca,ch));
        }
        if(packet instanceof InfoRequestPacket){
            InfoRequestPacket p = (InfoRequestPacket)packet;
            JSONObject json = null;
            if(p.getType() == IPacket.Target.CATEGORY){
                for(Category c : Challenges.getManager().getCategories()){
                    if(c.getUuid().toString().equals(p.getUuid())){
                        json = c.toJson();
                        JSONArray jarr = new JSONArray();
                        for(Challenge cha : Challenges.getManager().getIndex().getRequiredChallenges(c)){
                            jarr.add(cha.getUuid().toString());
                        }
                        json.put("requiredChallenges", jarr);
                        break;
                    }
                }
            }
            else if(p.getType() == IPacket.Target.CHALLENGE){
                for(Challenge c : Challenges.getManager().getChallenges()){
                    if(c.getUuid().toString().equals(p.getUuid())){
                        json = c.toJson();
                        JSONArray jarr = new JSONArray();
                        for(Challenge cha : Challenges.getManager().getIndex().getRequiredChallenges(c)){
                            jarr.add(cha.getUuid().toString());
                        }
                        json.put("requiredChallenges", jarr);
                        break;
                    }
                }
            }
            client.send(new InfoRequestPacket(id,json!=null,json));
        }
        if(packet instanceof UpdateRequestPacket){
            UpdateRequestPacket p = (UpdateRequestPacket)packet;
            if(p.getType() == IPacket.Target.CATEGORY){
                Challenges.getManager().addOrReplaceCategory(p.getJson());
            }
            else if(p.getType() == IPacket.Target.CHALLENGE){
                Challenges.getManager().addOrReplaceChallenge(p.getJson());
            }
        }
        if(packet instanceof DeleteRequestPacket){
            DeleteRequestPacket p = (DeleteRequestPacket)packet;
            UUID u = UUID.fromString(p.getUuid());
            boolean success = false;
            String reason = "";
            if(p.getType()==IPacket.Target.CATEGORY){
                Optional<Category> optCat = Challenges.getManager().retrieveCategoryByUUID(u);
                if(optCat.isPresent()){
                    success = Challenges.getManager().removeCategory(optCat.get());
                    if(!success) reason = "La catégorie n'est pas vide.";
                }else{
                    reason = "La catégorie est introuvable.";
                }
            }else{
                Optional<Challenge> optCha = Challenges.getManager().retrieveChallengeByUUID(u);
                if(optCha.isPresent()){
                    Challenges.getManager().removeChallenge(optCha.get());
                    success = true;
                }else{
                    reason = "Le challenge est introuvable.";
                }
            }
            client.send(new DeleteRequestPacket(id,success,reason));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if(e.getPlayer()==client.session.getOwner()){
            Challenges.getEditor().stopSession("Le joueur qui a commencé cette session s'est déconnecté.");
        }
    }

}
