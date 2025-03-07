package main.java.com.dhanraj;

import android.content.Context;
import androidx.annotation.NonNull;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import java.util.List;

public class BillingModule extends ReactContextBaseJavaModule {
    private BillingClient billingClient;

    public BillingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        billingClient = BillingClient.newBuilder(reactContext)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // Handle purchase updates
        }
    };

    @NonNull
    @Override
    public String getName() {
        return "BillingModule";
    }

    @ReactMethod
    public void initializeBillingClient(Promise promise) {
        if (billingClient != null) {
            promise.resolve("Billing Client Initialized");
        } else {
            promise.reject("INIT_ERROR", "Failed to initialize billing client");
        }
    }
}
