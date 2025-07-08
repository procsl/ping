package cn.procsl.ping.boot.extract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;

public class JsonPushToDatabase {

    public static void main(String[] args) throws IOException {
        FileInputStream is = new FileInputStream("C:\\Users\\procsl\\Desktop\\fetch\\2025-01-01\\extract.08.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nodes = mapper.readTree(is);
        boolean arrays = nodes.isArray();
        if (!arrays) {
            return;
        }
        for (JsonNode node : nodes) {
//            node.
        }
    }



}
