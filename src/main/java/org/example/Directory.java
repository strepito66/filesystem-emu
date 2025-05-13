package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Directory extends File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Map<Integer, File> content = new HashMap<>();
    private Directory parent;

    public Directory(User owner, Group group, String name, String perms, Directory parent) {
        super(Type.DIRECTORY, owner, group, name, perms);
        this.parent = parent;
    }
    public Directory(User owner, Group group, String name, String perms) {
        super(Type.DIRECTORY, owner, group, name, perms);
        this.parent = null;
    }

    public void addEntry(User user, File file){
        if (this.getPermissions().canWrite(user)){
            content.put(file.getInode(),file);
        }else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }


    public void delEntry(User user, int inode){
        if (this.getPermissions().canWrite(user)){
            content.remove(inode);
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }

    public File getEntry(User user, int inode){
        if (this.getPermissions().canRead(user)){
            return content.get(inode);
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }
    public Map<Integer, File> getEntries(User user){
        if (this.getPermissions().canRead(user)){
            return content;
        } else {
            throw new SecurityException(user.getUsername() + " doesn't have the permissions to do this.");
        }
    }

    public int inodeFromName(String dirName){
        for (File file : content.values()){
            if (file != null && file.getName().equals(dirName)){
                return file.getInode();
            }
        }
        return -1;
    }

    public Directory getParent() {
        return parent;
    }

    public int getSize(){
        int size = 0;
        for (File file : content.values()){
            if (file instanceof RegularFile reg){
                size += reg.getSize();
            } else if (file instanceof Directory dir) {
                size += dir.getSize();
            }
        }
        return size;
    }
}
