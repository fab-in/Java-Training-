package Java_8_Features;

import java.util.*;
import java.util.stream.Collectors;

public class lambda_func {
    public static void main(String[] args) {

        Greeting greeting = name -> System.out.println("Hello, " + name + "!");
        greeting.sayHello("Fabin");

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");
        System.out.println("\nUsing lambda in forEach:");
        names.forEach(n -> System.out.println("Hi " + n));

        List<String> fruits = Arrays.asList("Banana", "Apple", "Mango", "Cherry");
        System.out.println("\nBefore Sorting: " + fruits);

        Collections.sort(fruits, (a, b) -> a.compareTo(b));
        System.out.println("After Sorting (Ascending): " + fruits);

        Collections.sort(fruits, (a, b) -> b.compareTo(a));
        System.out.println("After Sorting (Descending): " + fruits);

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

        List<Integer> evenNumbers = numbers.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
        System.out.println("\nEven numbers: " + evenNumbers);

        List<Integer> squares = numbers.stream()
                .map(n -> n * n)
                .collect(Collectors.toList());
        System.out.println("Squares: " + squares);

        List<Integer> evenSquares = numbers.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .collect(Collectors.toList());
        System.out.println("Even Squares: " + evenSquares);
    }
}

interface Greeting {
    void sayHello(String name);
}
