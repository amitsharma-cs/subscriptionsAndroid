package com.dhanraj;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.billingclient.api.*;

import com.facebook.react.bridge.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RNSubscriptionsAndroidModule extends ReactContextBaseJavaModule {
    private static final String TAG = "BillingModule";
    private BillingClient billingClient;
    private Promise purchasePromise;

    public RNSubscriptionsAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "BillingModule";
    }

    // Expose initializeBillingClient as a React Method
    @ReactMethod
    public void initializeBillingClient(Promise promise) {
        if (billingClient != null && billingClient.isReady()) {
            promise.resolve(true);
            return;
        }

        billingClient = BillingClient.newBuilder(getReactApplicationContext())
                .setListener((billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        handlePurchase(purchases);
                    } else {
                        Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
                    }
                })
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client is ready.");
                    promise.resolve(true);
                } else {
                    promise.reject("BILLING_ERROR", "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e(TAG, "Billing Service Disconnected.");
            }
        });
    }

    @ReactMethod
    public void fetchProducts(ReadableArray productIds, Promise promise) {
        if (billingClient == null || !billingClient.isReady()) {
            promise.reject("ERROR", "Billing Client is not initialized.");
            return;
        }

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (Object id : productIds.toArrayList()) {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId((String) id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                WritableArray results = Arguments.createArray();
                for (ProductDetails details : productDetailsList) {
                    WritableMap item = Arguments.createMap();
                    item.putString("productId", details.getProductId());
                    item.putString("title", details.getTitle());
                    item.putString("price", details.getOneTimePurchaseOfferDetails() != null ?
                            details.getOneTimePurchaseOfferDetails().getFormattedPrice() : "N/A");
                    results.pushMap(item);
                }
                promise.resolve(results);
            } else {
                promise.reject("ERROR", "Failed to fetch products.");
            }
        });
    }

    @ReactMethod
    public void purchaseSubscription(String productId, Promise promise) {
        if (billingClient == null || !billingClient.isReady()) {
            promise.reject("ERROR", "Billing Client is not initialized.");
            return;
        }

        Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("ERROR", "Activity is null.");
            return;
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Arrays.asList(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty()) {
                ProductDetails productDetails = productDetailsList.get(0);
                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                productDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build());

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
                Log.d(TAG, "Billing Flow Response Code: " + responseCode);

                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    purchasePromise = promise;
                } else {
                    promise.reject("ERROR", "Billing flow failed.");
                }
            } else {
                promise.reject("ERROR", "Product not found.");
            }
        });
    }

    private void handlePurchase(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                WritableMap result = Arguments.createMap();
                result.putString("orderId", purchase.getOrderId());
                result.putString("purchaseToken", purchase.getPurchaseToken());
                if (purchasePromise != null) {
                    purchasePromise.resolve(result);
                    purchasePromise = null;
                }
            }
        }
    }
}
