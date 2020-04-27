package cn.procsl.ping.app.admin.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author procsl
 * @date 2020/04/27
 */
@Controller
@RequestMapping
public class IndexController {

    @Autowired
    HttpServletRequest request;
//
//    {
//        String[] SESSION_ID = new String[]{"jsessionid", "[SESSION_ID]"};
//        String[] setting = new String[]{"setting", "[SESSION_ID]"};
//    }

    @GetMapping
    public void index(HttpSession session, HttpServletResponse response) throws IOException {
        String path = "/h2/index.do?jsessionid=[SESSION_ID]&language=zh_CN".replace("[SESSION_ID]", session.getId());
        response.sendRedirect(path);
    }

}
