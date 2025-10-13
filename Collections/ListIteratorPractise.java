package Collections;

import java.util.*;

public class ListIteratorPractise {
    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<>();

        arr.add("I");
        arr.add("am");
        arr.add("Learning");
        arr.add("Java");

        System.out.println("Fprward Direction :");
        ListIterator<String> li = arr.listIterator();
        while (li.hasNext()) {
            System.out.print(li.next() + " ");
        }

        System.out.println("\nBackward Direction :");
        while (li.hasPrevious()) {
            System.out.print(li.previous() + " ");
        }

    }

}
