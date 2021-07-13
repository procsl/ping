package cn.procsl.ping.processor.doc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JavaCommentResolver {

    final String comment;

    final List<String> arrays;

    public List<String> findTags(String key) {
        ArrayList<String> list = new ArrayList<>();
        for (String array : arrays) {
            if (array.startsWith(key)) {
                list.add(array.replaceAll(key, "").trim());
            }
        }
        return list;
    }

    public String findOndTag(String key) {
        List<String> tags = this.findTags(key);
        if (tags.isEmpty()) {
            return "";
        }
        return tags.get(0);
    }

    public String getParameterComment(String paramName) {
        List<String> tags = this.findTags("@param");
        if (tags.isEmpty()) {
            return "";
        }
        for (String tag : tags) {
            if (tag.startsWith(paramName)) {
                return tag.replaceAll(paramName, "").trim();
            }
        }
        return "";
    }

    public String getName() {
        return this.findOndTag("@name");
    }

    public String getDescription() {
        return this.findOndTag("@description");
    }

    public JavaCommentResolver(String comment) {
        this.comment = comment;
        if (comment == null || comment.isBlank()) {
            arrays = Collections.emptyList();
            return;
        }
        this.arrays = Arrays.stream(comment.split("\n")).filter(item -> !item.isEmpty()).map(String::trim).collect(Collectors.toList());
    }

    public String getGeneralComment() {
        List<String> list = this.arrays.stream().filter(item -> !item.startsWith("@")).collect(Collectors.toList());
        return String.join("\n", list);
    }
}
