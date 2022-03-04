package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Required.Items;

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
            return o1.getItem().getName().compareTo(o2.getItem().getName());
        }
        return 0;
    }
}
