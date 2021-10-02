package domain.ports.google_api;


import domain.ports.errors.ConnectionException;

public interface GoogleGetUserEndpoint {
     class Result {
          public static class Success extends Result {
               public GooglePojo googlePojo;
               public Success(GooglePojo googlePojo) {
                    this.googlePojo = googlePojo;
               }
          }

          public static class AuthError extends Result {

          }

          public static class GeneralError extends Result {

          }
     }
     Result getUser(String accessToken) throws ConnectionException;
}