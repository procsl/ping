package cn.procsl.ping.boot.common.web;

import cn.procsl.ping.boot.common.dto.MessageVO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.Map;

@Controller
@RequestMapping
public class MarkWebController {

    @Created(path = "/restful/created", summary = "创建接口")
    public Collection<String> created(@RequestBody Collection<String> body) {
        return body;
    }

    @Accepted(path = "/restful/accepted", summary = "异步任务")
    public MessageVO accepted() {
        return new MessageVO("12345678");
    }

    @Changed(path = "/restful/changed", summary = "修改接口")
    public void changed(@RequestBody Collection<String> body) {
    }

    @Deleted(path = "restful/deleted", summary = "删除接口")
    public void delete() {
    }

    @Query(path = "restful/details", summary = "查询详情接口")
    public Map<String, String> queryDetails() {
        return null;
    }

    @MarkPageable
    @Query(path = "restful/page", summary = "查询详情接口")
    public FormatPage<String> pageable(Pageable pageable) {
        return null;
    }

}
