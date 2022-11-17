package ywphsm.ourneighbor.repository.store;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.spatial.GeometryExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.*;
import org.geolatte.geom.cga.Circle;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ywphsm.ourneighbor.domain.store.Store;
import ywphsm.ourneighbor.repository.store.dto.SimpleSearchStoreDTO;

import java.util.Iterator;
import java.util.List;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.springframework.util.StringUtils.*;
import static ywphsm.ourneighbor.domain.category.QCategory.*;
import static ywphsm.ourneighbor.domain.category.QCategoryOfStore.*;
import static ywphsm.ourneighbor.domain.hashtag.QHashtag.*;
import static ywphsm.ourneighbor.domain.hashtag.QHashtagOfStore.*;
import static ywphsm.ourneighbor.domain.store.QStore.*;

@Slf4j
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Store> searchByKeyword(String keyword) {
        return queryFactory
                .select(store)
                .from(store)
                .where(keywordContains(keyword))
                .fetch();
    }

    @Override
    public List<Store> searchByCategory(Long categoryId) {
        return queryFactory
                .select(store)
                .from(store)
                .innerJoin(store.categoryOfStoreList, categoryOfStore)
                .innerJoin(categoryOfStore.category, category)
                .where(categoryOfStore.category.id.eq(categoryId), categoryOfStore.store.id.eq(store.id))
                .fetch();
    }

//  Projections 참고
//  https://wildeveloperetrain.tistory.com/94
//  geolatte 참고
//  https://github.com/GeoLatte/geolatte-geom  
    @Override
    public Slice<SimpleSearchStoreDTO> searchByHashtag(List<Long> hashtagIdList,
                                                       double nex, double ney,
                                                       double nwx, double nwy,
                                                       double swx, double swy,
                                                       double sex, double sey,
                                                       Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        buildHashtagIdEq(hashtagIdList, builder);

        List<SimpleSearchStoreDTO> list = queryFactory
                .select(Projections.constructor(SimpleSearchStoreDTO.class,
                        store.id, store.name, store.lon, store.lat, store.ratingTotal,
                        store.phoneNumber, store.status, store.address, store.file.uploadImageUrl
                )).distinct()
                .from(store)
                .innerJoin(store.hashtagOfStoreList, hashtagOfStore)
                .innerJoin(hashtagOfStore.hashtag, hashtag)
                .where(builder)
                .where(pointContains(nex, ney, nwx, nwy, swx, swy, sex, sey))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;

        if (list.size() > pageable.getPageSize()) {
            list.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(list, pageable, hasNext);
    }

    private BooleanExpression pointContains(double nex, double ney,
                                            double nwx, double nwy,
                                            double swx, double swy,
                                            double sex, double sey) {

        Polygon<G2D> polygon = polygon(WGS84, ring(g(ney, nex), g(nwy, nwx), g(swy, swx), g(sey, sex), g(ney, nex)));

        return GeometryExpressions
                .asGeometry(polygon)
                .contains(store.point);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }

        return store.name.contains(keyword);
    }


    private void buildHashtagIdEq(List<Long> hashtagIdList, BooleanBuilder builder) {
        if (hashtagIdList.isEmpty()) {
            return;
        }

        for (Long id : hashtagIdList) {
            builder.or(hashtagOfStore.hashtag.id.eq(id).and(hashtagOfStore.store.id.eq(store.id)));
        }
    }
}