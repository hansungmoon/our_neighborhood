package ywphsm.ourneighbor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ywphsm.ourneighbor.domain.store.Review;
import ywphsm.ourneighbor.domain.dto.ReviewDTO;
import ywphsm.ourneighbor.domain.dto.ReviewMemberDTO;
import ywphsm.ourneighbor.domain.dto.hashtag.HashtagDTO;
import ywphsm.ourneighbor.domain.file.AwsS3FileStore;
import ywphsm.ourneighbor.domain.file.FileStore;
import ywphsm.ourneighbor.domain.file.UploadFile;
import ywphsm.ourneighbor.domain.hashtag.Hashtag;
import ywphsm.ourneighbor.domain.member.Member;
import ywphsm.ourneighbor.domain.store.Store;
import ywphsm.ourneighbor.repository.hashtag.HashtagRepository;
import ywphsm.ourneighbor.repository.member.MemberRepository;
import ywphsm.ourneighbor.repository.review.ReviewRepository;
import ywphsm.ourneighbor.repository.store.StoreRepository;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static ywphsm.ourneighbor.domain.hashtag.HashtagOfStore.linkHashtagAndStore;
import static ywphsm.ourneighbor.domain.hashtag.HashtagUtil.*;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final MemberService memberService;
    private final StoreService storeService;

    private final MemberRepository memberRepository;

    private final HashtagRepository hashtagRepository;

    private final AwsS3FileStore awsS3FileStore;

    private final FileStore fileStore;

    private final EntityManager entityManager;

    @Transactional
    public Long save(ReviewDTO.Add dto, String hashtag) throws IOException, ParseException {
        Store linkedStore = storeService.findById(dto.getStoreId());
        Member linkedMember = memberService.findById(dto.getMemberId());

        List<UploadFile> newUploadFiles = awsS3FileStore.storeFiles(dto.getFile());

        Review review = dto.toEntity(linkedStore, linkedMember);
        linkedStore.addReview(review);
        linkedStore.updateRatingAverage(ratingAverage(linkedStore));
        linkedMember.addReview(review);

        review.addFile(newUploadFiles);

        if (!hashtag.isEmpty()) {
            Store findStore = storeService.findById(dto.getStoreId());

            List<String> hashtagNameList = getHashtagNameList(hashtag);

            saveHashtagLinkedStore(findStore, hashtagNameList);
        }

        return reviewRepository.save(review).getId();
    }

    @Transactional
    public Long delete(Long storeId, Long reviewId) {
        Review review = findOne(reviewId);
        Store store = storeService.findById(storeId);
        store.reviewDelete(review.getRating());

        review.getFileList().stream()
                .map(UploadFile::getStoredFileName).forEach(awsS3FileStore::deleteFile);

        reviewRepository.delete(review);
        entityManager.flush();

        store = storeService.findById(storeId);
        store.updateRatingAverage(ratingAverage(store));

        return reviewId;
    }

    public Review findOne(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(NoSuchElementException::new);
    }

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public Slice<ReviewMemberDTO> pagingReview(Long storeId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 5);
        Slice<ReviewMemberDTO> reviewMemberDTOS = reviewRepository.reviewPage(pageRequest, storeId);
        findImg(reviewMemberDTOS.getContent());
        return reviewMemberDTOS;
    }

    public List<ReviewMemberDTO> myReviewList(Long memberId) {
        return reviewRepository.myReview(memberId);
    }

    public double ratingAverage(Store store) {

        if (store.getReviewList().size() == 0) {
            return 0;
        }

        double ratingTotal = store.getRatingTotal();
        double count = store.getReviewList().size();
        double average = ratingTotal / count;

        return Math.round(average * 10) / 10.0;
    }

    private void saveHashtagLinkedStore(Store store, List<String> hashtagNameList) {
        for (String name : hashtagNameList) {
            boolean duplicateCheck = hashtagRepository.existsByName(name);

            Hashtag newHashtag;

            if (!duplicateCheck) {
                HashtagDTO hashtagDTO = HashtagDTO.builder()
                        .name(name)
                        .build();

                newHashtag = hashtagRepository.save(hashtagDTO.toEntity());
            } else {
                newHashtag = hashtagRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 해쉬태그입니다."));
            }

            linkHashtagAndStore(newHashtag, store);
        }
    }

    public List<ReviewMemberDTO> findImg(List<ReviewMemberDTO> content) {
        for (ReviewMemberDTO reviewMemberDTO : content) {
            List<String> imgUrl = reviewRepository.reviewImageUrl(reviewMemberDTO.getReviewId());
            reviewMemberDTO.setUploadImgUrl(imgUrl);
        }
        return content;
    }
}
