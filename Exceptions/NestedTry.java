package Exceptions;

import java.util.*;

public class NestedTry {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Integer> arr = new ArrayList<>();
        System.out.print("Enter numbe rof eleemnts: ");
        int n = sc.nextInt();

        System.out.println("Enter " + n + " elements: ");
        for (int i = 0; i < n; i++) {
            arr.add(sc.nextInt());
        }

        try {
            int val = arr.get(n);
            System.out.println("Value is: " + val);

            try {
                int value = arr.get(n - 1) / 0;
                System.out.println("Value is: " + value);
            } catch (ArithmeticException e) {
                System.out.println("Error is: " + e);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error is: " + e);
        } finally {
            System.out.println("Execution completed.");

        }

    }
}
