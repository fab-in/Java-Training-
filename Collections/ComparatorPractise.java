package Collections;

import java.util.*;

class IntegerComparator implements Comparator<Integer> {
    public int compare(Integer o1, Integer o2) {
        return o2 - o1;
    }
}

class StringLengthComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
        return s1.length() - s2.length();
    }
}

public class ComparatorPractise {
    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<>();

        list1.add(12);
        list1.add(5);
        list1.add(8);
        list1.add(1);
        list1.add(20);

        // list1.sort(null);
        // System.out.println("Sorted List by default" + list);

        list1.sort(new IntegerComparator());
        System.out.println("Sorted List by custom comparator desc" + list1);

        List<String> list2 = new ArrayList<>();

        list2.add("malayalam");
        list2.add("apple");
        list2.add("hi");
        list2.add("zebra");
        list2.add("who");

        list2.sort(new StringLengthComparator());
        System.out.println("Sorted List by custom comparator length" + list2);
    }
}
