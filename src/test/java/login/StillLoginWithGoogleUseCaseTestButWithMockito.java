package login;

import domain.entities.UserEntity;
import domain.entities.UserRole;
import domain.entities.UserStatus;
import domain.ports.google_api.GoogleGetUserEndpoint;
import domain.ports.google_api.GooglePojo;
import login.LoginWithGoogleUseCase.Result;
import login.LoginWithGoogleUseCase.Result.AuthError;
import login.LoginWithGoogleUseCase.Result.GeneralError;
import login.LoginWithGoogleUseCase.Result.NotAllowed;
import login.LoginWithGoogleUseCase.Result.Success;
import login_convention.EmailValidator;
import domain.ports.errors.ConnectionException;
import domain.ports.testonly.testonly.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static common.TestConfig.GROUP.HAS_INTERACTION_WITH_USER_REPOSITORY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StillLoginWithGoogleUseCaseTestButWithMockito {
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
    private GoogleGetUserEndpoint googleGetUserEndpoint;
    private UserRepository userRepository;
    private EmailValidator emailValidator;

    private void endpointGeneralError() throws ConnectionException {
        when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenReturn(new GoogleGetUserEndpoint.Result.GeneralError());
    }

    private void endPointAuthError() throws ConnectionException {
        when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenReturn(new GoogleGetUserEndpoint.Result.AuthError());
    }

    private void endPointConnectionExceptionOccurs() throws ConnectionException {
        when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenThrow(new ConnectionException(""));
    }

    private void userRepositoryConnectionExceptionOccurs() throws ConnectionException {
        when (userRepository.getUserByEmail(EMAIL)).thenThrow(new ConnectionException(""));
    }

    @BeforeMethod(alwaysRun = true)
    public void setup () throws ConnectionException {
        googleGetUserEndpoint = mock(GoogleGetUserEndpoint.class);
        userRepository = mock(UserRepository.class);
        emailValidator = mock(EmailValidator.class);
        SUT = new LoginWithGoogleUseCase(googleGetUserEndpoint, userRepository,emailValidator);
        when(userRepository.getUserByEmail(EMAIL)).thenReturn(NON_INITIALIZE_USER);
        when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenReturn(new GoogleGetUserEndpoint.Result.Success(GOOGLE_POJO));
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE, true));
    }

    @Test
    public void loginWithGoogle_correctAccessToken_successReturned () throws ConnectionException {
        when(emailValidator.check(ACCESS_TOKEN)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE, true));
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
    }

    @Test
    public void loginWithGoogle_accessTokenPassedToEndPoint () throws ConnectionException {
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        SUT.executes(ACCESS_TOKEN);
        verify(googleGetUserEndpoint,times(1)).getUser(ac.capture());
        assertEquals(ac.getAllValues().get(0), ACCESS_TOKEN);
    }

    @Test
    public void loginWithGoogle_endPointGeneralError_generalErrorReturned () throws ConnectionException {
        endpointGeneralError();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);

    }

    @Test
    public void loginWithGoogle_endPointAuthError_authErrorReturned () throws ConnectionException {
        endPointAuthError();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof AuthError);
    }



    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_noInteractionWithRepository () throws ConnectionException {
        endPointConnectionExceptionOccurs();
        SUT.executes(ACCESS_TOKEN);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loginWithGoogle_endPointGeneralError_noInteractionWithRepository () throws ConnectionException {
        endpointGeneralError();
        SUT.executes(ACCESS_TOKEN);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loginWithGoogle_endPointAuthError_noInteractionWithRepository () throws ConnectionException {
        endPointAuthError();
        SUT.executes(ACCESS_TOKEN);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loginWithGoogle_userRepositoryConnectionExceptionOccurs_generalErrorReturned () throws ConnectionException {
        userRepositoryConnectionExceptionOccurs();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);
    }

    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_generalErrorReturned () throws ConnectionException {
        endPointConnectionExceptionOccurs();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof GeneralError);
    }

    @Test
    public void loginWithGoogle_accessTokenNotFptAndNotInDb_notAllowReturned () {
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE, false));
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof NotAllowed);
    }

    @Test
    public void loginWithGoogle_accessTokenNotFptButInDb_successReturned () throws ConnectionException {
        when(userRepository.addUser(anyString(),anyString(),anyString(),any(), any())).thenReturn(USER);
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE, true));
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
    }

    @Test(groups = {HAS_INTERACTION_WITH_USER_REPOSITORY})
    public void loginWithGoogle_accessTokenIsFptButNotInDb_hasInteractionWithUserRepositoryAddMethod () throws ConnectionException {
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE,true));
        when(userRepository.getUserByEmail(EMAIL)).thenReturn(NON_INITIALIZE_USER);
        SUT.executes(ACCESS_TOKEN);
        verify(userRepository, times(1)).addUser(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    public void loginWithGoogle_accessTokenIsNotFptAndNotInDb_noInteractionWithUserRepositoryAddMethod () throws ConnectionException {
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(null,false));
        when(userRepository.getUserByEmail(EMAIL)).thenReturn(NON_INITIALIZE_USER);
        SUT.executes(ACCESS_TOKEN);
        verify(userRepository, never()).addUser(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    public void loginWithGoogle_googlePojoIsPassedCorrectlyToRepository () throws ConnectionException {
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        SUT.executes(ACCESS_TOKEN);
        verify(userRepository,times(1)).addUser(ac.capture(), ac.capture(), anyString(), any(), any());
        assertEquals(ac.getAllValues().get(0), EMAIL);
        assertEquals(ac.getAllValues().get(1), FULL_NAME);
    }

    @Test
    public void loginWithGoogle_successReturned_successUserMatchWithRepositoryUserData () throws ConnectionException {
        when(userRepository.getUserByEmail(EMAIL)).thenReturn(USER);
        Result result = SUT.executes(ACCESS_TOKEN);
        assertTrue(result instanceof Success);
        assertEquals(((Success)result).user, USER);
    }
}