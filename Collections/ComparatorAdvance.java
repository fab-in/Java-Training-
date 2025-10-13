package Collections;

import java.util.*;

class Student {
    String Name;
    double gpa;

    Student(String Name, double gpa) {
        this.Name = Name;
        this.gpa = gpa;
    }

}

class StudentComparator implements Comparator<Student> {

    public int compare(Student s1, Student s2) {
        if (s1.gpa < s2.gpa) {
            return -1;
        } else if (s1.gpa > s2.gpa) {
            return 1;
        } else {
            return 0;
        }
    }
}

public class ComparatorAdvance {
    public static void main(String[] args) {
        Student s1 = new Student("fabin", 8.12);
        Student s2 = new Student("justin", 9.7);
        Student s3 = new Student("kris", 8.0);
        Student s4 = new Student("priya", 8.12);

        List<Student> students = new ArrayList<>();
        students.add(s1);
        students.add(s2);
        students.add(s3);
        students.add(s4);
        students.sort(new StudentComparator());
        for (Student s : students) {
            System.out.println(s.Name + " : " + s.gpa);
        }

    }

}
