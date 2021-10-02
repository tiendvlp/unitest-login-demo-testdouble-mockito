package login_convention;

import domain.entities.UserRole;

public class EmailValidatorImp implements EmailValidator {

    public EmailValidatorImp () {

    }

    public Result check (String email) {
        String domain = email.substring(email.indexOf("@")+1);

        if (!(domain.equals("fpt.edu.vn") || domain.equals("fe.edu.vn"))) {
            return new Result(null, false);
        }

        int numOfDigit = email.replaceAll("\\D+", "").length();

        UserRole userRole;
        // Student
        if (numOfDigit >= 4) {
            userRole = new UserRole(UserRole.TYPE.STUDENT);
            // Staff or Lecturer
        } else {
            userRole = new UserRole(UserRole.TYPE.GUEST);
        }

        return new Result(userRole, true);
    }

}
