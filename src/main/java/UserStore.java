import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static final String USER_FILE = "users.txt";

    public static boolean login(String email, String password) {
        Map<String, String> users = loadUsers();
        return users.containsKey(email) && users.get(email).equals(password);
    }

    public static boolean register(String email, String password) {
        Map<String, String> users = loadUsers();
        if (users.containsKey(email)) {
            return false;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE, true))) {
            pw.println(email + ":" + password);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}