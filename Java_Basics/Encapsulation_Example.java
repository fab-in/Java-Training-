class Student {
    private String name;
    private int age;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAge(int age) {
        if (age > 0) {
            this.age = age;
        } else {
            System.out.println("Age cannot be negative!");
        }
    }

    public int getAge() {
        return age;
    }
}

public class Encapsulation_Example {
    public static void main(String[] args) {
        Student s = new Student();

        s.setName("Fabin");
        s.setAge(21);

        System.out.println("Name: " + s.getName());
        System.out.println("Age: " + s.getAge());

        s.setAge(-5);
    }
}
