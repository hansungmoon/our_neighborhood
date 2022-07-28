package ywphsm.ourneighbor.controller.form;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class MemberForm {

    @NotBlank
    private String username;

    @NotBlank
    private String nickname;

    @NotBlank
    private String birthDate;

    @NotNull
    private int gender;

    @NotBlank
    private String userId;

    @NotBlank
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{6,12}",
            message = "비밀번호는 영문자와 숫자, 특수기호가 적어도 1개 이상 포함된 6자~12자의 비밀번호여야 합니다.")
    private String password;

    @NotBlank
    private String passwordCheck;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;
}
