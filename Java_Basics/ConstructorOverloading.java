
class Trial {

    Trial(String name) {
        System.out.println("Constructor with one "
                + "argument - String: " + name);
    }

    Trial(String name, int age) {

        System.out.println(
                "Constructor with two arguments: "
                        + " String and Integer: " + name + " " + age);
    }

    Trial(long id) {
        System.out.println(
                "Constructor with one argument: "
                        + "Long: " + id);
    }
}

class ConstructorOverloading {
    public static void main(String[] args) {

        Trial t1 = new Trial("Sweta");

        Trial t2 = new Trial("Amiya", 28);

        Trial t3 = new Trial(325614567);
    }
}