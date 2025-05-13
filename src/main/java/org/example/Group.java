package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<User> members = new ArrayList<>();

    public Group(String name){
        this.name = name;
    }
    public ArrayList<User> getMembers() {
        return members;
    }

    public boolean addMember(User user){
        if (this.members.contains(user)){
            return false;
        }
        this.members.add(user);
        return true;
    }

    public String getName() {
        return name;
    }
}
