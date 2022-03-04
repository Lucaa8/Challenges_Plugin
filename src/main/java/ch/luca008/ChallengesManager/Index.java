package ch.luca008.ChallengesManager;

import ch.luca008.Admin.Editor.Sockets.Packets.IPacket;
import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.Comparators.ChallengesSlotComparator;
import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Index {

    Manager manager;
    private HashMap<Category, List<Challenge>> links = new HashMap<>();
    private HashMap<Category, List<Challenge>> requiredForCategories = new HashMap<>();
    private HashMap<Challenge, List<Challenge>> requiredForChallenges = new HashMap<>();

    public Index(JSONObject json){
        manager = Challenges.getManager();
        JSONObject l = (JSONObject) json.get("Challenges");
        JSONObject cat = (JSONObject) json.get("RequiredForCategories");
        for (Category category : manager.getCategories()) {
            links.put(category, fromArray(l, category.getUuid()));
            requiredForCategories.put(category, fromArray(cat, category.getUuid()));
        }
        JSONObject cha = (JSONObject) json.get("RequiredForChallenges");
        for (Challenge challenge : manager.getChallenges()) {
            requiredForChallenges.put(challenge, fromArray(cha, challenge.getUuid()));
        }
    }

    private JSONObject toJson(){
        JSONObject j = new JSONObject();
        JSONObject Challenges = new JSONObject();
        JSONObject RequiredForCategories = new JSONObject();
        JSONObject RequiredForChallenges = new JSONObject();
        links.forEach((Cat,ChaList)->{
            JSONArray jarr = new JSONArray();
            ChaList.forEach(c->jarr.add(c.getUuid().toString()));
            Challenges.put(Cat.getUuid().toString(), jarr);
        });
        requiredForCategories.forEach((Cat,ChaList)->{
            JSONArray jarr = new JSONArray();
            ChaList.forEach(c->jarr.add(c.getUuid().toString()));
            RequiredForCategories.put(Cat.getUuid().toString(), jarr);
        });
        requiredForChallenges.forEach((Cha,ChaList)->{
            JSONArray jarr = new JSONArray();
            ChaList.forEach(c->jarr.add(c.getUuid().toString()));
            RequiredForChallenges.put(Cha.getUuid().toString(), jarr);
        });
        j.put("Challenges", Challenges);
        j.put("RequiredForCategories", RequiredForCategories);
        j.put("RequiredForChallenges", RequiredForChallenges);
        return j;
    }

    public void unload(){
        try{
            File f = Manager.indexFile;
            if(!f.exists()){
                f.createNewFile();
            }
            Files.write(Paths.get(f.toURI()), JsonUtils.prettyJson(toJson()).getBytes(StandardCharsets.UTF_8));
            links = null;
            requiredForCategories = null;
            requiredForChallenges = null;
        }catch (IOException e) {
            System.err.println("Can't unload indexes to index.json file.");
            e.printStackTrace();
        }
    }

    /**
     * Add a new entry to categories/required for categories links (Category-Challenges list) if it doesn't exist. Create a new empty list so after this method, the category doesn't contain any challenges
     * @param category the category to add
     */
    public void addCategory(Category category){
        if(!links.containsKey(category)){
            links.put(category, new ArrayList<>());
        }
        if(!requiredForCategories.containsKey(category)){
            requiredForCategories.put(category, new ArrayList<>());
        }
    }

    /**
     * Remove the specified category of the current active links set if contained (else do nothing).
     * <p>
     * Can't remove a category if the associated challenges list isn't empty
     * @param category the category to remove
     * @return true if category has been removed. false if links doesn't contain category OR links contains category but challenges list isn't empty
     */
    public boolean removeCategory(Category category){
        if(links.containsKey(category)&&links.get(category).isEmpty()){
            links.remove(category);
            requiredForCategories.remove(category);
            return true;
        }
        return false;
    }

    /**
     * Add the specified challenge to the category's current challenges list or create a new one if doesn't exist + add the challenge.
     * <p>
     * Method doesn't add the challenge twice if already exists in the category's challenges list.
     * @param category the category to edit
     * @param challenge the challenge to add
     */
    public void addChallenge(Category category, Challenge challenge){
        if(!links.containsKey(category)){
            addCategory(category);
        }
        List<Challenge> challenges = links.get(category);
        if(!challenges.contains(challenge)){
            challenges.add(challenge);
            links.put(category, challenges);
        }
    }

    /**
     * Remove the specified challenge to the category's current challenges list. Doesn't throw errors if category or challenge doesn't exist in the map/list
     * @param category the category to edit
     * @param challenge the challenge to remove
     */
    public void removeChallenge(Category category, Challenge challenge){
        if(links.containsKey(category)){
            List<Challenge> challenges = links.get(category);
            if(challenges.contains(challenge)){
                challenges.remove(challenge);
                links.put(category, challenges);
            }
        }
    }

    /**
     * Add the specified challenge to the required list of a category or challenge. If category/challenge doesn't have a required list it will create a new one and add the challenge to it.
     * <p>
     * Also create the default category-challenges list set if non-existent ({@link #addCategory(Category)})
     * @param type the target type of the uuid. (CHALLENGE or CATEGORY)
     * @param uuid the uuid of the target to add {@param challenge}
     * @param challenge the required challenge to add
     */
    public void addRequired(IPacket.Target type, UUID uuid, Challenge challenge){
        if(type==IPacket.Target.CATEGORY){
            Optional<Category> cat = manager.retrieveCategoryByUUID(uuid);
            if(cat.isPresent()){
                Category c = cat.get();
                if(!requiredForCategories.containsKey(c)){
                    addCategory(c);
                }
                List<Challenge> required = requiredForCategories.get(c);
                if(!required.contains(challenge)){
                    required.add(challenge);
                    requiredForCategories.put(c,required);
                }
            }
        }else if(type==IPacket.Target.CHALLENGE){
            Optional<Challenge> cha = manager.retrieveChallengeByUUID(uuid);
            if(cha.isPresent()){
                Challenge c = cha.get();
                if(!requiredForChallenges.containsKey(c)){
                    requiredForChallenges.put(c,new ArrayList<>());
                }
                List<Challenge> required = requiredForChallenges.get(c);
                if(!required.contains(challenge)){
                    required.add(challenge);
                    requiredForChallenges.put(c,required);
                }
            }
        }
    }

    /**
     * Remove the specified challenge of the required list of a category or challenge. If category/challenge doesn't have a required list it will do nothing.
     * @param type the target type of the uuid. (CHALLENGE or CATEGORY)
     * @param uuid the uuid of the target to remove {@param challenge}
     * @param challenge the required challenge to remove
     */
    public void removeRequired(IPacket.Target type, UUID uuid, Challenge challenge){
        if(type==IPacket.Target.CATEGORY){
            Optional<Category> cat = manager.retrieveCategoryByUUID(uuid);
            if(cat.isPresent()) {
                Category c = cat.get();
                if(requiredForCategories.containsKey(c)){
                    List<Challenge> required = requiredForCategories.get(c);
                    if(required.contains(challenge)){
                        required.remove(challenge);
                        requiredForCategories.put(c,required);
                    }
                }
            }
        }
        else if(type==IPacket.Target.CHALLENGE){
            Optional<Challenge> cha = manager.retrieveChallengeByUUID(uuid);
            if(cha.isPresent()) {
                Challenge c = cha.get();
                if(requiredForChallenges.containsKey(c)){
                    List<Challenge> required = requiredForChallenges.get(c);
                    if(required.contains(challenge)){
                        required.remove(challenge);
                        requiredForChallenges.put(c,required);
                    }
                }
            }
        }
    }

    private List<Challenge> fromArray(JSONObject json, UUID key){
        List<UUID> contained = new ArrayList<>();
        if(json.containsKey(key.toString())){
            ((JSONArray)json.get(key.toString())).forEach(s->{
                contained.add(UUID.fromString((String)s));
            });
        }
        List<Challenge> retrieve = manager.retrieveChallengesByUUID(contained.toArray(new UUID[0]));
        retrieve.sort(new ChallengesSlotComparator());
        return retrieve;
    }

    public List<Challenge> getChallenges(Category c){
        if(links.containsKey(c)){
            return links.get(c);
        }
        return new ArrayList<>();
    }

    public List<Challenge> getRequiredChallenges(Challenge c){
        if(requiredForChallenges.containsKey(c)){
            return requiredForChallenges.get(c);
        }
        return new ArrayList<>();
    }

    public List<Challenge> getRequiredChallenges(Category c){
        if(requiredForCategories.containsKey(c)){
            return requiredForCategories.get(c);
        }
        return new ArrayList<>();
    }
}
