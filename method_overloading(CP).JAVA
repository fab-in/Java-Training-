class Calculator {

    int add(int a) {
        return a + 10;
    }

    int add(int a, int b) {
        return a + b;
    }

    double add(double a, double b) {
        return a + b;
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator c = new Calculator();       
        System.out.println("add(int): " + c.add(5));           
        System.out.println("add(int,int): " + c.add(5, 10));  
        System.out.println("add(double,double): " + c.add(2.5, 3.5)); 
    }
}
