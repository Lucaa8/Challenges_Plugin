package ch.luca008.Utils;

/*public class HeadsUtils {

    public enum Categories{
        ALPHABET("alphabet"),
        ANIMALS("animals"),
        BLOCKS("blocks"),
        DECORATION("decoration"),
        FOOD_DRINKS("food-drinks"),
        HUMANS("humans"),
        HUMANOID("humanoid"),
        MISCELLANEOUS("miscellaneous"),
        MONSTERS("monsters"),
        PLANTS("plants");
        private String cat;
        Categories(String cateogy){
            this.cat=cateogy;
        }
        public String getCategory(){
            return cat;
        }
    }

    private Map<UUID,HeadProperties> fetched = new HashMap<>();

    private JSONArray get(HeadsUtils.Categories category, boolean tags){
        try {
            URL u = new URL("https://minecraft-heads.com/scripts/api.php?cat="+category.getCategory()+(tags?"&tags=true":""));
            URLConnection conn = u.openConnection();
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str = "";
            for(String line; (line = reader.readLine()) != null;) str+=line;
            return (JSONArray) new JSONParser().parse(str);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public HeadProperties fetch(Categories category, String id){
        UUID uuid = UUID.fromString(id);
        if(fetched.containsKey(uuid)){
            return fetched.get(uuid);
        }
        JSONArray jarr = get(category, false);
        for (Object o : jarr) {
            JSONObject json = (JSONObject) o;
            if(json.containsKey("uuid")&&UUID.fromString((String)json.get("uuid")).equals(uuid)){
                HeadProperties property = new HeadProperties(json);
                fetched.put(uuid, property);
                return property;
            }
        }
        return null;
    }

    public static class HeadProperties{
        private String name = StringUtils.enumName(Material.PLAYER_HEAD);
        private UUID uuid = UUID.randomUUID();
        private String value;
        private List<String> tags = new ArrayList<>();

        public HeadProperties(JSONObject json){
            if(json.containsKey("name")){
                name = (String) json.get("name");
            }
            if(json.containsKey("uuid")){
                uuid = UUID.fromString((String)json.get("uuid"));
            }
            if(json.containsKey("value")){
                value = (String)json.get("value");
            }
            if(json.containsKey("tags")){
                tags.addAll(Arrays.asList(((String) json.get("tags")).split(",")));
            }
        }

        public ItemStack applySignature(ItemStack item){
            Object nmsItem = NMSManager.getNMSItem(item);
            Object tags = NMSManager.getTags(item);//NBTTagCompound "tag"
            Class<?> itemstack = NMSManager.getNMSClass("ItemStack");
            Class<?> nbtbase = NMSManager.getNMSClass("NBTBase");
            Class<?> nbtstring = NMSManager.getNMSClass("NBTTagString");
            Class<?> nbtlist = NMSManager.getNMSClass("NBTTagList");
            Object id = NMSManager.invoke(nbtstring, "a", new Class[]{String.class}, nbtstring, uuid.toString());
            Object val = NMSManager.invoke(nbtstring, "a", new Class[]{String.class}, nbtstring, value);
            Object compoundSO = null;
            Object compoundProp = null;
            Object compound0 = null;
            Object textures = null;
            try{
                textures = nbtlist.getConstructor().newInstance();
                compoundSO = NMSManager.getNMSClass("NBTTagCompound").getConstructor().newInstance();
                compoundProp = compoundSO.getClass().getConstructor().newInstance();
                compound0 = compoundSO.getClass().getConstructor().newInstance();
            }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
                System.err.println("Can't get a new instance for NBTTagCompund or NBTTagList.");
                e.printStackTrace();
            }
            if(compound0!=null){
                NMSManager.invoke(compound0.getClass(), "set", new Class[]{String.class, nbtbase}, compound0, "Value", val);
            }
            if(textures!=null){
                NMSManager.invoke(textures.getClass(), "b", new Class[]{int.class, nbtbase}, textures, 0, compound0);
            }
            if(compoundProp!=null){
                NMSManager.invoke(compoundProp.getClass(), "set", new Class[]{String.class, nbtbase}, compoundProp, "textures", textures);
            }
            if(compoundSO!=null){
                NMSManager.invoke(compoundSO.getClass(), "set", new Class[]{String.class, nbtbase}, compoundSO, "Id", id);
                NMSManager.invoke(compoundSO.getClass(), "set", new Class[]{String.class, nbtbase}, compoundSO, "Properties", compoundProp);
            }
            NMSManager.invoke(tags.getClass(), "set", new Class[]{String.class, nbtbase}, tags, "SkullOwner", compoundSO);
            NMSManager.invoke(itemstack, "setTag", new Class[]{tags.getClass()}, nmsItem, tags);
            item = NMSManager.getBukkitItem(nmsItem);
            return item;
        }
        public String getName() {
            return name;
        }
        public UUID getUuid() {
            return uuid;
        }
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "\nHeadProperties{\n" +
                    "name=\"" + name + "\",\n"+
                    "uuid=\"" + uuid + "\",\n" +
                    "value=\"" + value + "\",\n" +
                    "tags=" + tags + "\n" +
                    '}';
        }
    }
}*/ //Pour un futur inventaire peut-être avec toutes les têtes d'une catégorie
