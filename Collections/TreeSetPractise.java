package Collections;

import java.util.*;

public class TreeSetPractise {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        TreeSet<Integer> numbers = new TreeSet<>();

        System.out.print("Enter number of elements to add: ");
        int n = sc.nextInt();

        System.out.println("Enter " + n + " numbers:");
        for (int i = 0; i < n; i++) {
            numbers.add(sc.nextInt());
        }

        System.out.println("TreeSet elements: " + numbers);

        System.out.print("Enter number of elements to remove: ");
        int m = sc.nextInt();

        System.out.println("Enter " + m + " numbers to remove:");
        for (int i = 0; i < m; i++) {
            int num = sc.nextInt();
            if (numbers.remove(num))
                System.out.println(num + " removed.");
            else
                System.out.println(num + " not found.");
        }

        System.out.println("Updated TreeSet: " + numbers);
        sc.close();
    }
}
