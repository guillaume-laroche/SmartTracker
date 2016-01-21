package fr.imelo.tracker.net;

/**
 * Created by gl on 14/04/2015.
 */
public class NetException extends Exception {

    public NetException() {}

    public NetException(String message) {
        super(message);
    }

    public NetException(Throwable cause) {
        super(cause);
    }

    public NetException(String message, Throwable cause) {
        super(message, cause);
    }

}
