package example.backend.utils;

public class StringConstants {
    public static final String USER_WITH_USERNAME_EMAIL_NUMBER_ALREADY_EXISTS = "User with that username or email or phone number already exists!";
    public static final String EMAIL_ALREADY_IN_USE = "Email already in use!";
    public static final String PHONE_NUMBER_ALREADY_IN_USE = "Phone number already in use!";
    public static final String CARD_BRAND_UNSUPPORTED = "Unsupported card brand!";
    public static final String CARD_NUMBER_MUST_CONTAIN_ONLY_DIGITS = "Card number must contain only digits!";
    public static final String NOT_ALLOWED_TO_DELETE_CARD = "You do not have permission to delete this card!";
    public static final String CARD_ALREADY_ADDED = "Card already added!";
    public static final String CANNOT_ACCESS_WALLET_YOU_ARE_NOT_OWNER_OF = "Oh, oh. Seems like you are trying to access someone else's wallet. Not possible!";
    public static final String CANNOT_DELETE_WALLET_YOU_ARE_NOT_OWNER_OF = "Oh, oh. Seems like you are trying to delete someone else's wallet. Not possible!";
    public static final String USER_BLOCKED = "You are blocked. Your actions in the app are limited. Please contact customer support to get that resolved!";
    public static final String ACCOUNT_NOT_VERIFIED = "Account is not verified. To proceed please verify your account!";
    public static final String WALLET_DUPLICATE = "You already own a %s wallet! You either did not specify currency and it sets the default to EUR and you own a EUR wallet or you simply have a wallet of the specified currency!";
    public static final String YOU_DO_NOT_OWN_THAT_TRANSACTION = "You can only view your own transactions!";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds!";
    public static final String SAME_WALLET_TRANSACTION_IMPOSSIBLE = "You cannot transfer from one wallet to the same one!";
    public static final String TRANSFER_AMOUNT_CANNOT_BE_NEGATIVE = "Transfer amount cannot be negative or zero!";
    public static final String WALLET_IDS_NOT_FOUND = "Wallet ids inconsistent, therefore not found!";
    public static final String EMAIL_MUST_BE_VALID = "Email must be a valid email address!";
    public static final String FIELD_CANNOT_BE_BLANK = "Field cannot be blank!";
    public static final String FIELD_MUST_BE_IN_RANGE = "Field must be between 1 and 50 characters!";
    public static final String PHONE_NUMBER_MUST_BE_VALID = "Phone number must be in international format (e.g. +359888123456)";
    public static final String PHOTO_URL_MUST_BE_VALID = "Photo URL must be a valid URL!";
    public static final String FAILED_TO_SEND_EMAIL = "Failed to send email. ";
    public static final String VERIFICATION_CODE_VALIDATION = "Verification code must be 6 digits!";
    public static final String USER_ALREADY_VERIFIED = "User already verified!";
    public static final String VERIFICATION_CODE_EXPIRED = "Verification code expired!";
    public static final String VERIFICATION_CODE_DOES_NOT_MATCH = "Verification code does not match!";
}
