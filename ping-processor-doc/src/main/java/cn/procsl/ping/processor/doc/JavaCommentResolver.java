package cn.procsl.ping.processor.doc;


import java.util.*;
import java.util.stream.Collectors;

public class JavaCommentResolver {

    final String comment;

    final Set<String> arrays;

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
            return null;
        }
        return tags.get(0);
    }

    public String getParameterComment(String paramName) {
        List<String> tags = this.findTags("@param");
        if (tags.isEmpty()) {
            return null;
        }
        for (String tag : tags) {
            if (tag.startsWith(paramName)) {
                return tag.replaceAll(paramName, "").trim();
            }
        }
        return null;
    }

    public JavaCommentResolver(String comment) {
        this.comment = comment;
        if (comment == null || comment.isBlank()) {
            arrays = Collections.emptySet();
            return;
        }
        this.arrays = Arrays.stream(comment.split("\n")).filter(item -> !item.isEmpty()).map(String::trim).collect(Collectors.toSet());
    }
}
