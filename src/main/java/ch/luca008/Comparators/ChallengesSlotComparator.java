package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Challenges.Challenge;

import java.util.Comparator;

public class ChallengesSlotComparator implements Comparator<Challenge> {
    @Override
    public int compare(Challenge o1, Challenge o2) {
        if(o1.getPage()>o2.getPage()){
            return 1;
        }
        else if(o2.getPage()<o2.getPage()){
            return -1;
        }
        else{
            if(o1.getSlot()>o2.getSlot())return 1;
            else if(o1.getSlot()<o2.getSlot())return -1;
            else{
                return o1.getName().compareTo(o2.getName());
            }
        }
    }
}
