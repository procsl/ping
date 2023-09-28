package cn.procsl.ping.boot.editor.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class EditorController {

    @PostMapping("/v1/blog/post")
    @ResponseStatus(code = HttpStatus.CREATED)
    public String createPost() {
        return "post created";
    }


}
