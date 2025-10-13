package Collections;

import java.util.*;

public class EntrySetPractise {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();

        map.put(1, "Apple");
        map.put(2, "Banana");
        map.put(3, "Mango");
        map.put(4, "Orange");

        System.out.println("Original Map: " + map);

        System.out.println("\nIterating using entrySet():");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getKey() == 2) {
                entry.setValue("Blueberry");
            }
        }

        System.out.println("\nMap after updating key 2: " + map);

        Iterator<Map.Entry<Integer, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, String> entry = it.next();
            if (entry.getKey() == 3) {
                it.remove();
            }
        }

        System.out.println("\nMap after removing key 3: " + map);
    }
}
