class Vehicles {
    Vehicles() {
        System.out.println("This is a Vehicle");
    }
}

class Cars extends Vehicles {
    Cars() {
        System.out.println("This Vehicle is Car");
    }
}

class Bus extends Vehicles {
    Bus() {
        System.out.println("This Vehicle is Bus");
    }
}

public class HierarchialInheritance {
    public static void main(String[] args) {
        Cars obj1 = new Cars();
        Bus obj2 = new Bus();
    }
}