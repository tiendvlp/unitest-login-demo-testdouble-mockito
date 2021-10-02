package domain.ports.testonly.testonly;


import domain.entities.UserEntity;
import domain.entities.UserRole;
import domain.entities.UserStatus;
import domain.ports.errors.ConnectionException;


public interface UserRepository {
    UserEntity getUserByEmail (String email) throws ConnectionException;

    UserEntity addUser (String email, String fullName, String avatar, UserRole role, UserStatus userStatus) throws ConnectionException;
}

