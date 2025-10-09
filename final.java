final class Vehicle {
    void showType() {
        System.out.println("This is a vehicle.");
    }
}

class Car {
    final void startEngine() {
        System.out.println("Engine started!");
    }
}

class SportsCar extends Car {
    void display() {
        System.out.println("This is a sports car.");
    }

}

public class Main {
    public static void main(String[] args) {
        final int speedLimit = 120; 
        System.out.println("Speed limit:" + speedLimit);

        Vehicle v = new Vehicle();
        v.showType();

        SportsCar sc = new SportsCar();
        sc.startEngine(); 
        sc.display();
    }
}
