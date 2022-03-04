package ch.luca008.Utils;

import ch.luca008.UniPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Perms {

    public static String defaultNoPermMessage = "§cTu n'as pas la permission.";

    private static final String cadmin_base = "challenge.admin.";
    private static final String challenge_base = "challenge.use.";

    public enum Permission{
        CADMIN_COMMAND_USE("Permet d'utiliser la commande /cadmin", "challenge.admin.use", cadmin_base+"*"),

        CADMIN_TOGGLE_ALL("Activer/Desactiver les challenges (/cadmin enable|disable)", cadmin_base+"toggle.all", cadmin_base+"toggle.*", cadmin_base+"*"),
        CADMIN_TOGGLE_CATEGORY("Activer/Désactiver une catégorie grâce à /cadmin toggle cat <catégorie>", cadmin_base+"toggle.cat", cadmin_base+"toggle.*", cadmin_base+"*"),
        CADMIN_TOGGLE_CHALLENGE("Activer/Désactiver une challenge grâce à /cadmin toggle cha <challenge>", cadmin_base+"toggle.cha", cadmin_base+"toggle.*", cadmin_base+"*"),

        CADMIN_RELOAD_CONFIG("Recharger le fichier de configuration global (config.json)", cadmin_base+"reload.config", cadmin_base+"reload.*", cadmin_base+"*"),
        CADMIN_RELOAD_LANG("Recharger les fichiers de langues (Dossier /Lang/)", cadmin_base+"reload.lang", cadmin_base+"reload.*", cadmin_base+"*"),

        CADMIN_EDITOR_ISLAND("Permet de voir/modifier l'avancement des challenges d'une ile (/cadmin island <player>)", cadmin_base+"editor.island", cadmin_base+"editor.*", cadmin_base+"*"),
        CADMIN_EDITOR_APP("Permet de générer une clé afin d'accéder à une session d'édition sur l'app (Et aussi de supprimer une session en cours)", cadmin_base+"editor.app", cadmin_base+"editor.*", cadmin_base+"*"),
        CADMIN_EDITOR_PROMPTABLE("Autorise le serveur à capturer et traiter les données que le client envoie au serveur pour afficher et recevoir les lignes de l'éditeur (panneau)", cadmin_base+"promptable"),

        CHALLENGE_ADMIN_BYPASS("Permet d'accéder aux challenges même quand ils sont inactifs. (/cadmin disable)",cadmin_base+"bypass", cadmin_base+"*"),

        CHALLENGE_USE_COMMAND("Permet d'utiliser la commande /c", challenge_base+"command", challenge_base+"*"),
        CHALLENGE_USE_COMPLETE("Autorise un joueur à pouvoir compléter un challenge via la commande  /c c <challenge>", challenge_base+"complete", challenge_base+"*"),
        CHALLENGE_USE_CANCEL("Autorise un joueur à annuler un challenge de statistique en cours (Clic molette item + commande /c cancel <challenge>)", challenge_base+"cancel", challenge_base+"*"),
        ;

        private final String description;
        private final String permission;
        //A prioris les wildcards sont déjà pris en charge par Player#hasPermission. Il seront là pour guider l'admin a set les permissions
        //Mais ne sont pas utilisé dans Permission#hasPermission(CommandSender)
        private final String[] wildcards;

        Permission(String description, String permission, String...wildcards){
            this.description = description==null||description.isEmpty()?"unspecified":description;
            this.permission = permission;
            if(wildcards!=null&&wildcards.length>0){
                this.wildcards = wildcards;
            }else this.wildcards = new String[0];
        }

        public boolean hasPermission(CommandSender sender){
            if(sender.hasPermission(permission))return true;
            /*for(String wc : wildcards){
                if(sender.hasPermission(wc))return true;
            }*/
            return false;
        }

        public boolean hasPermission(UniPlayer player){
            Player p = player.getPlayer().orElse(null);
            if(p!=null){
                return hasPermission(p);
            }
            return false;
        }

        public String getDescription(){
            return description;
        }

        public String getInformation(){
            return " - " + getDescription() + "\n" + this.permission + " & " + Arrays.asList(wildcards);
        }
    }


    private CommandSender sender;

    public Perms(CommandSender sender){
        this.sender = sender;
    }

    public boolean hasPermission(Permission perm){
        return perm.hasPermission(sender);
    }

    public boolean isPlayer(){
        return sender instanceof Player;
    }

    /**
     * Cast sender to player
     * @return null if the sender isn't a player.
     */
    public Player getPlayer(){
        return isPlayer()?(Player)sender:null;
    }

}
