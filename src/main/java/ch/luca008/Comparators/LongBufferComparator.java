package ch.luca008.Comparators;

import java.util.Comparator;

public class LongBufferComparator implements Comparator<Long> {
    @Override
    public int compare(Long o1, Long o2) {
        int count1 = (int)(o1 >> 32);
        int count2 = (int)(o2 >> 32);
        if(count1>count2)return -1;
        if(count1<count2)return 1;
        return Integer.compare((int)o1.longValue(), (int)o2.longValue())*-1;
    }
}
