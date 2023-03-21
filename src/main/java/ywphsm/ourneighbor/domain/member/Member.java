package ywphsm.ourneighbor.domain.member;

import lombok.*;
import ywphsm.ourneighbor.domain.BaseTimeEntity;
import ywphsm.ourneighbor.domain.file.UploadFile;
import ywphsm.ourneighbor.domain.store.Review;
import ywphsm.ourneighbor.domain.store.RequestAddStore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {
        "id", "userId", "password",
        "username", "nickname", "email",
        "phoneNumber", "age", "gender"
})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String userId;

    private String password;    // 암호화

    private String username;

    private String nickname;

    private String email;

    private String phoneNumber;

    private int age;

    private String birthDate;

    private int gender;         // 0 : 남자, 1 : 여자

    @Enumerated(EnumType.STRING)
    private Role role;


    //(N:N) Store
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<MemberOfStore> memberOfStoreList = new ArrayList<>();

    //(1:N) Review
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RequestAddStore> requestAddStoreList = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private UploadFile file;

    // 생성 메소드
    @Builder
    public Member(Long id, String userId, String password, String username, String nickname, String email, String phoneNumber, int age, int gender, Role role, String birthDate, UploadFile file) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.file = file;
    }

    //시큐리티 때매 추가
    public String getRoleKey() {
        return this.role.getKey();
    }


    //회원 수정
    public void updateMember(String nickname, String email) {
        this.nickname = nickname;
        this.email =email;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void addReview(Review review) {
        review.setMember(this);
        reviewList.add(review);
    }

    public void setFile(UploadFile file) {
        this.file = file;
    }

}