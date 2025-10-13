package Collections;

import java.util.*;

public class LinkedHashMapPrac {
    public static void main(String[] args) {

        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

        map.put(1, "First");
        map.put(2, "Second");
        map.put(3, "Third");
        map.put(4, "Fourth");
        map.put(5, "Fifth");

        System.out.println(map);

        map.put(2, "2nd");
        System.out.println(map);

        map.remove(5);
        System.out.println(map);

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
