class Car{
    String model;
    int year;

    Car(String model,int year){
        this.year=year;
        this.model=model;
    }

    void display(){
        System.out.println(model+" "+year);
    }
}

public class Main{
    public static void main(String[] args){
        Car c1=new Car("Pagani",2023);
        c1.display();
    }
}