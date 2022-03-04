package ch.luca008.Comparators;

import ch.luca008.ChallengesManager.Categories.Category;

import java.util.Comparator;

public class CategoriesSlotComparator implements Comparator<Category> {
    @Override
    public int compare(Category o1, Category o2) {
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
