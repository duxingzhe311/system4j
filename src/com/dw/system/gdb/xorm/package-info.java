/**
 * XORM - XmlData - Object - Relational Mapping
 * 
 * 在大构建系统中,最关键的问题是如何连接的问题
 * 而连接的关键是:如何使用统一的数据结构,同时又能满足不相互依赖,还可以满足内部实现方便的需要
 * 
 * XOR-Mapping就是这种东东.
 * 1,X - XmlData是可以满足互相不依赖的通用xml结构,可以用来方便的传递信息
 * 2,O - Object是标准的类及对象,可以为内部的实现提供方便,比如构件内部通过O来做数据的生成和获取
 * 3,R - 关系数据库,也是应用系统大问题之一,他和XO有着先天的矛盾.
 * 		通过适当的R,我们可以解决一些繁琐的数据表访问问题,如直接把对象写入到数据库中
 * 		同时,保持数据库关系访问的另一层次的灵活性(SQL)-通过GDB解决.
 * 		避免OR-Mapping的滥用
 * 
 * XOR-Mapping特点
 * 0, 定义信息以类为中心,每个类对应一个表.
 * 
 * 1,XO : 需要定义类,并根据XmlData Annotion指定XmlData 成员 - 建立类和XmlData之间的关系
 * 2,根据本包 指定的Annotion,定义类对应的数据库表.
 * 3,自动建表的语句,通过gdb_xxx.xml进行 - 里面只需要指定类的名称即可
 * 4,如果不同表之间需要建立外键等约束关系,则通过gdb_xxx.xml文件进行.
 */
package com.dw.system.gdb.xorm;