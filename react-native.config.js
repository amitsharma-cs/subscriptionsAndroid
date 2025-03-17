module.exports = {
    dependencies: {
      "react-native-subscriptions-android": {
        platforms: {
          android: {
            packageImportPath: "import com.dhanraj.RNSubscriptionsAndroidPackage;",
            packageInstance: "new RNSubscriptionsAndroidPackage()",
          },
        },
      },
    },
  };
  