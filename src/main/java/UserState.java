import java.util.HashMap;
import java.util.Map;

public class UserState {

    private transient String userEmail;
    private double money;
    private Map<String, Integer> inventory;

    public UserState(String userEmail) {
        this.userEmail = userEmail;
        this.money = 100.0;
        this.inventory = new HashMap<>();
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public Map<String, Integer> getInventory() {
        if (this.inventory == null) {
            this.inventory = new HashMap<>();
        }
        return inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    public int getInventoryCount(String mineralName) {
        return getInventory().getOrDefault(mineralName, 0);
    }

    public void addToInventory(String mineralName, int amount) {
        int currentAmount = getInventoryCount(mineralName);
        getInventory().put(mineralName, currentAmount + amount);
    }

    public boolean removeFromInventory(String mineralName, int amount) {
        int currentAmount = getInventoryCount(mineralName);
        if (currentAmount >= amount) {
            getInventory().put(mineralName, currentAmount - amount);
            return true;
        }
        return false;
    }
}