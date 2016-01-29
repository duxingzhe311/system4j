package com.dw.comp.mail.config;

import com.dw.comp.mail.util.StringUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class Internationalization
  implements Serializable
{
  private Locale m_SystemLocale;
  private Locale m_DefaultViewLocale;
  private Set m_ViewLocales;

  public Internationalization()
  {
    this.m_ViewLocales = Collections.synchronizedSet(new HashSet(10));
  }

  public String getSystemLanguage()
  {
    return this.m_SystemLocale.getLanguage();
  }

  public void setSystemLanguage(String language)
  {
    this.m_SystemLocale = new Locale(language, "");
  }

  public Locale getSystemLocale()
  {
    return this.m_SystemLocale;
  }

  public void setSystemLocale(Locale locale)
  {
    this.m_SystemLocale = locale;
  }

  public String getDefaultViewLanguage()
  {
    return this.m_DefaultViewLocale.getLanguage();
  }

  public void setDefaultViewLanguage(String language)
  {
    this.m_DefaultViewLocale = new Locale(language, "");
  }

  public Locale getDefaultViewLocale()
  {
    return this.m_DefaultViewLocale;
  }

  public void setDefaultViewLocale(Locale locale)
  {
    this.m_DefaultViewLocale = locale;
  }

  public String getViewLanguageList()
  {
    return StringUtil.join(listViewLanguages(), ",");
  }

  public void setViewLanguageList(String list)
  {
    String[] locales = StringUtil.split(list, ",");
    this.m_ViewLocales.clear();
    for (int i = 0; i < locales.length; i++)
      addViewLocale(new Locale(locales[i], ""));
  }

  public String[] listViewLanguages()
  {
    String[] strs = new String[this.m_ViewLocales.size()];
    int i = 0;
    for (Iterator iterator = this.m_ViewLocales.iterator(); iterator.hasNext(); i++) {
      strs[i] = ((Locale)(Locale)iterator.next()).getLanguage();
    }
    return strs;
  }

  public Locale[] listViewLocales()
  {
    Locale[] strs = new Locale[this.m_ViewLocales.size()];
    return (Locale[])this.m_ViewLocales.toArray(strs);
  }

  public void addViewLanguage(String lang)
  {
    this.m_ViewLocales.add(new Locale(lang, ""));
  }

  public void addViewLocale(Locale locale)
  {
    this.m_ViewLocales.add(locale);
  }

  public void removeViewLanguage(String lang)
  {
    this.m_ViewLocales.remove(new Locale(lang, ""));
  }

  public void removeViewLocale(Locale locale)
  {
    this.m_ViewLocales.remove(locale);
  }

  public boolean isSupportedViewLanguage(String lang)
  {
    return this.m_ViewLocales.contains(new Locale(lang, ""));
  }

  public boolean isSupportedViewLocale(Locale loc)
  {
    return this.m_ViewLocales.contains(loc);
  }
}
