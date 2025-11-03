import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest; // Para el hashing
import java.security.NoSuchAlgorithmException; // Para el hashing
import java.util.Base64; // Para codificar el hash

public class UserStore {
    private static final String USER_FILE = "users.txt";

    /**
     * Hashea una contraseña usando SHA-256 y la codifica en Base64.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            // Convertir el hash de bytes a un String legible (Base64)
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // Esto no debería pasar si SHA-256 está disponible
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    /**
     * Verifica el email y la contraseña contra el hash almacenado.
     */
    public static boolean login(String email, String password) {
        Map<String, String> users = loadUsers();
        if (!users.containsKey(email)) {
            return false; // El usuario no existe
        }

        String storedHash = users.get(email);
        String providedHash = hashPassword(password);

        // Compara el hash de la contraseña introducida con el hash guardado
        return storedHash.equals(providedHash);
    }

    /**
     * Registra un nuevo usuario guardando el hash de su contraseña.
     */
    public static boolean register(String email, String password) {
        Map<String, String> users = loadUsers();
        if (users.containsKey(email)) {
            return false; // El email ya está en uso
        }

        String passwordHash = hashPassword(password);

        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE, true))) {
            // Guarda el email y el HASH
            pw.println(email + ":" + passwordHash);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga los usuarios (email y hash) desde el archivo.
     */
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
                    // parts[0] = email, parts[1] = passwordHash
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}