package finalproject.homesecurity.model;

/**
 * Created by Robbie on 23/09/2015.
 */
public class User {
    private String email;
    private String password;

    public User(String id,String password)
    {
        this.email = id;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
