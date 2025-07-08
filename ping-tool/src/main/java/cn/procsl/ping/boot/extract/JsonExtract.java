package cn.procsl.ping.boot.extract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.model.Task;
import com.meilisearch.sdk.model.TaskInfo;
import com.meilisearch.sdk.model.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class JsonExtract {

    private final static Configuration document = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL).jsonProvider(new JacksonJsonProvider());
    private final static Pattern pattern = Pattern.compile("^(?<time>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) +INFO +-{3} +.+SVC接口返回信息:(?<content>\\{.+}),状态码：\\d{3}$");
    private final static JsonPath path = JsonPath.compile("$.responses[*].hits.hits[*]._source.message");
    private final static JsonPath path2 = JsonPath.compile("$.data");
    private final static File dir = new File("C:\\Users\\procsl\\Desktop\\fetch");
    private final static Set<String> sets = Set.of("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
    private final static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        File[] list = dir.listFiles((d, name) -> {
            if (!d.isDirectory()) {
                return false;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                LocalDate.parse(name, formatter);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        });
        if (list == null) {
            return;
        }


        Config config = new Config("http://127.0.0.1:7700", "9WCKcxCuiluzjJQDjzeL1qDtBqR8toceX9S91dyHfd0");
        Client client = new Client(config);


        HashSet<String> set = new HashSet<>();
        for (File s : list) {
            File[] j = s.listFiles((json, name) -> {
                if (!name.endsWith(".json")) {
                    return false;
                }
                String n = name.replaceAll("\\.json$", "");
                return sets.contains(n);
            });
            if (j == null) {
                continue;
            }
            List<File> ss = Arrays.stream(j).sorted().toList();

            for (File file : ss) {

                String parent = file.getParentFile().getAbsolutePath();
                String resultName = "task." + file.getName();
                File resultFile = new File(parent, resultName);

//                extract2(file, set);
                if (getResult(client, resultFile)) {
                    continue;
                }

                List result = extract(file);
                String json = mapper.writeValueAsString(result);
                TaskInfo taskInfo = client.index("extract").addDocuments(json);
                boolean bb = writeResultToFile(resultFile, taskInfo.getStatus(), taskInfo.getTaskUid());
                log.info("提交: {}", resultFile.getAbsolutePath());
                if (!bb) {
                    log.info("出现错误: {}, {}", taskInfo.getStatus(), taskInfo.getTaskUid());
                    System.exit(-1);
                }
            }
        }
        for (String s : set) {
            log.info("[{}]", s);
        }
    }

    static final Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();

    private static void extract2(File file, HashSet<String> sets) throws IOException {
        FileInputStream is = new FileInputStream(file);
        BOMInputStream buf = BOMInputStream.builder().setInputStream(is).get();
        List<String> result = path.read(buf, document);
        is.close();
        result.stream().forEach(s -> {
            if (s == null || s.isEmpty()) {
                return;
            }
            Matcher matcher = pattern.matcher(s);
            if (!matcher.find()) {
                return;
            }
            String content = matcher.group("content");
            List<String> tmp = JsonPath.using(conf).parse(content).read("$..*");
            sets.addAll(tmp);
        });
    }

    private static void jsonPath(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        BOMInputStream buf = BOMInputStream.builder().setInputStream(is).get();
        List<String> result = path.read(buf, document);
        DocumentContext context = JsonPath.parse(result);
        is.close();
    }

    private static boolean getResult(Client client, File resultFile) throws IOException {
        if (resultFile.exists()) {
            String task = FileUtils.readFileToString(resultFile, StandardCharsets.UTF_8);
            if (task == null || task.isEmpty()) {
                resultFile.deleteOnExit();
                return false;
            }

            if (TaskStatus.SUCCEEDED.taskStatus.equals(task)) {
                log.info("已成功处理: {}", resultFile.getAbsolutePath());
                return true;
            }

            if (TaskStatus.ENQUEUED.taskStatus.equals(task)) {
                log.info("处理中: {}", resultFile.getAbsolutePath());
            }

            try {
                int uid = Integer.parseInt(task);
                Task res = client.getTask(uid);
                return writeResultToFile(resultFile, res.getStatus(), uid);
            } catch (RuntimeException e) {
                resultFile.deleteOnExit();
                log.info("出现错误: ", e);
                return false;
            }
        }
        return false;
    }

    private static boolean writeResultToFile(File resultFile, TaskStatus status, int id) throws IOException {
        boolean re = TaskStatus.SUCCEEDED.equals(status);
        if (re) {
            FileUtils.write(resultFile, TaskStatus.SUCCEEDED.toString(), StandardCharsets.UTF_8);
            return true;
        }
        if (TaskStatus.ENQUEUED.equals(status)) {
            FileUtils.write(resultFile, id + "", StandardCharsets.UTF_8);
            return true;
        }
        return false;
    }

    public static List<Map<String, Object>> extract(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        BOMInputStream buf = BOMInputStream.builder().setInputStream(is).get();
        List<String> result = path.read(buf, document);
        is.close();
        return result.stream().map(s -> {
            if (s == null || s.isEmpty()) {
                return null;
            }
            Matcher matcher = pattern.matcher(s);
            if (!matcher.find()) {
                return null;
            }
            String time = matcher.group("time");
            String content = matcher.group("content");
            Object data = path2.read(content, document);
            if (data == null) {
                return null;
            }

            String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
            String pattern2 = "yyyyMMddHHmmssSSS";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            SimpleDateFormat formatter2 = new SimpleDateFormat(pattern2);
            if (data instanceof Map map) {
                HashMap<String, Object> hash = new HashMap<>();
                Map<String, ?> mas = ((Map<String, ?>) data);
                Object dto = mas.get("svcEBasicInfoDto");
                hash.computeIfAbsent("members", (ss) -> new HashSet<>());
                hash.computeIfAbsent("mobiles", (ss) -> new HashSet<>());
                hash.computeIfAbsent("certs", (ss) -> new HashSet<>());
                if ((dto instanceof Map<?, ?> ddo)) {
                    hash.put("identifyCnName", ddo.get("identifyCnName"));
                    hash.put("identifyEnName", ddo.get("identifyEnName"));
                    hash.put("createDate", ddo.get("createDate"));
                    {
                        HashSet<String> set = (HashSet<String>) hash.get("members");
                        Object members = ddo.get("memberNos");
                        if (members instanceof List<?>) {
                            ((List<?>) members).forEach(k -> {
                                if (k == null) {
                                    return;
                                }
                                set.add(k.toString());
                            });
                        }
                        hash.put("members", set);
                    }

                    {

                        HashSet<String> set = (HashSet<String>) hash.get("members");
                        Object bdo = ddo.get("svcEIdentifyBindInfoDto");
                        HashSet<String> mobiles = (HashSet<String>) hash.get("mobiles");
                        if (bdo instanceof Map<?, ?> bbdo) {
                            set.add((String) bbdo.get("bindMemberNo"));
                            mobiles.add((String) bbdo.get("identifyMobile"));
                        }

                    }

                    {

                        HashSet<String> set = (HashSet<String>) hash.get("certs");
                        set.add((String) ddo.get("identifyCertNum"));
                        Object ccs = ddo.get("svcPcCertificateDtos");
                        if (ccs instanceof List<?> cccs) {
                            for (Object ccc : cccs) {
                                if (ccc instanceof Map<?, ?> cca) {
                                    set.add((String) cca.get("certNum"));
                                }
                            }
                        }

                        Object certificates = ddo.get("certificates");
                        if (certificates instanceof List<?> cmp) {
                            for (Object o : cmp) {
                                if (o instanceof Map<?, ?> oo) {
                                    Object cert = oo.get("certNum");
                                    set.add((String) cert);
                                }
                            }
                        }

                    }

                    {
                        HashSet<String> set = (HashSet<String>) hash.get("mobiles");
                        set.add((String) ddo.get("identifyMobile"));
                        set.add((String) ddo.get("mobliePhone"));
                        set.add((String) ddo.get("mobileNo"));
                        set.add((String) ddo.get("ffpMobileNo"));
                    }
                }

                Object memberInfoDto = mas.get("memberInfoDto");
                if (memberInfoDto instanceof Map<?, ?> mid) {
                    hash.put("identifyCnName", mid.get("chineseName"));
                    hash.put("identifyEnName", mid.get("englishName"));
                    hash.put("birthDay", mid.get("birthDay"));
                    {
                        HashSet<String> set = (HashSet<String>) hash.get("members");
                        set.add((String) mid.get("memberNo"));
                    }

                    {
                        HashSet<String> set = (HashSet<String>) hash.get("certs");
                        Object certificates = mid.get("certificates");
                        if (certificates instanceof List<?> cmp) {
                            for (Object o : cmp) {
                                if (o instanceof Map<?, ?> oo) {
                                    Object cert = oo.get("certNum");
                                    set.add((String) cert);
                                }
                            }
                        }
                    }

                    {
                        HashSet<String> set = (HashSet<String>) hash.get("mobiles");
                        set.add((String) mid.get("mobliePhone"));
                    }

                }


                if (hash.size() <= 3) {
                    return null;
                }


                {
                    HashSet<String> mobiles = (HashSet<String>) hash.remove("mobiles");
                    String mm = String.join(",", mobiles);
                    if (!mm.isEmpty()) {
                        hash.put("mobiles", mm);
                    }
                }
                {
                    HashSet<String> certs = (HashSet<String>) hash.remove("certs");
                    String mm = String.join(",", certs);
                    if (!mm.isEmpty()) {
                        hash.put("certs", mm);
                    }
                }
                {
                    HashSet<String> members = (HashSet<String>) hash.remove("members");
                    String mm = String.join(",", members);
                    if (!mm.isEmpty()) {
                        hash.put("members", mm);
                    }
                }

                hash.put("request_time", time);
                try {
                    String id = formatter2.format(formatter.parse(time));
                    hash.put("id", id);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                return hash;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
