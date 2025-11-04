import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class UserDataService {

    private static final String DATA_DIRECTORY = "user_data";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de datos del usuario.");
            e.printStackTrace();
        }
    }

    private static String getFilePath(String userEmail) {
        String fileName = userEmail.replaceAll("[^a-zA-Z0-9.-_]", "_") + ".json";
        return Paths.get(DATA_DIRECTORY, fileName).toString();
    }

    public static void saveUserState(UserState state) {
        String filePath = getFilePath(state.getUserEmail());
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(state, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar el estado del usuario: " + state.getUserEmail());
            e.printStackTrace();
        }
    }

    public static UserState loadUserState(String userEmail) {
        String filePath = getFilePath(userEmail);
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("No se encontró estado guardado para " + userEmail + ". Creando uno nuevo.");
            UserState newState = new UserState(userEmail);
            saveUserState(newState);
            return newState;
        }

        try (Reader reader = new FileReader(filePath)) {
            UserState state = gson.fromJson(reader, UserState.class);

            state.setUserEmail(userEmail);

            if (state.getInventory() == null) {
                state.setInventory(new HashMap<>());
            }
            return state;
        } catch (IOException e) {
            System.err.println("Error al cargar el estado del usuario: " + userEmail);
            e.printStackTrace();
            return new UserState(userEmail);
        }
    }
}