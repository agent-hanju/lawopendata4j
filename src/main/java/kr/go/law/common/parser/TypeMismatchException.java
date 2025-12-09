package kr.go.law.common.parser;

public class TypeMismatchException extends RuntimeException {
  public TypeMismatchException(String msg) {
    super(msg);
  }

  public TypeMismatchException(Throwable cause) {
    super(cause);
  }

  public TypeMismatchException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
