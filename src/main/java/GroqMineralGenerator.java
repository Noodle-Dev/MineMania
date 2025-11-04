import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Type;
import java.util.List;

public class GroqMineralGenerator {

    // **IMPORTANTE:** Usa la clave que tienes, por ejemplo:
    // private static final String API_KEY = "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";
    private static final String API_KEY = ConfigLoader.getGroqApiKey();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_NAME = "llama-3.1-8b-instant";
    private static final Gson gson = new Gson();

    /**
     * Clase interna para mapear un objeto mineral de la respuesta JSON de Groq.
     */
    public static class MineralData {
        private String name;
        private double price;

        public String getName() { return name; }
        public double getPrice() { return price; }

        @Override
        public String toString() {
            return name + " ($" + price + ")";
        }
    }

    /**
     * Llama a la API de Groq para generar nuevos minerales y precios.
     * @param numMinerals El número de minerales a generar.
     * @return Una lista de objetos MineralData.
     */
    public List<MineralData> generateNewMinerals(int numMinerals) {
        try {
            URL url = new URL(GROQ_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);

            String systemPrompt = "Eres una IA que genera nombres de minerales que inicien en rigo y sus precios iniciales de mercado no deben ser mayores que 100. Responde ÚNICAMENTE con un array JSON de objetos.";
            String userPrompt = "Genera " + numMinerals + " nuevos nombres de minerales para un juego de minería espacial, los minerales deben empezar en rigo, y asigna a cada uno un precio inicial aleatorio entre 10.0 y 100.0. El formato JSON DEBE ser: [{\"name\": \"NombreMineral\", \"price\": 123.45}, {\"name\": \"OtroMineral\", \"price\": 45.67}, ...]";

            // Construcción del cuerpo de la solicitud JSON para Groq
            String jsonInputString = String.format("""
                {
                    "model": "%s",
                    "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                    ],
                    "temperature": 0.8,
                    "response_format": {"type": "json_object"}
                }
                """, MODEL_NAME, systemPrompt, userPrompt);

            // Enviar la solicitud
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Leer la respuesta
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // 1. Parsear la respuesta completa para obtener el contenido del mensaje
                    GroqResponse groqResponse = gson.fromJson(response.toString(), GroqResponse.class);
                    String jsonContent = groqResponse.choices.get(0).message.content;

                    // 2. Usar TypeToken para parsear el String JSON a una List<MineralData>
                    Type listType = new TypeToken<List<MineralData>>() {}.getType();
                    return gson.fromJson(jsonContent, listType);
                }
            } else {
                System.err.println("Groq API error. Response code: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    br.lines().forEach(System.err::println);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al comunicarse con la API de Groq: " + e.getMessage());
            e.printStackTrace();
        }
        return new java.util.ArrayList<>();
    }

    // --- Clases Auxiliares para el Parséo de la Respuesta de Groq ---

    // Nivel más alto de la respuesta de la API
    private static class GroqResponse {
        List<Choice> choices;
    }

    // Nivel intermedio: la elección del modelo
    private static class Choice {
        Message message;
    }

    // Nivel más bajo: el mensaje que contiene el JSON del mineral
    private static class Message {
        String content;
    }
}