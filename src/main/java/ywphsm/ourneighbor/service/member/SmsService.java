package ywphsm.ourneighbor.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ywphsm.ourneighbor.controller.form.PhoneCertifiedForm;
import ywphsm.ourneighbor.service.member.email.VerificationNumberConst;
import ywphsm.ourneighbor.service.member.login.SessionConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SmsService {

    //휴대폰 인증번호 발송
    public void certifiedPhoneNumber(String phoneNumber, HttpServletRequest request) {

        Random rand = new Random();
        String certifiedNumber = "";
        for (int i = 0; i < 6; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            certifiedNumber += ran;
        }

        log.info("수신자 번호:{}", phoneNumber);
        log.info("인증 번호:{}", certifiedNumber);

        Message coolsms = new Message(VerificationNumberConst.API_KEY, VerificationNumberConst.API_SECRET);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNumber);        // 수신전화번호
        params.put("from", "01038352375");    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
        params.put("type", "SMS");
        params.put("text", "Our neighborhood 휴대폰인증 메시지 : 인증번호는" + "[" + certifiedNumber + "]" + "입니다.");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
        }

        PhoneCertifiedForm certifiedForm = new PhoneCertifiedForm(phoneNumber, certifiedNumber);
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.PHONE_CERTIFIED, certifiedForm);

    }
}
