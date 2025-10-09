import java.io.*;

class Geeks {

    Geeks(String name)
    {
        System.out.println("Constructor with one "
                           + "argument - String: " + name);
    }

    Geeks(String name, int age)
    {

        System.out.println(
            "Constructor with two arguments: "
            + " String and Integer: " + name + " " + age);
    }

    Geeks(long id)
    {
        System.out.println(
            "Constructor with one argument: "
            + "Long: " + id);
    }
}

class GFG {
    public static void main(String[] args)
    {

        Geeks geek2 = new Geeks("Sweta");

        Geeks geek3 = new Geeks("Amiya", 28);

        Geeks geek4 = new Geeks(325614567);
    }
}