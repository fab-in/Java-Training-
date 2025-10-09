class Parent {
    int a = 10;
    static int b = 20;
}

class RRR extends Parent {
    void GFG() {
        this.a = 100;
        System.out.println("this.a = " + this.a);
        System.out.println("super.a = " + super.a);

        RRR.b = 600;
        System.out.println("this.b = " + this.b);
        System.out.println("super.b = " + super.b);
    }

    public static void main(String[] args) {
        new RRR().GFG();
    }
}
