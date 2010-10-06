package tigase.jaxmpp.core.client.xml;

/**
 * Exception for XML errors.
 * @author Mads Randstoft
 */
public class XMLException extends Exception {

    /**
     * Creates a new instance of <code>XMLException</code> without detail message.
     */
    public XMLException() {
    }


    /**
     * Constructs an instance of <code>XMLException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public XMLException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>XMLException</code> with the specified cause.
     * @param cause the cause.
     */
    public XMLException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>XMLException</code> with the specified detail message and cause.
     * @param msg the detail message.
     * @param cause the cause.
     */
    public XMLException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
