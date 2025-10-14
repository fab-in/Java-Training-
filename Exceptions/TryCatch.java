package Exceptions;

import java.util.*;

public class TryCatch {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.print("Enter number 1: ");
            int a = sc.nextInt();
            System.out.print("Enter number 2: ");
            int b = sc.nextInt();
            int val = a / b;
            System.out.println("Result: " + val);
        } catch (ArithmeticException | InputMismatchException e) {
            System.out.println("Error is : " + e);
        } finally {
            System.out.println("Execution completed.");

        }
    }
}
