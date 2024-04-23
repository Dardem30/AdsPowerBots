package bo;

public class PayUpVideoAcc {
    private int id;
    private Float balance;
    private boolean blocked;
    private VkAccount vkAccount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public VkAccount getVkAccount() {
        return vkAccount;
    }

    public void setVkAccount(VkAccount vkAccount) {
        this.vkAccount = vkAccount;
    }
}
