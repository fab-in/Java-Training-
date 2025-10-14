package Collections;

import java.util.*;

public class HashMapPractise {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter items and their count");

        for (int i = 0; i < 4; i++) {
            int key = sc.nextInt();
            String value = sc.next();
            map.put(key, value);
        }

        System.out.println(map);

        System.out.println("After updating elements:");
        int val = sc.nextInt();
        String value = sc.next();
        map.put(val, value);
        System.out.println(map);

        System.out.println("After removing elements:");
        int index = sc.nextInt();
        map.remove(index);
        System.out.println(map);

        for (Integer key : map.keySet()) {
            System.out.println("Key: " + key + ", Value: " + map.get(key));
        }

    }

}
