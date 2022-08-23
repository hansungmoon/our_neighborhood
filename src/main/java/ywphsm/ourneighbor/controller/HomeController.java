package ywphsm.ourneighbor.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;
import ywphsm.ourneighbor.domain.dto.MenuAddDTO;
import ywphsm.ourneighbor.domain.member.Member;
import ywphsm.ourneighbor.service.login.SessionConst;
import org.springframework.web.bind.annotation.ModelAttribute;
import ywphsm.ourneighbor.domain.search.StoreSearchCond;
import ywphsm.ourneighbor.domain.store.Store;
import ywphsm.ourneighbor.service.MemberService;
import ywphsm.ourneighbor.service.StoreService;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class HomeController {

    private final MemberService memberService;
    private final StoreService storeService;

    // 검색 뷰페이지 임시
    @GetMapping("/map")
    public String map(Model model, @ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "map";
    }

    @GetMapping("/prac2")
    public String prac2(@ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "prac2";
    }


    @GetMapping("/prac3/{storeId}")
    public String addMenu(@PathVariable Long storeId, Model model) {
        MenuAddDTO dto = new MenuAddDTO();
        dto.setStoreId(storeId);
        model.addAttribute("menu", dto);
        return "prac3";
    }

    @GetMapping("/prac4")
    public String prac4(@ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "prac4";
    }

    @GetMapping("/")
    public String index(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member member,
                        Model model) {

        model.addAttribute("member", member);
        return "index";
    }

}