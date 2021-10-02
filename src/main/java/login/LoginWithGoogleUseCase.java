package login;

import domain.entities.UserEntity;
import domain.entities.UserStatus;
import domain.ports.errors.ConnectionException;
import domain.ports.google_api.GoogleGetUserEndpoint;
import domain.ports.google_api.GooglePojo;
import domain.ports.testonly.testonly.UserRepository;
import login_convention.EmailValidator;

public class LoginWithGoogleUseCase {
    public static class Result {
        public static class Success extends Result {
            public UserEntity user;

            public Success(UserEntity user) {
                this.user = user;
            }
        }
        public static class NotAllowed extends Result {
        }
        public static class AuthError extends Result {
        }
        public static class GeneralError extends Result {
        }
    }

    private final GoogleGetUserEndpoint loginApi;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;

    public LoginWithGoogleUseCase(GoogleGetUserEndpoint loginApi, UserRepository userRepository, EmailValidator emailValidator) {
        this.loginApi = loginApi;
        this.userRepository = userRepository;
        this.emailValidator = emailValidator;
    }

    public Result executes(String googleAccessToken) {
        try {
            GoogleGetUserEndpoint.Result getUserResult = loginApi.getUser(googleAccessToken);

            if (getUserResult instanceof GoogleGetUserEndpoint.Result.AuthError) {
                return new Result.AuthError();
            }

            if (getUserResult instanceof GoogleGetUserEndpoint.Result.GeneralError) {
                return new Result.GeneralError();
            }
            GooglePojo pojo = ((GoogleGetUserEndpoint.Result.Success) getUserResult).googlePojo;
            String fullName = pojo.getFullName();
            String email = pojo.getEmail();

            UserEntity user = userRepository.getUserByEmail(pojo.getEmail());
            if (user != null) {
                return new Result.Success(user);
            }

            EmailValidator.Result validatorResult = emailValidator.check(email);
            if (!validatorResult.isValid) {
                return new Result.NotAllowed();
            }
            user = userRepository.addUser(email, fullName, pojo.getPicture(), validatorResult.role, new UserStatus(UserStatus.STATUS.ACTIVE));
            return new Result.Success(user);
        } catch (ConnectionException ex) {
            return new Result.GeneralError();
        }
    }
}