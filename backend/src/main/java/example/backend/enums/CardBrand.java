package example.backend.enums;

import static example.backend.utils.StringConstants.CARD_BRAND_UNSUPPORTED;

public enum CardBrand {

    VISA(new int[]{13, 16}) {
        @Override
        public boolean matches(String number) {
            return number.startsWith("4");
        }
    },

    MASTERCARD(new int[]{16}) {
        @Override
        public boolean matches(String number) {
            int prefix = Integer.parseInt(number.substring(0, 2));
            return prefix >= 51 && prefix <=55;
        }
    },

    AMEX(new int[]{15}) {
        @Override
        public boolean matches(String number) {
            return number.startsWith("34") || number.startsWith("37");
        }
    },

    DISCOVER(new int[]{16}) {
        @Override
        public boolean matches(String number) {
            return number.startsWith("6011") || number.startsWith("65");
        }
    };

    private final int[] validLengths;

    CardBrand(int[] validLengths) {
        this.validLengths = validLengths;
    }

    public abstract boolean matches(String number);

    public boolean isValidLength(String number) {
        for (int length : validLengths) {
            if (length == number.length()) return true;
        }
        return false;
    }

    public static CardBrand detect(String number) {
        for (CardBrand cardBrand : CardBrand.values()) {
            if (cardBrand.matches(number)) {
                return cardBrand;
            }
        }
        throw new IllegalArgumentException(CARD_BRAND_UNSUPPORTED);
    }
}
