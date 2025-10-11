class Students {
    static String college = "RSET"; // static variable
    String name;

    Students(String n) {
        name = n;
    }

    void display() {
        System.out.println(name + " studies at " + college);
    }
}

public class StaticKeyword {
    public static void main(String[] args) {
        Students s1 = new Students("Fabin");
        Students s2 = new Students("Elviin");

        s1.display();
        s2.display();

        // Changing static variable affects both
        Students.college = "Rajagiri";
        s1.display();
        s2.display();
    }
}