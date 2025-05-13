package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class RegularFile extends File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    ArrayList<Byte> content = new ArrayList<>();

    public RegularFile(User owner, Group group, String name, String perms) {
        super(Type.REGULAR, owner, group, name, perms);
    }

    public void write(User user, ArrayList<Byte> content) {
        if (this.getPermissions().canWrite(user)){
            this.content = content;
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }

    public ArrayList<Byte> read(User user) {
        if (this.getPermissions().canRead(user)){
            return content;
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }

    public String execute(User user) {
        if (this.getPermissions().canExecute(user)){
            return "TODO";
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }

    public int getSize(){
        return content.size();
    }
}
