package Collections;

import java.util.*;

public class HashSetPractise {
    public static void main(String[] args) {

        HashSet<String> fruits = new HashSet<>();

        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Mango");
        fruits.add("Orange");
        fruits.add("Apple");

        System.out.println("Fruits in the HashSet: " + fruits);

        if (fruits.contains("Mango")) {
            System.out.println("Mango is in the set.");
        }

        fruits.remove("Banana");
        System.out.println("After removing Banana: " + fruits);

        System.out.println("Iterating using Iterator:");
        Iterator<String> it = fruits.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        fruits.clear();
        System.out.println("After clearing: " + fruits);
    }
}
