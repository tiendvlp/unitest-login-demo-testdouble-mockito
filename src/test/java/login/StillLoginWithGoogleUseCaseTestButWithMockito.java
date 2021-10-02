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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    private static final UserRole ADMIN_ROLE = new UserRole(UserRole.TYPE.ADMIN);
    private static final UserEntity NON_INITIALIZE_USER = null;
    private static final UserEntity NOT_ADMIN_USER;
    private static final UserEntity ADMIN_USER;
    private static final GooglePojo GOOGLE_POJO;

    static {
        GOOGLE_POJO = new GooglePojo(ID,EMAIL, true,NAME,GIVEN_NAME, FAMILY_NAME, "","");
        FULL_NAME = GOOGLE_POJO.getFullName();
        NOT_ADMIN_USER = new UserEntity(ID,EMAIL, FULL_NAME, NOT_ADMIN_ROLE, ACTIVE_STATUS);
        ADMIN_USER = new UserEntity(ID,EMAIL, FULL_NAME, ADMIN_ROLE, ACTIVE_STATUS);
    }

    private LoginWithGoogleUseCase SUT;
    private GoogleGetUserEndpoint googleGetUserEndpoint;
    private UserRepository userRepository;
    private EmailValidator emailValidator;

    @Before
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
        assertThat(result, instanceOf(Success.class));
    }

    @Test
    public void loginWithGoogle_accessTokenPassedToEndPoint () throws ConnectionException {
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        SUT.executes(ACCESS_TOKEN);
        verify(googleGetUserEndpoint,times(1)).getUser(ac.capture());
        assertThat(ac.getAllValues().get(0), is(ACCESS_TOKEN));
    }

    @Test
    public void loginWithGoogle_endPointGeneralError_GeneralErrorReturned () throws ConnectionException {
        endpointGeneralError();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(GeneralError.class));
    }

    private void endpointGeneralError() throws ConnectionException {
        when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenReturn(new GoogleGetUserEndpoint.Result.GeneralError());
    }

    @Test
    public void loginWithGoogle_endPointAuthError_AuthErrorReturned () throws ConnectionException {
        endPointAuthError();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(AuthError.class));
    }

    private OngoingStubbing<GoogleGetUserEndpoint.Result> endPointAuthError() throws ConnectionException {
        return when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenReturn(new GoogleGetUserEndpoint.Result.AuthError());
    }

    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_noInteractionWithRepository () throws ConnectionException {
        endPointConnectionExceptionOccurs();
        SUT.executes(ACCESS_TOKEN);
        verifyNoMoreInteractions(userRepository);
    }

    private OngoingStubbing<GoogleGetUserEndpoint.Result> endPointConnectionExceptionOccurs() throws ConnectionException {
        return when(googleGetUserEndpoint.getUser(ACCESS_TOKEN)).thenThrow(new ConnectionException(""));
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
    public void loginWithGoogle_userRepositoryConnectionExceptionOccurs_GeneralErrorReturned () throws ConnectionException {
        userRepositoryConnectionExceptionOccurs();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(GeneralError.class));
    }

    private OngoingStubbing<UserEntity> userRepositoryConnectionExceptionOccurs() throws ConnectionException {
        return when (userRepository.getUserByEmail(EMAIL)).thenThrow(new ConnectionException(""));
    }

    @Test
    public void loginWithGoogle_endPointConnectionExceptionOccurs_GeneralErrorReturned () throws ConnectionException {
        endPointConnectionExceptionOccurs();
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(GeneralError.class));
    }

    @Test
    public void loginWithGoogle_accessTokenNotFptAndNotAdmin_NotAllowReturned () {
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(NOT_ADMIN_ROLE, false));
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(NotAllowed.class));
    }

    @Test
    public void loginWithGoogle_accessTokenNotFptButAdmin_successReturned () {
        when(emailValidator.check(EMAIL)).thenReturn(new EmailValidator.Result(ADMIN_ROLE, true));
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(Success.class));
    }

    @Test
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
        assertThat(ac.getAllValues().get(0), is(EMAIL));
        assertThat(ac.getAllValues().get(1), is(FULL_NAME));
    }

    @Test
    public void loginWithGoogle_successReturned_successUserMatchWithRepositoryUserData () throws ConnectionException {
        when(userRepository.getUserByEmail(EMAIL)).thenReturn(NOT_ADMIN_USER);
        Result result = SUT.executes(ACCESS_TOKEN);
        assertThat(result, instanceOf(Success.class));
        assertThat(((Success)result).user, equalTo(NOT_ADMIN_USER));
    }
}