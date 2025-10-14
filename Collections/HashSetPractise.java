package Collections;

import java.util.*;

public class HashSetPractise {
    public static void main(String[] args) {

        HashSet<String> fruits = new HashSet<>();

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Fruits:");

        for (int i = 0; i < 4; i++) {
            String value = sc.next();
            fruits.add(value);
        }

        System.out.println("Fruits in the HashSet: " + fruits);

        System.out.println("Enter fruit to be checked: ");
        String checkFruit = sc.next();

        if (fruits.contains(checkFruit)) {
            System.out.println(checkFruit + "is in the set.");
        } else {
            System.out.println(checkFruit + " is not in the set.");
        }

        System.out.println("Enter fruit to be removed: ");
        String removeFruit = sc.next();
        fruits.remove(removeFruit);
        System.out.println("After removing" + removeFruit + ": " + fruits);

        System.out.println("Iterating using Iterator:");
        Iterator<String> it = fruits.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        fruits.clear();
        System.out.println("After clearing: " + fruits);
    }
}
