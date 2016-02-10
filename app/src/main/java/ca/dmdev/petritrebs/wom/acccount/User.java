package ca.dmdev.petritrebs.wom.acccount;

/**
 * Created by mathe_000 on 2016-02-08.
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String gender;

    public User(int id, String name, String email, String gender){
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
    }
}
