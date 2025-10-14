package Collections;

import java.util.*;

public class LinkedHashMapPrac {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

        System.out.print("Enter number of entries to add: ");
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {
            System.out.print("Enter count: ");
            int key = sc.nextInt();
            sc.nextLine();
            System.out.print("Enter fruit: ");
            String value = sc.nextLine();
            map.put(key, value);
        }

        System.out.println("Map after adding: " + map);

        System.out.print("Enter key to update: ");
        int updateKey = sc.nextInt();
        sc.nextLine();
        if (map.containsKey(updateKey)) {
            System.out.print("Enter new value: ");
            String newValue = sc.nextLine();
            map.put(updateKey, newValue);
            System.out.println("Updated map: " + map);
        } else {
            System.out.println("Key not found.");
        }

        System.out.print("Enter key to remove: ");
        int removeKey = sc.nextInt();
        if (map.containsKey(removeKey)) {
            map.remove(removeKey);
            System.out.println("After removing key " + removeKey + ": " + map);
        } else {
            System.out.println("Key not found.");
        }

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        sc.close();
    }
}
