package cn.procsl.ping.boot.domain.business.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PathUtilsTest {

    @Test
    public void isSubPath() {
        int res = PathUtils.isSubPath("root", "root/1/2/3/4", "/");
        Assert.assertEquals(res, 1);

        int res1 = PathUtils.isSubPath("/root", "root/1/2/3/4", "/");
        Assert.assertEquals(res1, 1);

        int res2 = PathUtils.isSubPath("/root", "/root/1/2/3/4", "/");
        Assert.assertEquals(res2, 1);

        int res3 = PathUtils.isSubPath("/root", "/root/1/2/3/4/", "/");
        Assert.assertEquals(res3, 1);

        int res4 = PathUtils.isSubPath("/root/", "/root/1/2/3/4/", "/");
        Assert.assertEquals(res4, 1);

        int res5 = PathUtils.isSubPath("/root/", "/root//1/2/3/4/", "/");
        Assert.assertEquals(res5, 1);

        int res6 = PathUtils.isSubPath("/root", "root", "/");
        Assert.assertEquals(res6, 0);

        int res7 = PathUtils.isSubPath("root", "root", "/");
        Assert.assertEquals(res7, 0);

        int res8 = PathUtils.isSubPath("root", "roo", "/");
        Assert.assertEquals(res8, -1);

        int res9 = PathUtils.isSubPath("root", "/roo", "/");
        Assert.assertEquals(res9, -1);

        int res10 = PathUtils.isSubPath("root", "/roo/", "/");
        Assert.assertEquals(res10, -1);
    }

    @Test
    public void split() {
        List<String> nodes = PathUtils.split("/root", "/");
        Assert.assertEquals(nodes.get(0), "root");

        List<String> nodes1 = PathUtils.split("/root/1/2", "/");
        Assert.assertEquals(nodes1.get(0), "root");
        Assert.assertEquals(nodes1.get(1), "1");
        Assert.assertEquals(nodes1.get(2), "2");

        List<String> nodes2 = PathUtils.split("root/1/2", "/");
        Assert.assertEquals(nodes2.get(0), "root");

        List<String> nodes3 = PathUtils.split("/", "/");
        Assert.assertEquals(nodes3.isEmpty(), true);

        List<String> nodes4 = PathUtils.split("", "/");
        Assert.assertEquals(nodes4.isEmpty(), true);
    }

    @Test
    public void distinct() {
        String[] paths = {"root", "root/1", "/root/2", "1", "2", "roo", "/root"};
        List<String> list = Arrays.stream(paths).map(item -> PathUtils.standardize(item, "/")).collect(Collectors.toList());
        List<String> des = PathUtils.distinct(list, "/");
        Assert.assertTrue(des.contains("/root"));
        Assert.assertTrue(des.contains("/1"));
        Assert.assertTrue(des.contains("/2"));
        Assert.assertTrue(des.contains("/roo"));
        Assert.assertEquals(des.size(), 4);
    }

    @Test
    public void standardize() {
        String path = PathUtils.standardize("root", "/");
        Assert.assertEquals("/root", path);

        String path1 = PathUtils.standardize("root/", "/");
        Assert.assertEquals("/root", path1);

        String path2 = PathUtils.standardize("/root/", "/");
        Assert.assertEquals("/root", path2);

        String path3 = PathUtils.standardize("/root", "/");
        Assert.assertEquals("/root", path3);

        String path4 = PathUtils.standardize("/", "/");
        Assert.assertEquals("/", path4);

        String path5 = PathUtils.standardize("///", "/");
        Assert.assertEquals("/", path5);

        String path6 = PathUtils.standardize("", "/");
        Assert.assertEquals("/", path6);

        String path7 = PathUtils.standardize(" ", "/");
        Assert.assertEquals("/", path7);

        String path8 = PathUtils.standardize("///root", "/");
        Assert.assertEquals("/root", path8);

        String path9 = PathUtils.standardize("///root//", "/");
        Assert.assertEquals("/root", path9);

        String path10 = PathUtils.standardize("//root// ", "/");
        Assert.assertEquals("/root", path10);
    }

    @Test
    public void isRoot() {
        boolean root1 = PathUtils.isRoot("root", "/");
        Assert.assertTrue(root1);

        boolean root2 = PathUtils.isRoot("/root", "/");
        Assert.assertTrue(root2);

        boolean root3 = PathUtils.isRoot("root/", "/");
        Assert.assertTrue(root3);

        boolean root4 = PathUtils.isRoot("/", "/");
        Assert.assertTrue(root4);

        boolean root5 = PathUtils.isRoot("root/foo", "/");
        Assert.assertFalse(root5);
    }

    @Test
    public void isEmpty() {
        boolean empty = PathUtils.isEmpty("root", "/");
        Assert.assertFalse(empty);

        boolean empty1 = PathUtils.isEmpty("", "/");
        Assert.assertTrue(empty1);

        boolean empty2 = PathUtils.isEmpty("  ", "/");
        Assert.assertTrue(empty2);

        boolean empty3 = PathUtils.isEmpty("/", "/");
        Assert.assertTrue(empty3);

        boolean empty4 = PathUtils.isEmpty("//", "/");
        Assert.assertTrue(empty4);
    }
}
