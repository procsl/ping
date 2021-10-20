package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;

public interface TypeName extends Model {

    TypeName NONE_TYPE = new NoneTypeName();

    String getPackageName();

    String getClassName();

}
