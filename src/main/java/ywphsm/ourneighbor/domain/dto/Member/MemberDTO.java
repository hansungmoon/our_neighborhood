package ywphsm.ourneighbor.domain.dto.Member;

import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import ywphsm.ourneighbor.domain.dto.ReviewDTO;
import ywphsm.ourneighbor.domain.file.UploadFile;
import ywphsm.ourneighbor.domain.member.Member;
import ywphsm.ourneighbor.domain.member.Role;
import ywphsm.ourneighbor.domain.menu.Menu;
import ywphsm.ourneighbor.domain.menu.MenuFeat;
import ywphsm.ourneighbor.domain.menu.MenuType;
import ywphsm.ourneighbor.domain.store.Store;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemberDTO {

    @Data
    @NoArgsConstructor
    public static class Add {

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
        private String password;

        @NotBlank
        private String passwordCheck;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String phoneNumber;

        @NotBlank
        private String certifiedNumber;

        private MultipartFile file;


        @Builder
        public Add(String username, String nickname, String birthDate, int gender, String userId, String password, String passwordCheck, String email, String phoneNumber, String certifiedNumber, MultipartFile file) {
            this.username = username;
            this.nickname = nickname;
            this.birthDate = birthDate;
            this.gender = gender;
            this.userId = userId;
            this.password = password;
            this.passwordCheck = passwordCheck;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.certifiedNumber = certifiedNumber;
            this.file = file;
        }

        public Member toEntity(int age, String encodedPassword) {
            return Member.builder()
                    .userId(userId)
                    .password(encodedPassword)
                    .username(username)
                    .nickname(nickname)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .gender(gender)
                    .birthDate(birthDate)
                    .age(age)
                    .role(Role.USER)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    public static class Detail {

        private Long memberId;

        private String userId;

        private String username;

        private String nickname;

        private String email;

        private String phoneNumber;

        private int age;

        private String birthDate;

        private int gender;         // 0 : 남자, 1 : 여자

        private String imgUrl;

        private List<MemberOfStoreDTO> memberOfStoreList = new ArrayList<>();

        private List<ReviewDTO.Detail> reviewList = new ArrayList<>();


        @Builder
        public Detail(Member member) {
            memberId = member.getId();
            userId = member.getUserId();
            username = member.getUsername();
            nickname = member.getNickname();
            email = member.getEmail();
            phoneNumber = member.getPhoneNumber();
            age = member.getAge();
            birthDate = member.getBirthDate();
            gender = member.getGender();
            imgUrl = member.getFile().getUploadImageUrl();
            reviewList = member.getReviewList().stream()
                    .map(ReviewDTO.Detail::new)
                    .collect(Collectors.toList());
            memberOfStoreList = member.getMemberOfStoreList().stream()
                    .map(MemberOfStoreDTO::new)
                    .collect(Collectors.toList());
        }

    }

    @Data
    @NoArgsConstructor
    public static class ApiAdd {

        @NotBlank
        private String username;

        @NotBlank
        private String nickname;

        @NotBlank
        private String birthDate;

        @NotNull
        private int gender;

        private Role role;

        @Email
        @NotBlank
        private String email;

        private UploadFile file;


        public ApiAdd(Member member) {
            this.username = member.getUsername();
            this.role = member.getRole();
            this.email = member.getEmail();
            this.file = member.getFile();
        }

        @Builder
        public ApiAdd(String username, String nickname, String birthDate, int gender, Role role, String email, UploadFile file) {
            this.username = username;
            this.nickname = nickname;
            this.birthDate = birthDate;
            this.gender = gender;
            this.role = role;
            this.email = email;
            this.file = file;
        }

        public Member toEntity(int age) {
            return Member.builder()
                    .username(username)
                    .nickname(nickname)
                    .email(email)
                    .gender(gender)
                    .birthDate(birthDate)
                    .age(age)
                    .file(file)
                    .role(Role.USER)
                    .build();
        }

    }
}
