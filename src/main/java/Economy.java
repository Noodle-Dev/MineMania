import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Random;
import javax.swing.Timer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Economy {
    private static Economy instance;
    private HashMap<String, Double> buyPrices;
    private HashMap<String, Double> sellPrices;
    private Random random;
    private Timer priceTimer;
    private List<String> mineralNames;
    private PriceHistory priceHistory;
    private static final Gson gson = new Gson();

    private Economy() {
        this.random = new Random();
        this.priceHistory = new PriceHistory();
        initializePrices();
        startPriceUpdates();
    }

    public static Economy getInstance() {
        if (instance == null) {
            instance = new Economy();
        }
        return instance;
    }

    public List<String> getMineralNames() {
        return mineralNames;
    }

    private void initializePrices() {
        buyPrices = new HashMap<>();
        sellPrices = new HashMap<>();
        mineralNames = new ArrayList<>();

        addMineralsFromAI(5);

        if (buyPrices.isEmpty()) {
            addDefaultMineralWithHistory();
        }

        for (String mineral : buyPrices.keySet()) {
            sellPrices.put(mineral, buyPrices.get(mineral) * 0.7);
        }
    }

    private void startPriceUpdates() {
        priceTimer = new Timer(5000, e -> updatePrices());
        priceTimer.start();
    }

    public void forcePriceUpdate() {
        updatePrices();
    }

    public void buyMineral(String mineralName, int amount) {
        double price = buyPrices.get(mineralName);
    }

    public void sellMineral(String mineralName) {
        double price = sellPrices.get(mineralName);
    }

    public double getBuyPrice(String mineralName) {
        if (buyPrices.containsKey(mineralName)) {
            return buyPrices.get(mineralName);
        }
        return 0.0;
    }

    public double getSellPrice(String mineralName) {
        if (sellPrices.containsKey(mineralName)) {
            return sellPrices.get(mineralName);
        }
        return 0.0;
    }

    private void updatePrices() {
        long currentTime = System.currentTimeMillis();

        for (String mineral : new ArrayList<>(buyPrices.keySet())) {
            double currentBuyPrice = buyPrices.get(mineral);
            double fluctuation = (random.nextDouble() * 0.4) - 0.2;
            double newBuyPrice = currentBuyPrice * (1 + fluctuation);

            newBuyPrice = Math.max(5.0, Math.min(5000.0, newBuyPrice));

            buyPrices.put(mineral, Math.round(newBuyPrice * 100.0) / 100.0);

            double newSellPrice = newBuyPrice * 0.7;
            sellPrices.put(mineral, Math.round(newSellPrice * 100.0) / 100.0);

            priceHistory.addPricePoint(mineral, newBuyPrice, currentTime);
        }
    }

    public void reset() {
        if (priceTimer != null) {
            priceTimer.stop();
        }
        instance = null;
    }

    public void addMineralsFromAI(int count) {
        try {
            List<MineralData> newMinerals = generateMineralsFromGroq(count);
            long currentTime = System.currentTimeMillis();

            if (!newMinerals.isEmpty()) {
                for (MineralData mineral : newMinerals) {
                    String name = mineral.getName().trim();
                    double initialPrice = mineral.getPrice();

                    initialPrice = Math.max(10.0, Math.min(5000.0, initialPrice));

                    if (!mineralNames.contains(name)) {
                        mineralNames.add(name);
                        buyPrices.put(name, Math.round(initialPrice * 100.0) / 100.0);
                        sellPrices.put(name, Math.round((initialPrice * 0.7) * 100.0) / 100.0);

                        generateInitialPriceHistory(name, initialPrice, currentTime);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en Groq API: " + e.getMessage());
            addDefaultMineralWithHistory();
        }
    }

    private void generateInitialPriceHistory(String mineralName, double initialPrice, long baseTime) {
        Random rand = new Random();

        for (int i = 0; i < 15; i++) {
            long timestamp = baseTime - (15 - i) * 3000;
            double fluctuation = (rand.nextDouble() * 0.3) - 0.15;
            double historicalPrice = initialPrice * (1 + fluctuation);
            historicalPrice = Math.max(initialPrice * 0.5, Math.min(initialPrice * 1.5, historicalPrice));

            priceHistory.addPricePoint(mineralName, historicalPrice, timestamp);
        }

        priceHistory.addPricePoint(mineralName, initialPrice, baseTime);
    }

    private void addDefaultMineralWithHistory() {
        String defaultMineral = "DefaultOre";
        double defaultPrice = 50.0;
        long currentTime = System.currentTimeMillis();

        if (!mineralNames.contains(defaultMineral)) {
            mineralNames.add(defaultMineral);
            buyPrices.put(defaultMineral, defaultPrice);
            sellPrices.put(defaultMineral, defaultPrice * 0.7);
            generateInitialPriceHistory(defaultMineral, defaultPrice, currentTime);
        }
    }

    public List<PriceHistory.PricePoint> getPriceHistory(String mineralName) {
        return priceHistory.getPriceHistory(mineralName);
    }

    private List<MineralData> generateMineralsFromGroq(int numMinerals) throws Exception {
        String API_KEY = ConfigLoader.getGroqApiKey();
        String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
        String MODEL_NAME = "llama-3.1-8b-instant";

        URL url = new URL(GROQ_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        String systemPrompt = "Eres una IA que genera nombres de minerales que inicien en rigo y sus precios iniciales de mercado no deben ser mayores que 100. Responde ÚNICAMENTE con un array JSON de objetos.";

        String userPrompt = "Genera " + numMinerals + " nuevos nombres de minerales para un juego de minería espacial, los minerales deben empezar en rigo, y asigna a cada uno un precio inicial aleatorio entre 10.0 y 1000.0. El formato JSON DEBE ser: {\"minerals\": [{\"name\": \"NombreMineral\", \"price\": 123.45}, {\"name\": \"OtroMineral\", \"price\": 45.67}, ...]}";

        GroqRequest request = new GroqRequest();
        request.model = MODEL_NAME;
        request.temperature = 0.8;
        request.response_format = new ResponseFormat("json_object");

        List<RequestMessage> messages = new ArrayList<>();
        messages.add(new RequestMessage("system", systemPrompt));
        messages.add(new RequestMessage("user", userPrompt));
        request.messages = messages;

        String jsonInputString = gson.toJson(request);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                GroqResponse groqResponse = gson.fromJson(response.toString(), GroqResponse.class);
                String jsonContent = groqResponse.choices.get(0).message.content;

                MineralListWrapper wrapper = gson.fromJson(jsonContent, MineralListWrapper.class);

                if (wrapper != null && wrapper.minerals != null) {
                    return wrapper.minerals;
                } else {
                    System.err.println("El modelo de Groq no devolvió el array 'minerals'. Contenido recibido: " + jsonContent);
                    return new ArrayList<>();
                }
            }
        } else {
            System.err.println("Groq API error. Response code: " + responseCode);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                br.lines().forEach(System.err::println);
            }
            return new ArrayList<>();
        }
    }

    public static class MineralData {
        private String name;
        private double price;

        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    private static class MineralListWrapper {
        List<MineralData> minerals;
    }

    private static class GroqResponse {
        List<Choice> choices;
    }

    private static class Choice {
        Message message;
    }

    private static class Message {
        String content;
    }

    private static class GroqRequest {
        String model;
        List<RequestMessage> messages;
        double temperature;
        ResponseFormat response_format;
    }

    private static class RequestMessage {
        String role;
        String content;

        public RequestMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ResponseFormat {
        String type;

        public ResponseFormat(String type) {
            this.type = type;
        }
    }
}