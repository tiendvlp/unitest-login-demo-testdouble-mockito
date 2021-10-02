package login_convention;

import domain.entities.UserRole;


public interface EmailValidator {
      class Result {
        public UserRole role;
          public boolean isValid;

        public Result( UserRole role, boolean isValid) {
            this.role = role;
            this.isValid = isValid;
        }
    }

    Result check (String email);
}
