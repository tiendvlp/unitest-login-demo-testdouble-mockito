package login;

import domain.entities.UserEntity;
import domain.entities.UserRole;
import domain.entities.UserStatus;
import domain.ports.errors.ConnectionException;
import domain.ports.google_api.GoogleGetUserEndpoint;
import domain.ports.google_api.GooglePojo;
import domain.ports.testonly.testonly.UserRepository;
import login.LoginWithGoogleUseCase.Result;
import login.LoginWithGoogleUseCase.Result.*;
import login_convention.EmailValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static common.TestConfig.GROUP.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LoginWithGoogleUseCaseTestExercise {
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String EMAIL = "EMAIL";
    private static final String ID = "ID";
    private static final String FULL_NAME;
    private static final String GIVEN_NAME = "GIVEN_NAME";
    private static final String FAMILY_NAME = "FAMILY_NAME";
    private static final String NAME = "NAME";
    private static final UserStatus ACTIVE_STATUS = new UserStatus(UserStatus.STATUS.ACTIVE);
    private static final UserRole NOT_ADMIN_ROLE = new UserRole(UserRole.TYPE.STUDENT);
    private static final UserEntity NON_INITIALIZE_USER = null;
    private static final UserEntity USER;
    private static final GooglePojo GOOGLE_POJO;

    static {
        GOOGLE_POJO = new GooglePojo(ID,EMAIL, true,NAME,GIVEN_NAME, FAMILY_NAME, "","");
        FULL_NAME = FAMILY_NAME + " " + GIVEN_NAME;
        USER = new UserEntity(ID,EMAIL, FULL_NAME, NOT_ADMIN_ROLE, ACTIVE_STATUS);
    }

    private LoginWithGoogleUseCase SUT;
    private GoogleGetUserEndpointTdImp googleGetUserEndpointTd;
    private UserRepositoryTdImp userRepository;
    private EmailValidatorTdImp emailValidator;

    @BeforeMethod(alwaysRun = true)
    public void setup () {
        googleGetUserEndpointTd = new GoogleGetUserEndpointTdImp();
        userRepository = new UserRepositoryTdImp();
        emailValidator = new EmailValidatorTdImp();
        SUT = new LoginWithGoogleUseCase(googleGetUserEndpointTd, userRepository, emailValidator);
    }

    private static class GoogleGetUserEndpointTdImp implements GoogleGetUserEndpoint {
        public String accessToken;
        public boolean isGeneralError;
        public boolean isAuthError;
        public boolean isConnectionError;

        @Override
        public Result getUser(String accessToken) throws ConnectionException {
            if (isGeneralError) {
                return new Result.GeneralError();
            }
            if (isAuthError) {
                return new Result.AuthError();
            }
            if (isConnectionError) {
                throw new ConnectionException("");
            }
            this.accessToken = accessToken;
            return new Result.Success(GOOGLE_POJO);
        }
    }

    private static class UserRepositoryTdImp implements UserRepository {
        public boolean isConnectionExceptionOccurs = false;
        public boolean isUserNotfound = false;
        public UserEntity user = NON_INITIALIZE_USER;

        @Override
        public UserEntity getUserByEmail(String email) throws ConnectionException {
            if (isConnectionExceptionOccurs) {
                throw new ConnectionException("");
            }

            if (isUserNotfound) {
                return NON_INITIALIZE_USER;
            }

            return USER;
        }

        @Override
        public UserEntity addUser(String email, String fullName, String avatar, UserRole role, UserStatus userStatus) throws ConnectionException {
            if (isConnectionExceptionOccurs) {
                throw new ConnectionException("");
            }
            this.user = new UserEntity(ID, email, fullName, role, userStatus);
            return this.user;
        }
    }

    private static class EmailValidatorTdImp implements EmailValidator {
        public boolean isFptEmail = false;

        @Override
        public Result check(String email) {
            if (isFptEmail) {
                return new Result(NOT_ADMIN_ROLE, true);
            } else {
                return new Result(null, false);
            }
        }
    }

    // 1. ACCESS TOKEN IS PASSED TO GOOGLE ENDPOINT
    // 2. GOOGLE POJO IS PASSED CORRECTLY TO REPOSITORY
    // 3. IF ACCESS TOKEN IS CORRECT THEN SUCCESS RESULT RETURNED
    // 4. ACCESS TOKEN NOT FPT AND NOT IN DB, THEN NOT ALLOWED RETURNED
    // 5. ACCESS TOKEN NOT FPT BUT IN DB, THEN SUCCESS RETURNED
    // 6. ACCESS TOKEN IS NOT FPT AND NOT IN DB, THEN THERE IS NO INTERACTION WITH USER REPOSITORY ADD METHOD
    // 7. ACCESS TOKEN IS FPT BUT NOT IN DB, THEN HAS INTERACTION WITH USER REPOSITORY ADD METHOD
    // 8. ENDPOINT RETURN GENERAL ERROR THEN GENERAL ERROR RETURNED
    // 9. ENDPOINT RETURN AUTH ERROR THEN AUTH ERROR RETURNED
    // 10.ENDPOINT RETURN GENERAL ERROR, THEN THERE IS NO INTERACTION WITH REPOSITORY
    // 11. END POINT RETURN AUTHENTICATION ERROR, THEN THERE IS NO INTERACTION WITH USER REPOSITORY ADD METHOD
    // 12. ENDPOINT THROW CONNECTION EXCEPTION, THEN THERE IS NO INTERACTION WITH REPOSITORY
    // 13. ENDPOINT THROWS CONNECTION EXCEPTION THEN GENERAL ERROR RETURNED
    // 14. USER REPOSITORY CONNECTION EXCEPTION OCCURS THEN GENERAL ERROR RETURNED
    // 15. SUCCESS RESULT RETURN THE SAME USER THAT GET FORM REPOSITORY

}