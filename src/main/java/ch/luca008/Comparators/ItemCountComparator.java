package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Required.Items;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.Utils.ItemUtils;
import ch.luca008.Utils.SbItem;

import java.util.Comparator;

public class ItemCountComparator implements Comparator<Items.Item> {

    private final boolean invert;

    public ItemCountComparator(boolean invert){
        this.invert = invert;
    }

    public ItemCountComparator(){
        this(false);
    }

    @Override
    public int compare(Items.Item o1, Items.Item o2) {
        if(o1.getCount()>o2.getCount())return invert ? -1 : 1;
        if(o1.getCount()<o2.getCount())return invert ? 1 : -1;
        if(o1.getCount()==o2.getCount()){
            return getName(o1.getItem()).compareTo(getName(o2.getItem()));
        }
        return 0;
    }

    private String getName(SbItem item){
        return item.getName() == null ? ch.luca008.SpigotApi.Utils.StringUtils.enumName(item.getMaterial()) : item.getName();
    }
}
