package smeo.experiments.playground.spring.model;

/**
 * Created by truehl on 05.07.16.
 */
public class Person {
    private String name;
    private String address;
    private String age;

    Person(){
        System.out.println("Person created");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAge() {
        return age;
    }
}
