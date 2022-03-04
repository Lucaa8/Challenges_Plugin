package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Required.Island;

import java.util.Comparator;

public class BlockCountComparator implements Comparator<Island.Block> {
    @Override
    public int compare(Island.Block o1, Island.Block o2) {
        if(o1.getCount()>o2.getCount())return 1;
        if(o1.getCount()<o2.getCount())return -1;
        if(o1.getCount()==o2.getCount()){
            return o1.getMaterial().name().compareTo(o2.getMaterial().name());
        }
        return 0;
    }
}
