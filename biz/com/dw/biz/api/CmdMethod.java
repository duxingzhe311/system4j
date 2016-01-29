package com.dw.biz.api;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD})
@Inherited
public @interface CmdMethod
{
  public abstract String cmd_name();

  public abstract String cmd_desc();

  public abstract boolean is_changed();

  public abstract boolean is_needadmin_login();

  public abstract boolean is_needuser_login();
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.api.CmdMethod
 * JD-Core Version:    0.6.2
 */