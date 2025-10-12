package Collections;

import java.util.*;

public class AlPractise {
    public static void main(String[] args) {
        ArrayList<Integer> arr = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            arr.add(i + 1);
        }
        System.out.println(arr);

        arr.remove(3);
        System.out.println(arr);

        arr.set(3, 99);
        System.out.println(arr);

        for (int i = 0; i < arr.size(); i++) {
            System.out.println("Element " + (i + 1) + " is " + arr.get(i));
        }
        System.out.println(arr);

    }
}
