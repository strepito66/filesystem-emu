package org.example;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZShell implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String STATE_FILE = "zshell_state.ser";

    private User loggedInUser;
    private FileSystem fs;
    private String defaultGroup;
    private Directory activeDir;

    public ZShell(String defaultGroup) {
        this.defaultGroup = defaultGroup;
        // Attempt to load state
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATE_FILE))) {
            ZShell loadedShell = (ZShell) ois.readObject();
            this.loggedInUser = loadedShell.loggedInUser;
            this.fs = loadedShell.fs;
            this.defaultGroup = loadedShell.defaultGroup;
            this.activeDir = loadedShell.activeDir;
            System.out.println("ZShell state loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing ZShell state found or failed to load. Creating new state.");
            fs = new FileSystem(defaultGroup);
        }
    }


    public void run() {
        try {
            BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            System.out.print("~ > ");
            while ((message = inReader.readLine()) != null) {
                String[] tokens = message.split(" ");
                String command = tokens[0];

                switch (command) {
                    case "help":
                        handleHelp();
                        break;
                    case "login":
                        handleLogin(tokens);
                        break;
                    case "signup":
                        handleSignup(tokens);
                        break;
                    case "grouplist":
                        handleGrouplist();
                        break;
                    case "exit":
                        handleExit();
                        return; // Exit the run loop
                    case "groupadd":
                        handleGroupadd(tokens);
                        break;
                    case "logout":
                        handleLogout();
                        break;
                    case "mkdir":
                        handleMkdir(tokens);
                        break;
                    case "cd":
                        handleCd(tokens);
                        break;
                    case "ls":
                        handleLs(tokens);
                        break;
                    case "touch":
                        handleTouch(tokens);
                        break;
                    case "read":
                        handleRead(tokens);
                        break;
                    case "write":
                        handleWrite(tokens);
                        break;
                    case "execute":
                        handleExecute(tokens);
                        break;
                    case "rm":
                        handleRm(tokens);
                        break;
                    case "chmod":
                        handleChmod(tokens);
                        break;
                    default:
                        message("command not found: " + command);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private boolean hasRequiredParams(String[] tokens, int requiredLength) {
        if (tokens.length < requiredLength) {
            message("not enough params");
            return false;
        }
        return true;
    }

    private File findEntryInActiveDir(String name) {
        int inode = activeDir.inodeFromName(name);
        if (inode != -1) {
            return activeDir.getEntry(loggedInUser, inode);
        } else {
            message("no such file or directory");
            return null;
        }
    }

    private void handleHelp() {
        message("- help\n- login <username> <password>\n- signup <username> <group> <password>\n- exit\n- grouplist\n##### Log In Required #####\n- groupadd <group>\n- logout\n- mkdir <name>\n- cd <dir>\n- ls (-l/-r)\n- touch <fileName>\n- read <fileName>\n- write <fileName> <string/bytes>\n- execute <fileName>\n- rm (-r) <fileName/dirName>\n- chmod <perms> <fileName>");
    }

    private boolean isLoggedIn() {
        if (loggedInUser == null) {
            message("login required!");
            return false;
        }
        return true;
    }

    private void handleLogin(String[] tokens) {
        if (loggedInUser != null) {
            message("already logged in");
            return;
        }
        if (!hasRequiredParams(tokens, 3)) return;
        String username = tokens[1];
        String password = tokens[2];
        List<User> users = fs.getUsers();
        User authenticatedUser = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        if (authenticatedUser == null) {
            message("user doesn't exist, sign up first!");
        } else {
            if (authenticatedUser.authenticate(password)) {
                loggedInUser = authenticatedUser;
                message("logged in!");
            } else {
                message("invalid password");
            }
        }
    }

    private void handleSignup(String[] tokens) {
        if (!hasRequiredParams(tokens, 4)) return;
        String username = tokens[1].replaceAll(",", "");
        String groupName = tokens[2];
        String password = tokens[3];
        List<User> users = fs.getUsers();
        User authenticatedUser = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        Group userGroup = fs.getGroups().stream()
                .filter(group -> group.getName().equals(groupName))
                .findFirst()
                .orElse(null);
        if (!(authenticatedUser == null)) {
            message("user exists, log in");
        } else {
            if (groupName.equals(defaultGroup) && !fs.getGroups().getFirst().getMembers().isEmpty()) {
                message("can't sign up on the default group");
            } else if (userGroup != null) {
                User usr = User.signup(username, userGroup, password);
                fs.addUser(usr);
                if (fs.getUsers().size() == 1) {
                    fs.genStructure(usr, usr.getGroup());
                    activeDir = fs.getRoot();
                }
                message("signed up successfully! You can now login");
            } else {
                message("non existent group");
            }
        }
    }

    private void handleGrouplist() {
        StringBuilder grouplist = new StringBuilder();
        for (Group group : fs.getGroups()) {
            grouplist.append(group.getName()).append("\n");
        }
        message(grouplist.toString());
    }

    private void handleExit() {
        // Save state before exiting
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATE_FILE))) {
            oos.writeObject(this);
            System.out.println("ZShell state saved successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save ZShell state: " + e.getMessage());
        }
    }

    private void handleGroupadd(String[] tokens) {
        if (!isLoggedIn()) return;

        if (!hasRequiredParams(tokens, 2)) return;
        String groupName = tokens[1];
        Group group = new Group(groupName);
        if (fs.getGroups().contains(group)) {
            message("group already exists");
            return;
        }

        fs.addGroup(group);
        message(group.getName() + " successfully created!");
    }

    private void handleLogout() {
        if (!isLoggedIn()) return;
        loggedInUser = null;

        while (activeDir.getParent() != null) {
            activeDir = activeDir.getParent();
        }
        message("logged out successfully");
    }

    private void handleMkdir(String[] tokens) {
        if (!isLoggedIn()) return;

        if (!hasRequiredParams(tokens, 2)) return;
        String dirName = tokens[1];
        if (activeDir.inodeFromName(dirName) == -1) {
            activeDir.addEntry(loggedInUser, new Directory(loggedInUser, loggedInUser.getGroup(), dirName, "755", activeDir));
            message("");
        } else {
            message("file " + dirName+ " already exists");
        }
    }

    private void handleCd(String[] tokens) {
        if (!isLoggedIn()) return;
        if (tokens.length == 1) {
            while (activeDir.getParent() != null) {
                activeDir = activeDir.getParent();
            }
            message("");
            return;
        }

        String dirName = tokens[1];
        if (dirName.equals("..")) {
            if (activeDir.getParent() != null) {
                activeDir = activeDir.getParent();
                message("");
            } else {
                message("no parent directory");
            }
        } else {
            File target = findEntryInActiveDir(dirName);
            if (target != null) {
                if (target.getType().equals(File.Type.DIRECTORY)) {
                    activeDir = (Directory) target;
                    message("");
                } else {
                    message("not a directory");
                }
            }
        }
    }

    private void handleLs(String[] tokens) {
        if (!isLoggedIn()) return;
        String dirs = "";

        if (tokens.length == 2){
            if (tokens[1].equals("-r")){
                int end = activeDir.getEntries(loggedInUser).values().size();
                File[] files = new File[end];
                for (File file : activeDir.getEntries(loggedInUser).values()){
                    end--;
                    files[end] = file;
                }

                for (File file : files) {
                    dirs += file.getName() + "\n";
                }
            } else if (tokens[1].equals("-l")) {
                for (File file : activeDir.getEntries(loggedInUser).values()) {
                    char typeChar = (file.getType() == File.Type.DIRECTORY) ? 'd' : '-';
                    String perms = file.getPermissions().formatPermissions();
                    String owner = file.getOwner().getUsername();
                    String group = file.getGroup().getName();
                    int size;
                    if (file instanceof Directory) {
                        size = ((Directory) file).getSize();
                    } else if (file instanceof RegularFile) {
                        size = ((RegularFile) file).getSize();
                    } else {
                        size = 0;
                    }
                    String name = file.getName();
                    dirs += String.format("%c%s %s %s %d %s\n",
                            typeChar, perms, owner, group, size, name);
                }
            } else if (tokens[1].equals("-R")) {
                dirs = listRecursive(activeDir);
            }else {
                message("not a valid argument: " + tokens[1]);
                return;
            }
        }else{
            for (File file : activeDir.getEntries(loggedInUser).values()) {
                dirs += file.getName() + "\n";
            }
        }

        dirs = dirs.replaceAll("[\n\r]$", "");
        message(dirs);
    }

    private void handleTouch(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 2)) return;
        String fileName = tokens[1];
        if (activeDir.inodeFromName(fileName) == -1) {
            activeDir.addEntry(loggedInUser, new RegularFile(loggedInUser, loggedInUser.getGroup(), fileName, "755"));
            message("");
        } else {
            message("file " + fileName+ " already exists");
        }
    }

    private void handleWrite(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 3)) return;
        String fileName = tokens[1];
        String arg = tokens[2];
        File target = findEntryInActiveDir(fileName);
        if (target != null) {
            if (target instanceof RegularFile) {
                Charset charset = StandardCharsets.UTF_8;
                byte[] byteArray = charset.encode(arg).array();
                ArrayList<Byte> toWrite = new ArrayList<>();
                for (byte b : byteArray) {
                    toWrite.add(b);
                }

                ((RegularFile) target).write(loggedInUser, toWrite);
                message("");
            } else {
                message("not a file");
            }
        }
    }

    private void handleRead(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 2)) return;
        String fileName = tokens[1];
        File target = findEntryInActiveDir(fileName);
        if (target != null) {
            if (target instanceof RegularFile) {
                message(((RegularFile) target).read(loggedInUser).toString());
            } else {
                message("not a file");
            }
        }
    }

    private void handleExecute(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 2)) return;
        String fileName = tokens[1];
        File target = findEntryInActiveDir(fileName);
        if (target != null) {
            if (target instanceof RegularFile) {
                message(((RegularFile) target).execute(loggedInUser));
            } else {
                message("not a file");
            }
        }
    }

    private void handleRm(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 2)) return;

        String arg1 = tokens[1];
        if (tokens.length > 2){
            String arg2 = tokens[2];
            if (arg1.equals("-r")){
                File target = findEntryInActiveDir(arg2);
                if (target != null) {
                    if (target instanceof Directory) {
                        activeDir.delEntry(loggedInUser, activeDir.inodeFromName(arg2));
                        message("");
                    } else {
                        message("not a directory, don't use -r");
                    }
                }
            }
        }else {
            File target = findEntryInActiveDir(arg1);
            if (target != null) {
                if (target instanceof RegularFile) {
                    activeDir.delEntry(loggedInUser, activeDir.inodeFromName(arg1));
                    message("");
                } else {
                    message("not a file, use -r");
                }
            }
        }
    }

    private void handleChmod(String[] tokens) {
        if (!isLoggedIn()) return;
        if (!hasRequiredParams(tokens, 3)) return;
        String perms = tokens[1];
        String fileName = tokens[2];

        File target = findEntryInActiveDir(fileName);
        if (target != null) {
            if (perms.matches("^[0-7]{3}$")){
                target.editPerms(loggedInUser,perms);
                message("");
            } else {
                message("invalid file mode: " + perms);
            }
        }
    }


    private void message(String msg) {
        if (loggedInUser != null) {

            ArrayList<File> parents = new ArrayList<>();
            Directory copy = activeDir;
            while (copy.getParent() != null) {
                parents.add(copy);
                copy = copy.getParent();
            }
            parents.add(copy);
            Collections.reverse(parents);

            String activePath = "/";
            for (int i = 1; i < parents.size(); i++) { // Start from 1 to skip root "/"
                activePath += "/" + parents.get(i).getName();
            }

            if (activePath.startsWith("//")){
                activePath = activePath.substring(1);
            }

            if (msg.isEmpty()){
                System.out.print(loggedInUser.getUsername() + "@" + loggedInUser.getGroup().getName() + " " + activePath + " > ");
            }else {
                System.out.println(msg);
                System.out.print(loggedInUser.getUsername() + "@" + loggedInUser.getGroup().getName() + " " + activePath + " > ");
            }
        } else {
            System.out.println(msg);
            System.out.print("~ > ");
        }
    }

    private String listRecursive(Directory dir) {
        StringBuilder out = new StringBuilder();
        listRecursive(dir, out, 0);
        return out.toString();
    }

    private void listRecursive(Directory dir, StringBuilder out, int indent) {
        indent(out, indent);
        out.append(dir.getName()).append("/:").append(System.lineSeparator());

        for (File file : dir.getEntries(loggedInUser).values()) {
            if (file instanceof Directory) {
                listRecursive((Directory) file, out, indent + 1);
            } else {
                indent(out, indent + 1);
                out.append(file.getName()).append(System.lineSeparator());
            }
        }
    }

    private void indent(StringBuilder out, int level) {
        out.append("    ".repeat(Math.max(0, level)));
    }
}

//TODO: commands with paths
