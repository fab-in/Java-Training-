package Collections;

import java.util.*;

public class HashMapPractise {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();

        map.put(1, "Collections");
        map.put(2, "OOPS");
        map.put(3, "Java");
        map.put(4, "DSA");

        System.out.println(map);

        map.put(2, "SpringBoot");
        System.out.println(map);

        map.remove(1);
        System.out.println(map);

        for (Integer key : map.keySet()) {
            System.out.println("Key: " + key + ", Value: " + map.get(key));
        }

    }

}
