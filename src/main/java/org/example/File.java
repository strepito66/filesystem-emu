package org.example;

import java.io.Serial;
import java.io.Serializable;

public class File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    enum Type {
        REGULAR,
        DIRECTORY
    }

    private Type type;
    private static int globalInode = 0;
    private int inode;
    private User owner;
    private Group group;
    private String name;
    private Permission permissions;

    public File(Type type, User owner, Group group ,String name, String perms) {
        this.type = type;
        this.inode = globalInode;
        globalInode++;
        this.owner = owner;
        this.group = group;
        this.name = name;
        this.permissions = new Permission(perms);
    }

    public Type getType() {
        return this.type;
    }

    public int getInode(){
        return this.inode;
    }

    public String getName(){
        return this.name;
    }

    public User getOwner() { return owner; }

    public Group getGroup() {
        return group;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public void editPerms(User user, String perms) {
        if (this.getPermissions().canWrite(user)){
            this.permissions.setPerms(perms);
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }


    class Permission implements Serializable{
        @Serial
        private static final long serialVersionUID = 1L;
        private boolean ownerR, ownerW, ownerX;
        private boolean groupR, groupW, groupX;
        private boolean otherR, otherW, otherX;


        Permission(String permissions){
            octal(permissions);
        }


        private void octal(String octalString){
            if (octalString == null || octalString.length() != 3){
                throw new IllegalArgumentException("Permissions must be 3 octal digits");
            }

            int ownerBit = Character.getNumericValue(octalString.charAt(0));
            ownerR = (ownerBit & 4) != 0; //check bit del 4 (3)
            ownerW = (ownerBit & 2) != 0; //check bit del 2 (2)
            ownerX = (ownerBit & 1) != 0; //check bit dell'1 (1)

            int groupBit = Character.getNumericValue(octalString.charAt(1));
            groupR = (groupBit & 4) != 0;
            groupW = (groupBit & 2) != 0;
            groupX = (groupBit & 1) != 0;

            int otherBit = Character.getNumericValue(octalString.charAt(2));
            otherR = (otherBit & 4) != 0;
            otherW = (otherBit & 2) != 0;
            otherX = (otherBit & 1) != 0;

        }

        public boolean canRead(User target){
            if (target.getUsername().equals(File.this.owner.getUsername())){
                return ownerR;
            } else if (target.getGroup().equals(File.this.group)) {
                return groupR;
            }else {
                return otherR;
            }
        }

        public boolean canWrite(User target){
            if (target.getUsername().equals(File.this.owner.getUsername())){
                return ownerW;
            } else if (target.getGroup().equals(File.this.group)) {
                return groupW;
            }else {
                return otherW;
            }
        }

        public boolean canExecute(User target){
            if (target.getUsername().equals(File.this.owner.getUsername())){
                return ownerX;
            } else if (target.getGroup().equals(File.this.group)) {
                return groupX;
            }else {
                return otherX;
            }
        }

        public String formatPermissions() {
            return String.valueOf(ownerR ? 'r' : '-') +
                    (ownerW ? 'w' : '-') +
                    (ownerX ? 'x' : '-') +
                    (groupR ? 'r' : '-') +
                    (groupW ? 'w' : '-') +
                    (groupX ? 'x' : '-') +
                    (otherR ? 'r' : '-') +
                    (otherW ? 'w' : '-') +
                    (otherX ? 'x' : '-');
        }

        public void setPerms(String perms){
            octal(perms);
        }

    }
}
