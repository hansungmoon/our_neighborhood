package ywphsm.ourneighbor.controller.MemberContoller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ywphsm.ourneighbor.config.ScriptUtils;
import ywphsm.ourneighbor.controller.form.LoginForm;
import ywphsm.ourneighbor.domain.dto.Member.MemberDTO;
import ywphsm.ourneighbor.domain.member.Member;
import ywphsm.ourneighbor.domain.member.Role;
import ywphsm.ourneighbor.service.login.SessionConst;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MemberController {

    @GetMapping("/login")
    public String login(@ModelAttribute(name = "loginForm") LoginForm loginForm,
                        @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member member,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model, HttpServletResponse response) throws IOException {

        if (member != null) {
            ScriptUtils.alertAndMovePage(response, "이미 로그인이 되어있습니다.", "/");
        }

        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "member/loginForm";
    }

    @GetMapping("/sign_up")
    public String signUp(@ModelAttribute(value = "dto") MemberDTO.Add dto) {
        return "member/signUpForm";
    }

    @GetMapping("/admin/memberRole/edit")
    public String memberRoleEdit() {
        return "member/edit/member_role_edit";
    }

}
