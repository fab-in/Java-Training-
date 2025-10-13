package Collections;

import java.util.*;

public class IteratorPractise {
    public static void main(String[] args) {

        ArrayList<Integer> arr = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            arr.add(i + 1);
        }

        Iterator<Integer> li = arr.iterator();

        while (li.hasNext()) {
            System.out.print(li.next() + " ");
        }
        System.out.println();

        li = arr.iterator();

        System.out.println("After removing elements :");
        while (li.hasNext()) {
            int i = li.next();
            if (i % 2 == 0) {
                li.remove();
            }
        }
        System.out.println(arr);

    }

}
