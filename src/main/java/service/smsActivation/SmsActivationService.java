package service.smsActivation;

import bo.SmsActivationResponse;

public interface SmsActivationService {
    public boolean canPurchaseVkProduct() throws Exception;
    public SmsActivationResponse purchaseVkSmsVerification() throws Exception;
    public String retrieveLastResultCodeOfThePurchase(final Integer purchaseId, final int smsIndex) throws Exception;
    public void finishRequest(final Integer purchaseId) throws Exception;
    public void banRequest(final Integer purchaseId) throws Exception;
}
