import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class UserStore {

    private static final String USER_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    private static final Type USER_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    public static boolean login(String email, String password) {
        Map<String, String> users = loadUsers();
        if (!users.containsKey(email)) {
            return false;
        }
        String storedHash = users.get(email);
        String providedHash = hashPassword(password);
        return storedHash.equals(providedHash);
    }

    public static boolean register(String email, String password) {
        Map<String, String> users = loadUsers();
        if (users.containsKey(email)) {
            return false;
        }

        String passwordHash = hashPassword(password);

        users.put(email, passwordHash);

        return saveUsers(users);
    }


    private static Map<String, String> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (Reader reader = new FileReader(file)) {
            Map<String, String> users = gson.fromJson(reader, USER_MAP_TYPE);
            if (users == null) {
                return new HashMap<>();
            }
            return users;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static boolean saveUsers(Map<String, String> users) {
        try (Writer writer = new FileWriter(USER_FILE)) {
            gson.toJson(users, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}