package ch.luca008.ChallengesManager.Required;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.Comparators.BlockCountComparator;
import ch.luca008.Comparators.EntitiesCountComparator;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.SkyblockUtils;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class Island implements Required{

    private int radius;
    private List<Block> blocks;
    private List<Entity> entities;

    public Island(JSONObject json){
        if(json.containsKey("Radius")){
            this.radius = JsonUtils.getInt(json, "Radius");
        }else radius = 20;
        if(json.containsKey("Blocks")){
            blocks = new ArrayList<>();
            try{
                blocks = getArray((JSONArray) json.get("Blocks"));
                blocks.sort(new BlockCountComparator());
            }catch(Exception e){
                System.err.println("Can't initialize Block from JSON: " + json.get("Blocks"));
                e.printStackTrace();
            }
        }
        if(json.containsKey("Entities")){
            entities = new ArrayList<>();
            try{
                entities = getArray((JSONArray) json.get("Entities"));
                entities.sort(new EntitiesCountComparator());
            }catch(Exception e){
                System.err.println("Can't initialize Entity from JSON: " + json.get("Entities"));
                e.printStackTrace();
            }
        }
    }

    public Island(int radius, List<Block> blocks, List<Entity> entities){
        this.radius = radius;
        if(blocks != null && !blocks.isEmpty()){
            this.blocks = blocks;
            this.blocks.sort(new BlockCountComparator());
        }
        if(entities != null && !entities.isEmpty()){
            this.entities = entities;
            this.entities.sort(new EntitiesCountComparator());
        }
    }

    public boolean hasBlocks(){
        return blocks!=null&&!blocks.isEmpty();
    }
    public boolean hasEntities(){
        return entities!=null&&!entities.isEmpty();
    }

    private <T> List<T> getArray(JSONArray array){
        List<T> l = new ArrayList<>();
        for(Object o : array){
            JSONObject j = (JSONObject) o;
            if(j.containsKey("Material")){
                l.add((T)new Block(j));
            }else if(j.containsKey("Entity")){
                l.add((T)new Entity(j));
            }
        }
        return l;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        j.put("Radius", radius);
        if(hasBlocks()){
            JSONArray jarr = new JSONArray();
            blocks.forEach(b->{
                jarr.add(b.toJson());
            });
            j.put("Blocks", jarr);
        }
        if(hasEntities()){
            JSONArray jarr = new JSONArray();
            entities.forEach(e->{
                jarr.add(e.toJson());
            });
            j.put("Entities", jarr);
        }
        return j;
    }

    @Override
    public String toLore(Storage.ChallengeStorage storage, UniPlayer player) {
        String lore = player.getChallengeMessage("Challenge-Item-Required-Island-Radius", Map.entry("{0}", radius+""));
        if(hasBlocks()){
            for(Block b : blocks){
                lore+="\n §f- §b"+b.getCount()+" §a"+StringUtils.enumName(b.getMaterial());
            }
        }
        if(hasEntities()){
            for(Entity e : entities){
                lore+="\n §f- §b"+e.getCount()+" §a"+StringUtils.enumName(e.getEntityType());
            }
        }
        return "\n"+lore+"\n";
    }

    @Override
    public String toString(){
        String b = "";
        String e = "";
        if(hasBlocks()){
            b = "{";
            for (Block bl : blocks) {
                b+=bl.toString()+"},";
            }
            b = b.substring(0,b.length()-1) + "}";
        }
        if(hasEntities()){
            e = "{";
            for (Entity en : entities) {
                e+=en.toString()+"},";
            }
            e = e.substring(0,e.length()-1) + "}";
        }
        return "Required{Type:ISLAND,Radius:"+radius+(!b.isEmpty()?",Blocks:"+b:"")+(!e.isEmpty()?",Entities:"+e:"")+"}";
    }

    @Override
    public RequiredType getType() {
        return RequiredType.Island;
    }

    private List<org.bukkit.block.Block> getNearbyBlocks(Location l){
        List<org.bukkit.block.Block> blocks = new ArrayList<>();
        int xMin = l.getBlockX()-radius, xMax = l.getBlockX()+radius;
        int yMin = l.getBlockY()-radius, yMax = l.getBlockY()+radius;
        int zMin = l.getBlockZ()-radius, zMax = l.getBlockZ()+radius;
        for(int x=xMin;x<=xMax;x++){
            for(int y=yMin;y<=yMax;y++){
                for(int z=zMin;z<=zMax;z++){
                    blocks.add(l.getWorld().getBlockAt(x,y,z));
                }
            }
        }
        return blocks;
    }

    @Override
    public CompletableResult complete(Challenge c, UniPlayer p) {
        if(p.getIsland().isPresent() && p.getPlayer().isPresent()){
            boolean isCompleted = true;
            String msgBlocks = "";
            String msgEntities = "";
            Location ploc = p.getPlayer().get().getLocation();
            com.bgsoftware.superiorskyblock.api.island.Island island = p.getIsland().get();
            if(hasBlocks()){
                Map<Block, Integer> missing = new HashMap<>();
                List<org.bukkit.block.Block> nearbyBlocks = getNearbyBlocks(ploc);
                for(Block b : blocks){
                    int got = 0;
                    for(org.bukkit.block.Block block : nearbyBlocks){
                        if(block.getType()==b.getMaterial()&&SkyblockUtils.isOnIsland(island,block.getLocation()))got++;
                    }
                    int missingCount = b.getCount()-got;
                    if(missingCount>0)missing.put(b, missingCount);
                }
                if(!missing.isEmpty()){
                    isCompleted = false;
                    String msg = "";
                    for(Map.Entry<Block, Integer> missingCount : missing.entrySet()){
                        msg+=" §7- §a"+missingCount.getValue()+" §b"+StringUtils.enumName(missingCount.getKey().getMaterial())+"\n";
                    }
                    msgBlocks = p.getMessage("Challenge-Completion-Island-Blocks", Map.entry("{0}",msg));
                }
            }
            if(hasEntities()){
                Map<Entity, Integer> missing = new HashMap<>();
                Collection<org.bukkit.entity.Entity> nearbyEntities = ploc.getWorld().getNearbyEntities(ploc, radius, radius, radius);
                for(Entity e : entities){
                    int got = 0;
                    for(org.bukkit.entity.Entity entity : nearbyEntities){
                        if(entity.getType()==e.getEntityType()&&SkyblockUtils.isOnIsland(island,entity.getLocation()))got++;
                    }
                    int missingCount = e.getCount()-got;
                    if(missingCount>0)missing.put(e, missingCount);
                }
                if(!missing.isEmpty()){
                    isCompleted = false;
                    String msg = "";
                    for(Map.Entry<Entity, Integer> missingCount : missing.entrySet()){
                        msg+=" §7- §a"+missingCount.getValue()+" §b"+StringUtils.enumName(missingCount.getKey().getEntityType())+"\n";
                    }
                    msgEntities = p.getMessage("Challenge-Completion-Island-Entities",Map.entry("{0}",msg));
                }
            }
            boolean finalIsCompleted = isCompleted;
            String finalBlocks = msgBlocks;
            String finalEntities = msgEntities;
            return new CompletableResult() {
                @Override
                public boolean isCompleted() {
                    return finalIsCompleted;
                }

                @Override
                public boolean hasProgressed(){
                    return false;
                }

                @Override
                public Object getMessage() {
                    String msg = "";
                    if(!finalIsCompleted){
                        msg = p.getMessage("Challenge-Completion-Unable", Map.entry("{0}", c.getName()));
                        if(!finalBlocks.isEmpty())msg+=finalBlocks;
                        if(!finalEntities.isEmpty())msg+=finalEntities;
                    }
                    return msg;
                }

                @Override
                public UniPlayer getPlayer(){
                    return p;
                }

                @Override
                public Challenge getChallenge(){
                    return c;
                }
            };
        }
        return null;
    }

    public static class Block{
        UUID uuid;
        int count;
        Material material;
        public Block(Material m, int count){
            this.uuid = UUID.randomUUID();
            this.material = m;
            this.count = count;
        }
        public Block(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Material")){
                material = Material.valueOf((String)json.get("Material"));
            }else material = Material.STONE;
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json,"Count");
            }else count = 64;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Material", material.name());
            j.put("Count", count);
            return j;
        }
        public UUID getUuid(){
            return uuid;
        }
        @Override
        public String toString(){
            return "Block{UUID:"+uuid.toString()+",Material:"+material.name()+",Count:"+count+"}";
        }
        public Material getMaterial(){
            return material;
        }
        public int getCount(){
            return count;
        }
    }

    public static class Entity{
        UUID uuid;
        int count;
        EntityType entity;
        public Entity(EntityType entity, int count){
            this.uuid = UUID.randomUUID();
            this.entity = entity;
            this.count = count;
        }
        public Entity(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Entity")){
                entity = EntityType.valueOf((String)json.get("Entity"));
            }else entity = EntityType.ZOMBIE;
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json,"Count");
            }else count = 8;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Entity", entity.name());
            j.put("Count", count);
            return j;
        }
        public UUID getUuid(){
            return uuid;
        }
        @Override
        public String toString(){
            return "Entity{UUID:"+uuid.toString()+",EntityType:"+entity.name()+",Count:"+count+"}";
        }
        public EntityType getEntityType(){
            return entity;
        }
        public int getCount(){
            return count;
        }
    }

}

