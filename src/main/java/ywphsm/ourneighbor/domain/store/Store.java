package ywphsm.ourneighbor.domain.store;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import ywphsm.ourneighbor.domain.*;
import ywphsm.ourneighbor.domain.category.CategoryOfStore;
import ywphsm.ourneighbor.domain.embedded.Address;
import ywphsm.ourneighbor.domain.embedded.BusinessTime;
import ywphsm.ourneighbor.domain.file.UploadFile;
import ywphsm.ourneighbor.domain.hashtag.HashtagOfStore;
import ywphsm.ourneighbor.domain.member.MemberOfStore;
import ywphsm.ourneighbor.domain.menu.Menu;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {
        "id", "name", "lon", "lat",
        "phoneNumber", "notice", "intro", "status"
})
@Entity
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "store_id")
    private Long id;

    private String name;

    private Double lat;                // 위도

    private Double lon;                // 경도

    @Column(columnDefinition = "Point")
    private Point<G2D> point;

    public void addPoint(Point<G2D> point) {
        this.point = point;
    }

    private String phoneNumber;

    private String notice;                    // 가게 소식

    private String intro;                     // 가게 소개

    private int ratingTotal;

    private String homePage;

//    @OneToMany(mappedBy = "store")
//    private List<DaysOfStore> daysOfStore;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> offDays = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.OPEN;               // 가게 오픈 상황 (default: OPEN)

    @Enumerated(EnumType.STRING)
    private ParkAvailable park;

    private String parkDetail;


    /*
        임베디드 타입
     */
    @Embedded
    private Address address;

    @Embedded
    private BusinessTime businessTime;

    /*
        JPA 연관 관계 매핑
     */
    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL)
    private UploadFile file;

    public void setFile(UploadFile file) {
        this.file = file;
    }

    // Menu (1:N)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Menu> menuList = new ArrayList<>();

    // Review(N:1)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviewList = new ArrayList<>();

    // Category (N:N)
    @OneToMany(mappedBy = "store", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<CategoryOfStore> categoryOfStoreList = new ArrayList<>();

    // Hashtag (N:N)
    @OneToMany(mappedBy = "store", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<HashtagOfStore> hashtagOfStoreList = new ArrayList<>();

    // Member (N:N)
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<MemberOfStore> memberOfStoreList = new ArrayList<>();



    /*
        생성자
     */
    @Builder
    public Store(Long id, String name, Double lat, Double lon,
                 Point point, String phoneNumber, BusinessTime businessTime, String notice, String intro,
                 List<String> offDays, StoreStatus status, Address address,
                 ParkAvailable park, String parkDetail, String homePage,
                 List<Menu> menuList, List<CategoryOfStore> categoryOfStoreList,
                 List<HashtagOfStore> hashtagOfStoreList) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.point = point;
        this.phoneNumber = phoneNumber;
        this.businessTime = businessTime;
        this.notice = notice;
        this.intro = intro;
        this.offDays = offDays;
        this.status = status;
        this.address = address;
        this.park = park;
        this.homePage = homePage;
        this.parkDetail = parkDetail;
        this.menuList = menuList;
        this.categoryOfStoreList = categoryOfStoreList;
        this.hashtagOfStoreList = hashtagOfStoreList;
    }

    /*
        === 연관 관계 편의 메소드 ===
     */
    public void addMenu(Menu menu) {
        menu.setStore(this);
        menuList.add(menu);
    }

    public void addReview(Review review) {
        this.ratingTotal += review.getRating();
        review.setStore(this);
        reviewList.add(review);
    }


    /*
        === 생성 메소드 ===
    */

    /*
        === 비즈니스 로직 추가 ===
     */
    public void update(Store store) {
        this.name = store.getName();
        this.phoneNumber = store.getPhoneNumber();
        this.lat = store.getLat();
        this.lon = store.getLon();
        this.businessTime = store.getBusinessTime();
        this.notice = store.getNotice();
        this.intro = store.getIntro();
        this.park = store.getPark();
        this.parkDetail = store.getParkDetail();
        this.homePage = store.getHomePage();
        this.offDays = store.getOffDays();
        this.address = store.getAddress();
    }

    public void updateStatus(StoreStatus status) {
        this.status = status;
    }

    public void reviewDelete(Integer rating) {
        this.ratingTotal -= rating;
    }
}