import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Imports de Gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken; // Para el tipo de Map
import java.lang.reflect.Type;

public class UserStore {

    // 1. Cambiamos el nombre del archivo
    private static final String USER_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 2. Definimos el tipo de dato que Gson leerá: un Mapa de String a String
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

        // 3. Añade el nuevo usuario al Map
        users.put(email, passwordHash);

        // 4. Guarda el Map actualizado en el archivo JSON
        return saveUsers(users);
    }

    /**
     * MÉTODO ACTUALIZADO
     * Lee el archivo users.json y lo convierte en un Map.
     */
    private static Map<String, String> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (Reader reader = new FileReader(file)) {
            // Lee el JSON y conviértelo directamente a un Map
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

    /**
     * NUEVO MÉTODO
     * Guarda el Map de usuarios completo en el archivo JSON.
     * Sobrescribe el archivo cada vez.
     */
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