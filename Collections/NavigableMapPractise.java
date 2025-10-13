package Collections;

import java.util.*;

public class NavigableMapPractise {
    public static void main(String[] args) {
        NavigableMap<String, Integer> nm = new TreeMap<String, Integer>();

        nm.put("C", 1);
        nm.put("Y", 2);
        nm.put("A", 3);
        nm.put("T", 4);
        nm.put("B", 5);
        nm.put("A", 6);

        System.out.println("Mappings of NavigableMap : "
                + nm);

        System.out.printf("Descending Set : %s%n",
                nm.descendingKeySet());
        System.out.printf("Floor Entry : %s%n",
                nm.floorEntry("F"));
        System.out.printf("First Entry : %s%n",
                nm.firstEntry());
        System.out.printf("Last Key : %s%n", nm.lastKey());
        System.out.printf("First Key : %s%n",
                nm.firstKey());
        System.out.printf("Original Map : %s%n", nm);
        System.out.printf("Reverse Map : %s%n",
                nm.descendingMap());

        System.out.println("Iteration");
        Iterator<NavigableMap.Entry<String, Integer>> itr = nm.entrySet().iterator();
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }

    }

}
