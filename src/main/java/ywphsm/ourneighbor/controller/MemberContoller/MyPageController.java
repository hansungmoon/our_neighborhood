package ywphsm.ourneighbor.controller.MemberContoller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import ywphsm.ourneighbor.domain.dto.StoreDTO;
import ywphsm.ourneighbor.domain.dto.Member.MemberDTO;
import ywphsm.ourneighbor.domain.member.Member;
import ywphsm.ourneighbor.domain.member.MemberOfStore;
import ywphsm.ourneighbor.service.MemberService;
import ywphsm.ourneighbor.service.login.SessionConst;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MyPageController {

    private final MemberService memberService;

    @GetMapping("/user/myPage")
    public String myPage(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member member,
                         Model model) {

        Member byId = memberService.findById(member.getId());
        MemberDTO.Detail detail = new MemberDTO.Detail(byId);
        model.addAttribute("memberDetail", detail);
        return "member/myPage";
    }

    @GetMapping("/user/myLike")
    public String myLike(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member member,
                         Model model) {
        Member findById = memberService.findById(member.getId());

        List<StoreDTO.Detail> likeList = findById.getMemberOfStoreList().stream()
                .filter(MemberOfStore::isStoreLike)
                .map(memberOfStore -> new StoreDTO.Detail(memberOfStore.getStore()))
                .collect(Collectors.toList());

        int count = 0;
        if (!likeList.isEmpty()) {
            count = likeList.size();
        }

        model.addAttribute("like", likeList);
        model.addAttribute("count", count);
        return "member/myLike";
    }
}
