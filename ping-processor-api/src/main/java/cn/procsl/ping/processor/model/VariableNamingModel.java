package cn.procsl.ping.processor.model;

import lombok.Data;

import java.util.List;

@Data
public class VariableNamingModel extends NamingModel {

    List<NamingModel> variables;

}
