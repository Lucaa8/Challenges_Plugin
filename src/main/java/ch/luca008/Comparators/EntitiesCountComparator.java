package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Required.Island;

import java.util.Comparator;

public class EntitiesCountComparator implements Comparator<Island.Entity> {
    @Override
    public int compare(Island.Entity o1, Island.Entity o2) {
        if(o1.getCount()>o2.getCount())return 1;
        if(o1.getCount()<o2.getCount())return -1;
        if(o1.getCount()==o2.getCount()){
            return o1.getEntityType().name().compareTo(o2.getEntityType().name());
        }
        return 0;
    }
}
