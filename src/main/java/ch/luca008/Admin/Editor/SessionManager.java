package ch.luca008.Admin.Editor;

import ch.luca008.Admin.Editor.Sockets.Packets.ExitPacket;
import ch.luca008.Admin.Editor.Sockets.SocketManager;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class SessionManager {

    private SocketManager manager;
    private Session current;

    public SessionManager(){
        manager = new SocketManager();
    }

    public boolean createSession(Player p){
        if(current==null){
            current = new Session(p);
            return true;
        }
        return false;
    }

    public Session getCurrent(){
        return current;
    }

    /**
     * Permet de stopper (ou annuler si aucune connexion n'est démarrée après 1min) une session distante avec l'éditeur si existante. (Ne throw pas d'erreur)
     * <p>
     * La raison en paramètre devrait être nulle si le client a envoyé au serveur un packet Exit.
     * <p>
     * Si le serveur veut terminer la connexion alors le paramètre contiendra une raison personalisée qui sera affichée sur le client
     * @param reason Une raison qui sera affichée sur le client lorsque le serveur veut terminer la session
     */
    public void stopSession(@Nullable String reason){
        manager.active = false; //permet d'annuler un listening de client en cours
        if(current!=null){
            SocketManager.ClientThread client = current.getClient();
            if(client!=null){
                if(reason!=null){
                    client.send(new ExitPacket(reason));
                }
                client.active = false;
            }
            current = null;
        }
    }

    public class Session{
        private Player owner;
        private String key;
        private SocketManager.ClientThread client;
        public long activeSince = -1;
        public Session(Player owner){
            this.owner = owner;
            key = RandomStringUtils.randomAlphanumeric(10);
            manager.newSession();
        }
        public Player getOwner(){
            return owner;
        }
        public String getKey(){
            return key;
        }
        public void setClient(SocketManager.ClientThread client){
            this.client = client;
        }
        public SocketManager.ClientThread getClient(){
            return this.client;
        }
        public void sendMessage(String msg){
            if(owner!=null){
                owner.sendMessage(msg);
            }
        }
    }

}
