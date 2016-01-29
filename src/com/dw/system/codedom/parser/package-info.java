/**
 * 用户系统查询语言
 * 
 * 其中语言的分析采用javaCC实现，主要jjTree文件UQL.jjt
 * 编译生成命令：jjtree UQL.jjt 和 javacc UQL.jj
 * 
 * 其中AST开头的文件都是后续编译修改的文件，重新生成时不要做删除
 * @author Jason Zhu
 */
package com.dw.system.codedom.parser;
