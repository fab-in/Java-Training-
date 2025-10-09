class Student {
    static String college = "RSET"; // static variable
    String name;

    Student(String n) {
        name = n;
    }

    void display() {
        System.out.println(name + " studies at " + college);
    }
}

public class Main {
    public static void main(String[] args) {
        Student s1 = new Student("Fabin");
        Student s2 = new Student("Elviin");

        s1.display();
        s2.display();

        // Changing static variable affects both
        Student.college = "Rajagiri";
        s1.display();
        s2.display();
    }
}