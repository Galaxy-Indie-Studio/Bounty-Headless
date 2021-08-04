package me.bigtallahasee.headless.datastorage;

public class DataStorageException extends Exception{
    private static final long serialVersionUID = -3088304167385518610L;

    public DataStorageException(Throwable cause) {
        super(cause);
    }

    public DataStorageException(String cause) {
        super(cause);
    }
}

