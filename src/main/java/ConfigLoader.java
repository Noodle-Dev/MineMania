import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("ADVERTENCIA: No se pudo cargar config.properties. Usando clave vacía.");
            System.err.println("Asegúrate de que el archivo config.properties existe en la raíz del proyecto.");
        }
    }

    public static String getGroqApiKey() {
        return properties.getProperty("GROQ_API_KEY", "");
    }
}