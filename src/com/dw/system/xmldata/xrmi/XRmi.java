package com.dw.system.xmldata.xrmi;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XRmi {
	String reg_name();
}
