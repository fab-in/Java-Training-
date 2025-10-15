package Basic_Projects;

import java.util.*;

abstract class Task {
    private String title;
    private String description;
    private boolean completed;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void markCompleted() throws TaskAlreadyCompletedException {
        if (this.completed) {
            throw new TaskAlreadyCompletedException("Task is already completed!");
        }
        this.completed = true;
    }

    public abstract int getPriority();

    public abstract void displayDetails();
}

class TaskAlreadyCompletedException extends Exception {
    public TaskAlreadyCompletedException(String message) {
        super(message);
    }
}

class WorkTask extends Task {
    private int priority;

    public WorkTask(String title, String description, int priority) {
        super(title, description);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void displayDetails() {
        System.out.println("WORK TASK");
        System.out.println("Title: " + getTitle());
        System.out.println("Description: " + getDescription());
        System.out.println("Priority: " + priority);
        System.out.println("Status: " + isCompleted());
        System.out.println("---------------------------");
    }
}

class PersonalTask extends Task {
    private int priority;

    public PersonalTask(String title, String description, int priority) {
        super(title, description);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void displayDetails() {
        System.out.println("PERSONAL TASK");
        System.out.println("Title: " + getTitle());
        System.out.println("Description: " + getDescription());
        System.out.println("Priority: " + priority);
        System.out.println("Status: " + isCompleted());
        System.out.println("---------------------------");
    }
}

public class TaskManager {
    private static ArrayList<Task> tasks = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;

        do {
            System.out.println("\n----TASK MANAGER----");
            System.out.println("1. Add Work Task");
            System.out.println("2. Add Personal Task");
            System.out.println("3. View All Tasks");
            System.out.println("4. Mark Task as Completed");
            System.out.println("5. Show Tasks Sorted by Priority");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: {
                    addWorkTask();
                    break;
                }
                case 2: {
                    addPersonalTask();
                    break;
                }
                case 3: {
                    viewTasks();
                    break;
                }
                case 4: {
                    markTaskCompleted();
                    break;
                }
                case 5: {
                    showTasksSortedByPriority();
                    break;
                }
                case 6: {
                    System.out.println("Goodbye");
                }
                default: {
                    System.out.println("Invalid choice");
                }
            }
        } while (choice != 6);
    }

    private static void addWorkTask() {
        System.out.print("Enter title: ");
        String title = sc.nextLine();
        System.out.print("Enter description: ");
        String desc = sc.nextLine();
        System.out.print("Enter Priority Level (integer, higher = more important): ");
        int priority = sc.nextInt();
        sc.nextLine();

        Task t = new WorkTask(title, desc, priority);
        tasks.add(t);
        System.out.println("Work task added");
    }

    private static void addPersonalTask() {
        System.out.print("Enter title: ");
        String title = sc.nextLine();
        System.out.print("Enter description: ");
        String desc = sc.nextLine();
        System.out.print("Enter Priority Level (integer, higher = more important): ");
        int priority = sc.nextInt();
        sc.nextLine();

        Task t = new PersonalTask(title, desc, priority);
        tasks.add(t);
        System.out.println("Personal task added");
    }

    private static void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available");
            return;
        }
        System.out.println("\n----TASK LIST----");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("Task " + (i + 1));
            tasks.get(i).displayDetails();
        }
    }

    private static void markTaskCompleted() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available to mark");
            return;
        }
        viewTasks();
        System.out.print("Enter task number to mark completed: ");
        int num = sc.nextInt();
        if (num > 0 && num <= tasks.size()) {
            Task currentTask = tasks.get(num - 1);
            try {
                currentTask.markCompleted();
                System.out.println("Task marked as completed");
            } catch (TaskAlreadyCompletedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid task number");
        }
    }

    private static void showTasksSortedByPriority() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available to display");
            return;
        }

        ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> t2.getPriority() - t1.getPriority());

        System.out.println("\n-------Sorted Tasks------");
        for (int i = 0; i < sortedTasks.size(); i++) {
            System.out.println("Task " + (i + 1));
            sortedTasks.get(i).displayDetails();
        }
    }
}
