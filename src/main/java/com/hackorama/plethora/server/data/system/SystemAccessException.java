package com.hackorama.plethora.server.data.system;

/**
 * Wrapper for external system access library exceptions
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
@SuppressWarnings("serial")
public class SystemAccessException extends Exception {

    private Long pid = null;

    public SystemAccessException(Throwable cuase) {
        super(cuase);
    }

    public SystemAccessException(long pid, Throwable cause) {
        super(cause);
        this.pid = pid;
    }

    public SystemAccessException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        if (pid != null) {
            return "Process id " + pid + " : " + super.getMessage();
        }
        return super.getMessage();
    }

}
