package vlive.sdk.core.crypto;

import android.util.Base64;

import java.util.Arrays;

/**
 * The type Encode decode base 64.
 */
public class EncodeDecodeBase64 {

    private EncodeDecodeBase64() {
        throw new UnsupportedOperationException(
                "Should not create instance of Util class. Please use as static..");
    }

    public static String decodedMessage(String message) {
        return Arrays.toString(Base64.decode(message, Base64.DEFAULT));
    }

    public static String encodedMessage(String message) {
        return Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
    }
}
