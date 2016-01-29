package com.dw.comp;

/**
 * 实体对象必须实现的接口
 * 
 * 一个实体代表一个构件中与其他构件进行信息交流的数据单元
 * 它有一个对应构件的唯一id
 * 构件中对应与每个实体，都有相关的创建、修改、保存等View和Action
 * 
 * @author Jason Zhu
 */
public interface IEntity
{
	public String getEntityId() ;
}
