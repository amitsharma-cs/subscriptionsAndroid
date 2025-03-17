
import { NativeModules } from 'react-native';

const { RNSubscriptionsAndroid } = NativeModules;

class InAppBilling {

    // static initBilling(products, callback) {
    //     return RNSubscriptionsAndroid.initBillingClient(products,callback);
    //   }

    //   static getProducts(callback) {
    //     return RNSubscriptionsAndroid.loadSubscriptionProducts(callback);
    //   }

    //   static subscribeTo(oldProductId = null,productId,prorationMode = 1, callback) {
    //     return RNSubscriptionsAndroid.subscribeTo(oldProductId,productId,prorationMode, callback);
    //   }

    //   static subscribeToPlan(oldProductId = null,productId,prorationMode = 1, callback) {
    //     return RNSubscriptionsAndroid.subscribeToPlan(oldProductId,productId,prorationMode, callback);
    //   }
    
    static initBilling() {
      return RNSubscriptionsAndroid.initializeBillingClient();
    }

    static getProducts(products) {
      return RNSubscriptionsAndroid.fetchProducts(products);
    }

    static subscribeTo(productId) {
      return RNSubscriptionsAndroid.purchaseSubscription(productId);
    } 

}

module.exports = InAppBilling;


// export default RNSubscriptionsAndroid;
