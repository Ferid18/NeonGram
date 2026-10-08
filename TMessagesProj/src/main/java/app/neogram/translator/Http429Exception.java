package app.neogram.translator;

public class Http429Exception extends Exception {
    public Http429Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
