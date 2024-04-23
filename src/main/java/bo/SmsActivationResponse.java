package bo;

import service.smsActivation.SmsActivationService;

public class SmsActivationResponse {
    private Integer id;
    private String phone;
    private SmsActivationService smsActivationService;

    public SmsActivationService getSmsActivationService() {
        return smsActivationService;
    }

    public void setSmsActivationService(SmsActivationService smsActivationService) {
        this.smsActivationService = smsActivationService;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
