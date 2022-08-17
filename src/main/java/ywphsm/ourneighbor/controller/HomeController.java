package ywphsm.ourneighbor.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.SessionAttribute;
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
    public String prac(Model model, @ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        List<Store> stores = storeService.searchByKeyword(storeSearchCond);
        model.addAttribute("stores", stores);

        return "map";
    }


    // 검색 뷰페이지 임시
    @GetMapping("/prac2")
    public String prac2(@ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "prac2";
    }

    // 검색 뷰페이지 임시
    @GetMapping("/prac3")
    public String prac3(@ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "prac3";
    }

    // 검색 뷰페이지 임시
    @GetMapping("/prac4")
    public String prac4(@ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {
        return "prac4";
    }


    @GetMapping("/")
    public String home(Model model, @ModelAttribute("storeSearchCond") StoreSearchCond storeSearchCond) {

        return "index";
    }

}
