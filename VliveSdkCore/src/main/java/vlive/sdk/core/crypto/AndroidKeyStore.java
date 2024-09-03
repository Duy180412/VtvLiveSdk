package vlive.sdk.core.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;

import vlive.sdk.core.config.BaseConfig;
import vlive.sdk.core.enumext.EnvironmentSdk;


public class AndroidKeyStore {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";


    public static void generateSecretKey() {
        String key;
        if (BaseConfig.environment == EnvironmentSdk.Development) {
            key = "UD4WPukv3D122MWQMddUrNe)pn697MYmhg^b00b204fght";
        } else {
            key = "UD4WPukv3D122MWQMddUrNe)pn697MYmhg^b00b204fght";
        }
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(
                            key,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build()
            );
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return keyStore.aliases().nextElement();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }
}
