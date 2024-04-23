package bo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriptionInfo {
    private ConcurrentMap<Integer, Date> accIds;
    private Date cooldown;

    public SubscriptionInfo(final Integer accId) {
        accIds = new ConcurrentHashMap<>();
        accIds.put(accId, new Date());
    }

    public ConcurrentMap<Integer, Date> getAccIds() {
        return accIds;
    }

    public void setAccIds(ConcurrentHashMap<Integer, Date> accIds) {
        this.accIds = accIds;
    }

    public Date getCooldown() {
        return cooldown;
    }

    public void setCooldown(Date cooldown) {
        this.cooldown = cooldown;
    }
    public void addAccId(final Integer accId, final String link) {
       cleanUpEntry();
       final Date now = new Date();
       accIds.put(accId, now);
       if (accIds.size() > 5) {
           System.out.println(now + " Subscription [" + link + "] on cooldown");
           now.setTime(now.getTime() + 3600000);
           cooldown = now;
       }
    }
    private void cleanUpEntry() {
        final Date now = new Date();
        for (ConcurrentMap.Entry<Integer, Date> entry : accIds.entrySet()) {
            if (entry.getValue().getTime() + 3600000 < now.getTime()) {
                accIds.remove(entry.getKey());
            }
        }
    }
    public boolean isOnCooldown() {
       return cooldown != null && cooldown.getTime() + 3600000 > new Date().getTime();
    }
}
