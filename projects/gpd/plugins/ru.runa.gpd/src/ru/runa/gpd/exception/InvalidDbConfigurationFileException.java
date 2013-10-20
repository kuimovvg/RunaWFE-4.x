package ru.runa.gpd.exception;

public class InvalidDbConfigurationFileException extends RuntimeException {
    private static final long serialVersionUID = -2900918880853265779L;
    
   public InvalidDbConfigurationFileException(){
       super();
   }
   
   public InvalidDbConfigurationFileException(Throwable cause){
       super(cause);
   }

}
