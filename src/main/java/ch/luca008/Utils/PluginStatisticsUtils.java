package ch.luca008.Utils;

import ch.luca008.Admin.Editor.SessionManager;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;

import java.util.Set;

public class PluginStatisticsUtils {

    public static int getLoadedCategoriesCount(){
        return Challenges.getManager().getCategories().size();
    }

    public static int getActiveCategoriesCount(){
        int count = 0;
        for(Category c : Challenges.getManager().getCategories()){
            if(c.isActive())count++;
        }
        return count;
    }

    public static String getInactiveCategories(){
        String categories = "";
        int count = 0;
        for(Category c : Challenges.getManager().getCategories()){
            if(!c.isActive()){
                categories+=" §f- §c"+c.getName()+"\n";
                count++;
            }
        }
        categories+="§a"+count+" §2catégories inactives";
        return count==0?"§aToutes les catégories sont actives.":"§4Catégorie(s) inactive(s);\n"+categories;
    }

    public static int getLoadedChallengesCount(){
        return Challenges.getManager().getChallenges().size();
    }

    public static int getActiveChallengesCount(){
        int count = 0;
        for(Challenge c : Challenges.getManager().getChallenges()){
            if(c.isActive())count++;
        }
        return count;
    }

    public static String getInactiveChallenges(){
        String challenges = "";
        int count = 0;
        for(Challenge c : Challenges.getManager().getChallenges()){
            if(!c.isActive()){
                challenges+=" §f- §c"+c.getName()+"\n";
                count++;
            }
        }
        challenges+="§a"+count+" §2Challenges inactifs";
        return count==0?"§aTout les challenges sont actifs.":"§4Challenge(s) inactif(s);\n"+challenges;
    }

    public static int getLoadedPlayers(){
        return Challenges.getManager().getLoadedPlayers().size();
    }

    public static int getLoadedIslandStorages(){
        return Challenges.getManager().getLoadedStorages().size();
    }

    public static int getIslandsToResetCount(){
        return Challenges.getResetManager().getIslands().size();
    }

    public static Set<String> getLoadedLangs(){
        return Challenges.getLangManager().getAvailableLangs();
    }

    public static String getCurrentEditorSession(){
        if(Challenges.getEditor()!=null){
            SessionManager.Session s = Challenges.getEditor().getCurrent();
            if(s!=null){
                return "§a"+s.getOwner().getName()+" §7(Clé: §8"+s.getKey()+"§7, "+(s.activeSince==-1?"§cInactive":"§7Active depuis §8"+((System.currentTimeMillis()-s.activeSince)/1000)+" §7sec")+"§7)";
            }
        }
        return "§cAucune";
    }
}
