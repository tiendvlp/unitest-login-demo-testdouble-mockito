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

public class LoginWithGoogleUseCaseTestSolution {
    //region INITIALIZE
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
    //endregion

    private LoginWithGoogleUseCase SUT;
    //region init test double
    private GoogleGetUserEndpointTdImp googleGetUserEndpointTd;
    private UserRepositoryTdImp userRepository;
    private EmailValidatorTdImp emailValidator;
    //endregion

    @BeforeMethod(alwaysRun = true)
    public void setup () {
        //region init test double
        googleGetUserEndpointTd = new GoogleGetUserEndpointTdImp();
        userRepository = new UserRepositoryTdImp();
        emailValidator = new EmailValidatorTdImp();
        //endregion
        SUT = new LoginWithGoogleUseCase(googleGetUserEndpointTd, userRepository, emailValidator);
    }

    // create dump class
    //region implement test double
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
    //endregion

    //region unit test

    //region 1.ACCESS TOKEN IS PASSED TO GOOGLE ENDPOINT
    @Test
    public void loginWithGoogle_accessTokenPassedToEndPoint () {
        SUT.executes(ACCESS_TOKEN);
        assertEquals(googleGetUserEndpointTd.accessToken, ACCESS_TOKEN);
    }
    //endregion

    //region 2.GOOGLE POJO IS PASSED CORRECTLY TO REPOSITORY
    @Test
    public void loginWithGoogle_googlePojoIsPassedCorrectlyToRepository () {
        userRepository.isUserNotfound = true; // chưa đăng nhập vào ứng dụng
        emailValidator.isFptEmail = true; // là thành viên của fpt
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user.getFullName(), GOOGLE_POJO.getFullName());
        assertEquals(userRepository.user.getEmail(), GOOGLE_POJO.getEmail());
    }
    //endregion

    //region 3.IF ACCESS TOKEN IS CORRECT THEN SUCCESS RESULT RETURNED
    @Test ()
    public void loginWithGoogle_correctAccessToken_successReturned () {
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
    }
    //endregion

    //region 4.ACCESS TOKEN NOT FPT AND NOT IN DB, THEN NOT ALLOWED RETURNED
    @Test
    public void loginWithGoogle_accessTokenNotFptAndNotInDb_notAllowReturned () {
        userRepository.isUserNotfound = true;
        emailValidator.isFptEmail = false;
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof NotAllowed);
    }
    //endregion

    //region 5.ACCESS TOKEN NOT FPT BUT IN DB, THEN SUCCESS RETURNED
    @Test
    public void loginWithGoogle_accessTokenNotFptButAdmin_successReturned () {
        emailValidator.isFptEmail = false; // không phải email của fpt
        userRepository.isUserNotfound = false; // nhưng có tồn tại trong database
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
    }
    //endregion

    //region 6.ACCESS TOKEN IS NOT FPT AND NOT IN DB, THEN THERE IS NO INTERACTION WITH USER REPOSITORY ADD METHOD
    @Test
    public void loginWithGoogle_accessTokenIsNotFptAndNotInDb_noInteractionWithUserRepositoryAddMethod () {
        emailValidator.isFptEmail = false;
        userRepository.isUserNotfound = false;
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user, NON_INITIALIZE_USER);
    }
    //endregion

    //region 7.ACCESS TOKEN IS FPT BUT NOT IN DB, THEN HAS INTERACTION WITH USER REPOSITORY ADD METHOD
    @Test(groups = {HAS_INTERACTION_WITH_USER_REPOSITORY})
    public void loginWithGoogle_accessTokenIsFptButNotInDb_hasInteractionWithUserRepositoryAddMethod () {
        emailValidator.isFptEmail = true;
        userRepository.isUserNotfound = true;
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user, USER);
    }
    //endregion

    //region 8.ENDPOINT RETURN GENERAL ERROR THEN GENERAL ERROR RETURNED
    @Test
    public void loginWithGoogle_endPointGeneralError_generalErrorReturned () {
        googleGetUserEndpointTd.isGeneralError = true;
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);
    }
    //endregion

    //region 9.ENDPOINT RETURN AUTH ERROR THEN AUTH ERROR RETURNED
    @Test
    public void loginWithGoogle_endPointAuthError_authErrorReturned () {
        googleGetUserEndpointTd.isAuthError = true;
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof AuthError);
    }
    //endregion

    //region 10.ENDPOINT RETURN GENERAL ERROR, THEN THERE IS NO INTERACTION WITH REPOSITORY
    @Test
    public void loginWithGoogle_endPointGeneralError_noInteractionWithRepository () {
        googleGetUserEndpointTd.isGeneralError = true;
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user, NON_INITIALIZE_USER);
    }
    //endregion

    //region 11.ENDPOINT RETURN AUTHENTICATION ERROR, THEN THERE IS NO INTERACTION WITH REPOSITORY
    @Test
    public void loginWithGoogle_endPointAuthError_noInteractionWithRepository () {
        googleGetUserEndpointTd.isAuthError = true;
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user, NON_INITIALIZE_USER);
    }
    //endregion

    //region 12.ENDPOINT THROW CONNECTION EXCEPTION, THEN THERE IS NO INTERACTION WITH REPOSITORY
    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_noInteractionWithRepository () {
        googleGetUserEndpointTd.isConnectionError = true;
        SUT.executes(ACCESS_TOKEN);
        assertEquals(userRepository.user, NON_INITIALIZE_USER);
    }
    //endregion

    //region 13.ENDPOINT THROWS CONNECTION EXCEPTION THEN GENERAL ERROR RETURNED
    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_generalErrorReturned () {
        googleGetUserEndpointTd.isConnectionError = true;
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);
    }
    //endregion

    //region 14.USER REPOSITORY CONNECTION EXCEPTION OCCURS THEN GENERAL ERROR RETURNED
    @Test
    public void loginWithGoogle_userRepositoryConnectionExceptionOccurs_generalErrorReturned () {
        userRepository.isConnectionExceptionOccurs = true;
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);
    }
    //endregion

    //region 15.SUCCESS RESULT RETURN THE SAME USER THAT GET FROM REPOSITORY
    @Test
    public void loginWithGoogle_successReturned_successUserMatchWithRepositoryUserData () {
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
        assertEquals(((Success)result).user, USER);
    }
    //endregion

    //endregion

}