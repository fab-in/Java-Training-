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

    public void markCompleted() {
        this.completed = true;
    }

    public abstract void displayDetails();
}

class WorkTask extends Task {
    private String priority;

    public WorkTask(String title, String description, String priority) {
        super(title, description);
        this.priority = priority;
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
    private String priority;

    public PersonalTask(String title, String description, String priority) {
        super(title, description);
        this.priority = priority;
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
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addWorkTask();
                    break;
                case 2:
                    addPersonalTask();
                    break;
                case 3:
                    viewTasks();
                    break;
                case 4:
                    markTaskCompleted();
                    break;
                case 5:
                    System.out.println("Goodbye");
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        } while (choice != 5);
    }

    private static void addWorkTask() {
        System.out.print("Enter title: ");
        String title = sc.nextLine();
        System.out.print("Enter description: ");
        String desc = sc.nextLine();
        System.out.print("Enter Priority Level: ");
        String project = sc.nextLine();

        Task t = new WorkTask(title, desc, project);
        tasks.add(t);
        System.out.println("✅ Work task added");
    }

    private static void addPersonalTask() {
        System.out.print("Enter title: ");
        String title = sc.nextLine();
        System.out.print("Enter description: ");
        String desc = sc.nextLine();
        System.out.print("Enter Priority Level: ");
        String priority = sc.nextLine();

        Task t = new PersonalTask(title, desc, priority);
        tasks.add(t);
        System.out.println("✅ Personal task added");
    }

    private static void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available");
            return;
        }
        System.out.println("\n----TASK LIST----");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("Task " + (i + 1));
            Task currentTask = tasks.get(i);
            currentTask.displayDetails();
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
            currentTask.markCompleted();
            System.out.println("✅ Task marked as completed");
        } else {
            System.out.println("Invalid task number");
        }
    }
}
