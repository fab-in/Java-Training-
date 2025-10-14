package Collections;

import java.util.*;

public class AlPractise {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> groceryList = new ArrayList<>();

        while (true) {
            System.out.println("\n=== GROCERY LIST MANAGER ===");
            System.out.println("1. Add an item");
            System.out.println("2. Remove an item");
            System.out.println("3. Update an item");
            System.out.println("4. View all items");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter item to add: ");
                    String item = sc.nextLine();
                    groceryList.add(item);
                    System.out.println("Item added");
                    break;

                case 2:
                    System.out.print("Enter index of item to remove: ");
                    int removeIndex = sc.nextInt();
                    if (removeIndex >= 0 && removeIndex < groceryList.size()) {
                        System.out.println("Item Removed: " + groceryList.remove(removeIndex));
                    } else {
                        System.out.println("Invalid index");
                    }
                    break;

                case 3:
                    System.out.print("Enter index of item to update: ");
                    int updateIndex = sc.nextInt();
                    sc.nextLine();
                    if (updateIndex >= 0 && updateIndex < groceryList.size()) {
                        System.out.print("Enter new item name: ");
                        String newItem = sc.nextLine();
                        groceryList.set(updateIndex, newItem);
                        System.out.println("Item updated");
                    } else {
                        System.out.println("Invalid index!");
                    }
                    break;

                case 4:
                    if (groceryList.isEmpty()) {
                        System.out.println("List is empty");
                    } else {
                        System.out.println("\nYour Grocery List:");
                        for (int i = 0; i < groceryList.size(); i++) {
                            System.out.println(i + ": " + groceryList.get(i));
                        }
                    }
                    break;

                case 5:
                    System.out.println("Goodbye");
                    sc.close();
                    return;

                default:
                    System.out.println("Enter Valid choice");
            }
        }
    }
}
