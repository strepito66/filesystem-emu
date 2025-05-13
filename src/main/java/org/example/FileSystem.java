package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class FileSystem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();

    private Directory root;

    public FileSystem(String defaultgroup){
        Group group = new Group(defaultgroup);
        groups.add(group);
    }

    public ArrayList<User> getUsers() {
        return this.users;
    }

    public void addUser(User user){
        this.users.add(user);
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public void genStructure(User user, Group group){
        this.root = new Directory(user,group, "/", "755");
    }

    public Directory getRoot() {
        return root;
    }


}
