//package service.smsActivation;
//
//import bo.SmsActivationResponse;
//
//public class ActivationManager {
//    private static final SmsActivationService fiveSimService = FiveSimService.getInstance();
//    private static final SmsActivationService smsActivate = SmsActivate.getInstance();
//    private static final SmsActivationService smsRegService = SmsRegService.getInstance();
//
//    public static SmsActivationResponse purchaseVkActivation() throws Exception {
//        while (true) {
//            Thread.sleep(25);
////            try {
////                //       if (fiveSimService.canPurchaseVkProduct()) {
////                SmsActivationResponse smsActivationResponse = fiveSimService.purchaseVkSmsVerification();
////                if (smsActivationResponse != null) {
////                    System.out.println("[FiveSimService] Purchased phone number: " + smsActivationResponse.getPhone());
////                    smsActivationResponse.setSmsActivationService(fiveSimService);
////                    return smsActivationResponse;
////                }
////                //     }
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//            try {
//                //  if (smsActivate.canPurchaseVkProduct()) {
//                SmsActivationResponse smsActivationResponse = smsActivate.purchaseVkSmsVerification();
//                if (smsActivationResponse != null) {
//                    System.out.println("[SmsActivate] Purchased phone number: " + smsActivationResponse.getPhone());
//                    smsActivationResponse.setSmsActivationService(smsActivate);
//                    return smsActivationResponse;
//                }
//                //   }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
////            if (smsRegService.canPurchaseVkProduct()) {
////                SmsActivationResponse smsActivationResponse = smsRegService.purchaseVkSmsVerification();
////                if (smsActivationResponse != null) {
////                    System.out.println("[SmsRegService] Purchased phone number: " + smsActivationResponse.getPhone());
////                    smsActivationResponse.setSmsActivationService(smsRegService);
////                    return smsActivationResponse;
////                }
////            }
//
//        }
//    }
//}
